package cn.hitftcl.wearablepc.NetWork;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.wearablepc.DataFusion.DataFusionUtil;
import cn.hitftcl.wearablepc.DataFusion.FusionState;
import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.EnvironmentTable;
import cn.hitftcl.wearablepc.Model.HeartTable;
import cn.hitftcl.wearablepc.Model.UserIPInfo;

/**
 * Created by Administrator on 2018/11/1.
 */

public class FusionService extends Service {
    private static final String TAG = "debug001";

    private Timer timer = null;
    private TimerTask timerTask = null;
    private static int Timer_Interval = 3000;

    public static FusionState fusionResult = null;

//    private static UserIPInfo CaptainInfo = DataSupport.where("isCaptain = ?", String.valueOf(true)).findFirst(UserIPInfo.class);

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
        Log.d(TAG, "FusionService启动了");
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                HeartTable heartTable = DataSupport.findLast(HeartTable.class);
                EnvironmentTable environmentTable = DataSupport.findLast(EnvironmentTable.class);
                BDTable bdTable = DataSupport.findLast(BDTable.class);
                fusionResult = DataFusionUtil.situation1Fusion(heartTable, environmentTable, bdTable);
            }
        };
    }

    public static FusionState getFusionResult(){
        return fusionResult;
    }

}
