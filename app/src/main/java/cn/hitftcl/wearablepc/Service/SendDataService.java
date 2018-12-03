package cn.hitftcl.wearablepc.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.wearablepc.DataFusion.FusionState;
import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.NetWork.NetworkUtil;
import cn.hitftcl.wearablepc.NetWork.TransType;

public class SendDataService extends Service {
    private static final String TAG = "debug001";

    private Timer timer = null;
    private TimerTask timerTask = null;
    private  static int Max_Interval_Seconds = 2000;
    private static int Timer_Interval = 1000;

    private UserIPInfo CaptainInfo = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer.schedule(timerTask,0, Timer_Interval);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "数据融合123");
                CaptainInfo = DataSupport.where("isCaptain = ?", String.valueOf(1)).findFirst(UserIPInfo.class);
                //向队长发送地理位置数据
                String BD_Data_Json = LatestBDdata();
                if (BD_Data_Json!=null && CaptainInfo!=null){
                    NetworkUtil.sendByTCP(CaptainInfo.getIp(),CaptainInfo.getPort(), TransType.BD_TYPE,BD_Data_Json);
                }
                //向队长发送体征环境融合数据
                FusionState fusionState = LatestFusionResult();
                if(fusionState!=null && CaptainInfo!=null && CaptainInfo.getType()!=0 && (fusionState.heartAvailable||fusionState.envAvailable||fusionState.bdAvailable)){
                    String fusionStr = new Gson().toJson(fusionState);
                    NetworkUtil.sendByTCP(CaptainInfo.getIp(),CaptainInfo.getPort(),TransType.FUSION_RES,fusionStr);
                    Log.d(TAG, "向队长发送融合数据成功");
                }
            }
        };
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
        ArrayList<BDTable> list = new ArrayList<>();
        BDTable data = DataSupport.findLast(BDTable.class);
        if(data==null)
            return null;
        Date data_time = data.getRecordDate();
        Date current = new Date();
        if (current.getTime() - data_time.getTime()<= Max_Interval_Seconds){
            Gson gson = new Gson();
            return gson.toJson(list.add(data));
        }
        return null;
    }

    public String LatestHeartdata (){
        ArrayList<BDTable> list = new ArrayList<>();
        BDTable data = DataSupport.findLast(BDTable.class);
        Date data_time = data.getRecordDate();
        Date current = new Date();
        if (current.getTime() - data_time.getTime()<= Max_Interval_Seconds){
            Gson gson = new Gson();
            return gson.toJson(list.add(data));
        }
        return null;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
