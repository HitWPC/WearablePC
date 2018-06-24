package cn.hitftcl.wearablepc.Bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ScrollView;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.UUIDs;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

public class SensorDataService extends Service {
    public final static  String TAG = "debug001";

    public static StringBuilder temp_bd_data = new StringBuilder();
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
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleController.ACTION_DATA_AVAILABLE.equals(action)) {     //收到数据
                final byte[]  data = intent.getByteArrayExtra(BleController.EXTRA_DATA);
                final String uuid = intent.getStringExtra(BleController.EXTRA_UUID);
                if (data != null) {
                    ThreadPool.getInstance().execute(new Runnable() {
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
        intentFilter.addAction(BleController.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    public static synchronized void char6_store(byte[] data, String uuid) {
        switch (uuid){
            case UUIDs.UUID_ENVIRONMENT:
                deal_environment(data);
                break;
            case UUIDs.UUID_BD:
//                Log.d(TAG, new String(data));
                temp_bd_data.append(new String(data));
                if(ifHasDataNeeded(temp_bd_data)){
                    Log.d(TAG,temp_bd_data.toString());
//                    getFormatLatlng(temp_bd_data);
                    temp_bd_data.delete(0, temp_bd_data.length());
                }
                break;
        }
    }

    private static boolean ifHasDataNeeded(StringBuilder sb){
        Pattern p;
        if(sb != null){
//            p = Pattern.compile("\\$GNRMC,.+,[A|V],.+,[N|S],.+,[W|E],.*");
            p = Pattern.compile("\\$GNRMC");
            if(p.matcher(sb.toString()).find())
                return true;
            return false;
        }
        return false;
    }

    private static LatLng getFormatLatlng(StringBuilder sb){
        LatLng latLng;
        String str = sb.substring(sb.lastIndexOf("$GNRMC"),sb.length());
        Log.d(TAG,sb.substring(sb.lastIndexOf("$GNRMC"),sb.length()));
        String[] strArr = str.split(",");
        String lat = strArr[3];
        String NorS = strArr[4];
        String lng = strArr[5];
        String EorW = strArr[6];
        double lat_double = Double.parseDouble(lat.substring(0,2))+(Double.parseDouble(lat.substring(2,lat.length()))/60.0);
        double lng_double = Double.parseDouble(lng.substring(0,3))+(Double.parseDouble(lng.substring(3,lng.length()))/60.0);
        Log.d(TAG, ""+lat_double + "   "+lng_double);
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
//        locateAndMark(latLng);
        return latLng;
    }

    private static void deal_environment(byte[] data) {
        String showString="\n ";
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

        showString+="\t\t\t\t\t";
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

        showString+="\t\t\t\t\t";
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

        showString+="\t\t\t\t\t\t";
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

        showString+="\t\t\t\t\t\t";
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


        showString+="\t\t\t\t\t\t";
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
        Log.d(TAG, str);
    }
}
