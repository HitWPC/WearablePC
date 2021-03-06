package cn.hitftcl.wearablepc.Service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.clj.fastble.BleManager;
import com.clj.fastble.UUIDs;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hitftcl.wearablepc.Bluetooth.ECGLinkedList;
import cn.hitftcl.wearablepc.Bluetooth.Utils;
import cn.hitftcl.wearablepc.Model.ActionTable;
import cn.hitftcl.wearablepc.Model.ECGModel;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.Utils.BroadCastUtil;
import cn.hitftcl.wearablepc.Utils.Constant;
import cn.hitftcl.wearablepc.Utils.ModelOperation.BDOperation;

import cn.hitftcl.wearablepc.Utils.ModelOperation.EnviromentTableOperation;
import cn.hitftcl.wearablepc.Utils.ModelOperation.HeartOperation;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

import static com.defqx.classic.BluetoothSerial.bytes2HexString;


public class SensorDataService extends Service {
    public final static  String TAG = "debug001";

    public static final String ACTION_BD_AVAILABLE = "cn.hitftcl.wearable.ACTION_BD_AVAILABLE";
    public static final String MyIP = (DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class)).getIp();
    public final static String Avivable_BeiDouInfo_regex =  "\\$.+[NS].+[WE].+\\r\\n";
    public final static String BeiDouInfo_regex =  "\\$.+\\r\\n";

    public static StringBuffer temp_bd_data = new StringBuffer();

    public static short RECEIVED_RELIABLE_TAG_TEST = -1;

//    ThreadPoolExecutor ThreadExecutor = null;
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
//        ThreadExecutor =  ThreadPool.getInstance();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //TODO 收到ScanFragment发送的广播数据
            if (BroadCastUtil.notifyDataChanged.equals(action)) {
                final byte[]  data = intent.getByteArrayExtra(BroadCastUtil.EXTRA_DATA);

                final String uuid = intent.getStringExtra(BroadCastUtil.EXTRA_UUID);
                if (data != null) {
                    ThreadPool.getInstance().execute(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void run() {
                            char6_store(data, uuid);
//                            Log.d(TAG, Thread.currentThread().getName());
                        }
                    });
//                    Log.d(TAG, "CompletedTaskCount============================:"+ThreadPool.getInstance().getCompletedTaskCount());
//                    char6_store(data, uuid);
                }
            }
        }
    };


    private static IntentFilter makeGattUpdateIntentFilter() {                        //注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BleController.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BroadCastUtil.notifyDataChanged);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static synchronized void char6_store(byte[] data, String uuid) {
        switch (uuid){
            case UUIDs.UUID_ENVIRONMENT_Char_Notify:
                deal_environment(data);
                break;
            case UUIDs.UUID_BD_Char:
                System.out.println(new String (data));
//                Log.d(TAG, new String(data));
                temp_bd_data.append(new String(data));
                Log.d(TAG, "char6_store: "+temp_bd_data);
                String info=ifHasDataNeeded(temp_bd_data);
                if(info!=null){
                    Log.d(TAG,temp_bd_data.toString());

                    getFormatLatlng(info);

                }
                else    Log.d(TAG,temp_bd_data.toString());
                break;
            case UUIDs.UUID_KEYPad:
                Log.d(TAG, "char6_store: 接受到薄膜键盘----"+new String(data));
                break;
            case UUIDs.UUID_Heart_Char_Notify:
                deal_heart(data);
                break;
            case UUIDs.UUID_Action_Char_Notify:
                BroadCastUtil.broadcastUpdate(BroadCastUtil.sensorAction, "sensorData", byteToFloatAll(data));
                String[] temp = byteToFloatAll(data).split(" ");
                float x = Float.parseFloat(temp[3]);
                float y = Float.parseFloat(temp[4]);
                float z = Float.parseFloat(temp[5]);

                ActionTable actionTable  = new ActionTable(x, y, z, System.currentTimeMillis());
                actionTable.save();
                Log.d(TAG, "接收到动作数据:"+byteToFloatAll(data));
                break;
            case UUIDs.UUID_ECG_Char_Notify:
                Log.d(TAG, "接收到心电数据:"+data.length);
                dealECG(data);
                break;
            case UUIDs.UUID_Sync_Char_Notify:
                String receiveInfo = new String(data);
                Log.d(TAG, "char6_store: 接受到收据->"+receiveInfo);
                long time = System.currentTimeMillis();
                byte[] buf1 = LongToBytes(time);
                BleManager.getInstance().write(BleManager.getInstance().getAllConnectedDevice().get(0),
                        UUIDs.UUID_Sync_Service, UUIDs.UUID_Sync_Char_Write, buf1, new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                Log.d(TAG, "3-1:");
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {

                                Log.d(TAG, "3-2");
                            }
                        });
                break;
            case UUIDs.UUID_Reliable_Char_Notify:
                short receive = byte2int(data);

                if(RECEIVED_RELIABLE_TAG_TEST==-1){
                    byte[] buf = int2byte(receive);
                    BleManager.getInstance().write(BleManager.getInstance().getAllConnectedDevice().get(0),
                            UUIDs.UUID_Reliable_Service, UUIDs.UUID_Reliable_Char_Write, buf, new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                                    Log.d(TAG, "1-1");
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
//                                    Log.d(TAG, "1-2");
                                }
                            });
                    RECEIVED_RELIABLE_TAG_TEST = receive;
                }else if(RECEIVED_RELIABLE_TAG_TEST+1==receive){
                    byte[] buf = int2byte(receive);
                    BleManager.getInstance().write(BleManager.getInstance().getAllConnectedDevice().get(0),
                            UUIDs.UUID_Reliable_Service, UUIDs.UUID_Reliable_Char_Write, buf, new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                                    Log.d(TAG, "2-1");
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
//                                    Log.d(TAG, "2-2");
                                }
                            });
                    RECEIVED_RELIABLE_TAG_TEST = receive;
                }else{
                    byte[] buf = int2byte(RECEIVED_RELIABLE_TAG_TEST);
                    BleManager.getInstance().write(BleManager.getInstance().getAllConnectedDevice().get(0),
                            UUIDs.UUID_Reliable_Service, UUIDs.UUID_Reliable_Char_Write, buf, new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                                    Log.d(TAG, "3-1:");
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
//                                    Log.d(TAG, "3-2");
                                }
                            });
                }
                Log.d(TAG, "接收到可靠测试数据->"+ Utils.bytesTo10String(data)+" TAG->"+byte2int(data));
                break;
        }
    }
    public static byte[] LongToBytes(long values) {
        byte[] buffer = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = i*8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        System.out.println("时间：");
        for (byte b : buffer) {
            System.out.print(b+" ");
        }
        System.out.println();
        return buffer;
    }
    public static short byte2int(byte[] arr){
        if(arr.length>=2){
            return (short)((arr[0]&0xff) | ((arr[1]&0xff)<<8));
        }
        return -1;
    }

    public static byte[] int2byte(short a){
        return new byte[]{(byte)(a&0xff), (byte)((a>>8)&0xff)};
    }

    private static void dealECG(byte[] data) {
        if(data.length<20){
            Log.d(TAG, "心电数据不完整");
            return ;
        }
        for(byte t:data){
            ECGLinkedList.add(new ECGModel(new Date(), t, Constant.MY_IP));
        }
        BroadCastUtil.broadcastUpdate(BroadCastUtil.drawECGAction);
    }

    /**
     * 动作传感器解析
     * @param Array
     * @return
     */
    public static String byteToFloatAll(byte[] Array)
    {
        short accum = 0, Pos = 1, i;
        float accel = 0.0f;
        String s = new String(" ");
        s += "all acc";
        for(i=0; i<3; i++) {
            accum = 0;
            accum = (short)((Array[Pos + 2*i + 1] & 0xFF)| ((Array[Pos + 2*i + 0] & 0xFF) << 8));
            accel = accum/16384.0f;
            s += " "+Float.toString(accel);
        }
        Pos += 2*3;
        s += "  gyro";
        for(i=0; i<3; i++) {
            accum = 0;
            accum = (short)((Array[Pos + 2*i + 1] & 0xFF)|((Array[Pos + 2*i + 0] & 0xFF) << 8));
            accel = accum/16.4f;
            s += " "+Float.toString(accel);
        }
        return s;
    }

    private static String ifHasDataNeeded(StringBuffer sb){
        Pattern p;
        String info = null;
        if(sb != null){
            p = Pattern.compile(BeiDouInfo_regex);
//            p = Pattern.compile("\\$GNRMC");
            Matcher matcher = p.matcher(sb.toString());
            if(matcher.find()){
                info = matcher.group();
                int end = matcher.end();
                sb.delete(0,end);
//                Log.d(TAG, "leftsb: "+sb);
            }
        }
        return info;
    }
    public static List<String> splitInfo(String info){
        List<String> temp_info = new ArrayList<>();
        String[] strArr = info.split(",");
        Pattern pattern = Pattern.compile("([0-9]\\d*\\.?\\d*)|([a-zA-Z]+)");
        for (String tempsplit : strArr){
            if (tempsplit.contains("*")){
                temp_info.addAll(Arrays.asList(tempsplit.split("\\*")));
                continue;
            }
            Matcher matcher = pattern.matcher(tempsplit);
            while (matcher.find()){
               temp_info.add(matcher.group());
            }
        }
       return temp_info;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static LatLng getFormatLatlng(String info){
        LatLng latLng = null;
        Pattern pattern = Pattern.compile(Avivable_BeiDouInfo_regex);
        Matcher matcher = pattern.matcher(info);
        String recv_info ="";
        int end = 0;
        if (matcher.find()) {
            recv_info = matcher.group();
            recv_info = recv_info.substring(1);

        }
//        String str = sb.substring(sb.lastIndexOf("$GNRMC"),sb.length())
        if (recv_info.length()<=0){
            return null;
        }
        Log.d(TAG,"receieve data:"+recv_info);

        List<String> strArr = splitInfo(recv_info);
        Log.d(TAG, "&&&  "+strArr);

        String lat="",NorS="",lng="",EorW="",Times="";

        boolean flag = false;  //标志该数据是否有效
//        Log.d(TAG, "getFormatLatlng-------: "+strArr);
        if (strArr.get(0).equals("GNRMC") && strArr.size()>7 && strArr.get(2).equals("A") &&
                (strArr.get(4).equalsIgnoreCase("N") || strArr.get(4).equalsIgnoreCase("S")) &&
                (strArr.get(6).equalsIgnoreCase("W") || strArr.get(6).equalsIgnoreCase("E"))){
            flag = true;
            lat = strArr.get(3);
            NorS = strArr.get(4);
            lng = strArr.get(5);
            EorW = strArr.get(6);
            Times = strArr.get(1);

        }
        if(strArr.get(0).equals("GNGLL") && strArr.size()>=7 && strArr.get(6).equals("A") &&
                (strArr.get(2).equalsIgnoreCase("N") || strArr.get(2).equalsIgnoreCase("S")) &&
                (strArr.get(4).equalsIgnoreCase("W") || strArr.get(4).equalsIgnoreCase("E"))){
            flag = true;
            lat = strArr.get(1);
            NorS = strArr.get(2);
            lng = strArr.get(3);
            EorW = strArr.get(4);
            Times = strArr.get(5);

        }
        if(strArr.get(0).equals("GNGGA") && strArr.size()>=7 && !strArr.get(6).equals("0") &&
                (strArr.get(3).equalsIgnoreCase("N") || strArr.get(3).equalsIgnoreCase("S")) &&
                (strArr.get(5).equalsIgnoreCase("W") || strArr.get(5).equalsIgnoreCase("E"))){
            flag = true;
            lat = strArr.get(2);
            NorS = strArr.get(3);
            lng = strArr.get(4);
            EorW = strArr.get(5);
            Times = strArr.get(1);

        }
        if (flag){
            Double lat_double = Double.parseDouble(lat.substring(0,2))+(Double.parseDouble(lat.substring(2,lat.length()))/60.0);
            Double lng_double = Double.parseDouble(lng.substring(0,3))+(Double.parseDouble(lng.substring(3,lng.length()))/60.0);
//            Log.d(TAG, "+++++++="+lat_double + "   "+lng_double);
            if(NorS.equals("S"))
                lat_double = -lat_double;
            if(EorW.equals("W"))
                lng_double = -lng_double;

            latLng = new LatLng(lat_double, lng_double);
            //坐标转换
            CoordinateConverter converter = new CoordinateConverter(MyApplication.getContext());
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(latLng);
            latLng = converter.convert();

            //存数据
            BDOperation.SaveBDInfo(latLng.longitude,latLng.latitude,Times,MyIP);

            //发送广播给地图Activity
            Intent intent=new Intent();
            intent.setAction(ACTION_BD_AVAILABLE);
            MyApplication.getContext().sendBroadcast(intent);
        }

        return latLng;
    }

    private static void deal_heart(byte[] data){
        if(data.length<2){
            System.out.println("心率数据不完整！");
            return;
        }
        if(data[1]==0x00){
            Log.d(TAG, "心率数据异常，请检查设备或佩戴人员");
            return;
        }
        String heart_str = bytes2HexString(data);
        char[] temp = heart_str.toCharArray();
        int heart_int = (temp[2]-'0')*16+(temp[3]-'0');
        if(HeartOperation.storeHeartRate(heart_int,MyIP)){
            Log.d(TAG, "心率 "+heart_int+" 保存成功");
        }

    }

    private static void deal_environment(byte[] data) {
        if(data.length<12){
            Log.d(TAG,"环境数据不完整！");
            return;
        }
        String showString="";
        if(data[1]==0){
            if(data[0]==0){
                showString+="0.0";
            }
            else{
                showString+=data[0]+".0";
            }
        }else{
            showString+=(data[0]+data[1]/10.0);
        }
        showString+="\t";
        if(data[3]==0){
            if(data[2]==0){
                showString+="0.0";
            }
            else{
                showString+=data[2]+".0";
            }
        }else{
            showString+=(data[3]/10.0+data[2]);
        }

        showString+="\t";
        if(data[5]==0){
            if(data[4]==0){
                showString+="0.0";
            }
            else{
                showString+=data[4]+".0";
            }
        }else{
            showString+=(data[5]/10.0+data[4]);
        }

        showString+="\t";
        if(data[7]==0){
            if(data[6]==0){
                showString+="0.0";
            }
            else{
                showString+=data[6]+".0";
            }
        }else{
            showString+=(data[7]/10.0+data[6]);
        }

        showString+="\t";
        if(data[9]==0){
            if(data[8]==0){
                showString+="0.0";
            }
            else{
                showString+=data[8]+".0";
            }
        }else{
            showString+=(data[9]/10.0+data[8]);
        }


        showString+="\t";
        if(data[11]==0){
            if(data[10]==0){
                showString+="0.0";
            }
            else{
                showString+=data[10]+".0";
            }
        }else{
            showString+=(data[11]/10.0+data[10]);
        }


        String str=showString;

        String[] datas = str.split("\t");
        str=""+datas.length+"  ";
        for (String temp:datas){
            str+=temp+",";
        }
        double temperature = Double.parseDouble(datas[0]);
        double pressure = Double.parseDouble(datas[1]);
        double humidity = Double.parseDouble(datas[2]);
        double SO2 = Double.parseDouble(datas[3]);
        double NO = Double.parseDouble(datas[4]);
        double voltage = Double.parseDouble(datas[5]);
        if(EnviromentTableOperation.SaveEnviromentTable(temperature,pressure,humidity,SO2,NO,voltage,MyIP)){
            Log.d(TAG, "环境数据保存成功"+str);
        }
    }
}
