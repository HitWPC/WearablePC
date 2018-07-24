package cn.hitftcl.wearablepc.NetWork;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.UserIPInfo;

public class SendDataService extends Service {
    private static final String TAG = "debug001";

    private Timer timer = null;
    private TimerTask timerTask = null;
    private  static int Max_Interval_Seconds = 2000;
    private static int Timer_Interval = 1000;

    private static UserIPInfo CaptainInfo = DataSupport.where("isCaptain = ?", String.valueOf(true)).findFirst(UserIPInfo.class);

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
                String BD_Data_Json = LatestBDdata();
                if (BD_Data_Json!=null){
                    NetworkUtil.sendByTCP(CaptainInfo.getIp(),CaptainInfo.getPort(),TransType.BD_TYPE,BD_Data_Json);
                }
            }
        };
    }

    public String LatestBDdata (){
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
}
