package cn.hitftcl.wearablepc.Service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.wearablepc.BDMap.BD_Partner_Singleton;
import cn.hitftcl.wearablepc.DataFusion.FusionState;
import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.NetWork.NetworkUtil;
import cn.hitftcl.wearablepc.NetWork.TransType;
import cn.hitftcl.wearablepc.Utils.Constant;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

public class SendDataService extends Service {
    private static final String TAG = "debug001";

    public static Timer timer = null;
    public  static TimerTask timerTask = null;
    private  static int Max_Interval_Seconds = 2000;
    private static int Timer_Interval = 1000;

    public static int SLEEP_TIME = 1000;

    private SendDataThread sendDataThread= null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        timer.schedule(timerTask,0, Timer_Interval);
        if(sendDataThread==null){
            sendDataThread = new SendDataThread();
            sendDataThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        timer = new Timer();
//        timerTask = new SendDataTask();
    }

    class SendDataThread extends Thread{
        @Override
        public void run() {
            while(true){
//                System.out.println("发送数据服务开始运行");
//                Log.d(TAG, "android:layout_below=\"@+id/synBtn\" "+ Constant.dateFormat.format(new Date()));
                if (Thread.currentThread().isInterrupted()){
                    break;
                }

                UserIPInfo CaptainInfo = null;
                CaptainInfo = DataSupport.where("isCaptain = ?", String.valueOf(1)).findFirst(UserIPInfo.class);
//                System.out.println("队长*********"+(CaptainInfo!=null && CaptainInfo.getType()==0));
                if(CaptainInfo!=null && CaptainInfo.getType()!=0){  //TODO 我不是队长……
                    //TODO 向队长发送地理位置数据
                    String BD_Data_Json = LatestBDdata();
                    if (BD_Data_Json!=null){
                        NetworkUtil.sendByTCP(CaptainInfo.getIp(),CaptainInfo.getPort(), TransType.BD_TYPE,BD_Data_Json);
                    }
                    //TODO 向队长发送体征环境融合数据
                    FusionState fusionState = LatestFusionResult();
                    if(fusionState!=null && (fusionState.heartAvailable||fusionState.envAvailable||fusionState.bdAvailable)){
                        String fusionStr = new Gson().toJson(fusionState);
                        if(NetworkUtil.sendByTCP(CaptainInfo.getIp(),CaptainInfo.getPort(),TransType.FUSION_RES,fusionStr))
                            Log.d(TAG, "向队长发送融合数据成功");
                    }
                }else if(CaptainInfo!=null && CaptainInfo.getType()==0){  //TODO 我是队长……
                    //TODO 给队员发送所有成员地理位置  + 包括指挥端
                    final List<UserIPInfo> userIPInfos = DataSupport.where("type!=?","0").find(UserIPInfo.class);
                    ArrayList<BDTable> bdList = BD_Partner_Singleton.getInstance().getBDArrayList();
                    BDTable cap = DataSupport.findLast(BDTable.class);
                    if(cap!=null){
                        Log.d(TAG,  "添加自己位置信息");
                        bdList.add(cap);
                    }
                    final String bdJson = new Gson().toJson(bdList);
    //                Log.d(TAG, "Send bdJson:"+bdJson);
                    ThreadPool.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            for (UserIPInfo userIPInfo : userIPInfos){
                                NetworkUtil.sendByTCP(userIPInfo.getIp(), userIPInfo.getPort(), TransType.BD_TYPES, bdJson);
                            }

                        }
                    });

                    //TODO 给指挥端发送队员融合数据

                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 获取最新的数据融合结果
     * @return
     */
    private FusionState LatestFusionResult() {
        FusionState state = FusionService.getFusionResult();
        if(state!=null && new Date().getTime()-state.getFusionTime().getTime()<5000)
            return state;
        return null;
    }

    public String LatestBDdata (){
        BDTable data = DataSupport.findLast(BDTable.class);
        if(data==null)
            return null;
        Date data_time = data.getRecordDate();
        Date current = new Date();
        if (current.getTime() - data_time.getTime()<= Max_Interval_Seconds){
            Gson gson = new Gson();
            return gson.toJson(data);
        }
        return null;
    }

//
//    /**
//     * 获取最新的数据融合结果
//     * @return
//     */
//    private FusionState LatestFusionResult() {
//        FusionState state = FusionService.getFusionResult();
//        if(state!=null && new Date().getTime()-state.getFusionTime().getTime()<5000)
//            return state;
//        return null;
//    }
//
//    public String LatestBDdata (){
//        BDTable data = DataSupport.findLast(BDTable.class);
//        if(data==null)
//            return null;f
//        Date data_time = data.getRecordDate();
//        Date current = new Date();
//        if (current.getTime() - data_time.getTime()<= Max_Interval_Seconds){
//            Gson gson = new Gson();
//            return gson.toJson(data);
//        }
//        return null;
//    }

    public String LatestHeartdata (){
        ArrayList<BDTable> list = new ArrayList<>();
        BDTable data = DataSupport.findLast(BDTable.class);
        Date data_time = data.getRecordDate();
        Date current = new Date();
        if (current.getTime() - data_time.getTime()<= 10000){
            Gson gson = new Gson();
            return gson.toJson(list.add(data));
        }
        return null;
    }

    @Override
    public void onDestroy() {
        sendDataThread.interrupt();
        super.onDestroy();
    }
}
