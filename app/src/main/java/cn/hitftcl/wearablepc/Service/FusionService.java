package cn.hitftcl.wearablepc.Service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.UUIDs;
import cn.hitftcl.ble.callback.OnWriteCallback;
import cn.hitftcl.wearablepc.ActionRecognition.model.FeaVector;
import cn.hitftcl.wearablepc.BDMap.MapActivity;
import cn.hitftcl.wearablepc.DataFusion.DataFusionUtil;
import cn.hitftcl.wearablepc.DataFusion.FusionActivity;
import cn.hitftcl.wearablepc.DataFusion.FusionState;
import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.EnvironmentTable;
import cn.hitftcl.wearablepc.Model.HeartTable;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.ServiceManage.ServiceManageActivity;

/**
 * Created by Administrator on 2018/11/1.
 */

public class FusionService extends Service {
    private static final String TAG = "debug001";

    private BleController bleController;

    private Timer timer = null;
    private TimerTask timerTask = null;
    private static int Timer_Interval = 3000;

    private static int CurrentSendDataInterval = 1000;
    private static int SendDataMaxInterval = 12000;

    public static FusionState fusionResult = null;

    public static int ENV_SPEED_DEFAULT = 1;  //  1次/1秒
    public static int ENV_SPEED_MAX = 16;
    public static int ENV_SPEED_CURRENT = 1;

    public static int lastNotifyLevel = 0;
    public static String lastNotifyString = "";

//    private static UserIPInfo CaptainInfo = DataSupport.where("isCaptain = ?", String.valueOf(true)).findFirst(UserIPInfo.class);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(timer==null){
            timer = new Timer();
        }
        if(timerTask==null){
            timerTask = new MyTimerTask();
        }
        timer.schedule(timerTask,0, Timer_Interval);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bleController = BleController.getInstance().init(this);
        timer = new Timer();
        timerTask = new MyTimerTask();
    }

    class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            HeartTable heartTable = DataSupport.findLast(HeartTable.class);
            EnvironmentTable environmentTable = DataSupport.findLast(EnvironmentTable.class);
            BDTable bdTable = DataSupport.findLast(BDTable.class);
            List<FeaVector> feaVectors = DataSupport.select("*").where("startTime>?",""+(System.currentTimeMillis()-20000)).order("startTime desc").limit(10).find(FeaVector.class);
            fusionResult = DataFusionUtil.situation1Fusion(heartTable, environmentTable, bdTable, feaVectors);

            judgeAndShowNotification();
            speedChange();
            sendDataServiceRateChange();  //上报服务速率改变

        }
    }

    /**
     * 修改态势上报服务的速率
     */
    private void sendDataServiceRateChange() {
        if(fusionResult!=null && ServiceManageActivity.serviceInfo.isDataSendService() &&(fusionResult.envAvailable || fusionResult.heartAvailable)){  //存在数据可用
            if((fusionResult.envAvailable && !fusionResult.isEnvNormal())
                  ||  (fusionResult.heartAvailable && !fusionResult.isBodyNormal())){   //存在异常
                SendDataService.SLEEP_TIME = 1000;
            }else{   //完全正常
                if(SendDataService.SLEEP_TIME + 1000<=SendDataMaxInterval){
                    SendDataService.SLEEP_TIME += 1000;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if(timerTask!=null){
            timerTask.cancel();
            timerTask = null;
        }
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    private void judgeAndShowNotification() {
        int tempLevel = -1;
        boolean available = false;
        StringBuilder sb = new StringBuilder();
//        Log.d(TAG, "   "+fusionResult.envAvailable);
        if(fusionResult!=null && fusionResult.envAvailable){
            available = true;
            if(fusionResult.getSo2()==3 || fusionResult.getNo()==3){
                tempLevel = Math.max(tempLevel, 3);
                if(fusionResult.getSo2()==3){
                    sb.append("SO2浓度过高，请尽快撤离！");
                }
                if(fusionResult.getNo()==3){
                    sb.append("NO浓度过高，请尽快撤离！");
                }
            }if(fusionResult.getSo2()==2 || fusionResult.getNo()==2 ){
                tempLevel = Math.max(tempLevel, 2);
                if(fusionResult.getSo2()==2){
                    sb.append("SO2浓度偏高，请注意防护！");
                }
                if(fusionResult.getNo()==2){
                    sb.append("NO浓度偏高，请注意防护！");
                }
            }if(fusionResult.getPressure()==4 || fusionResult.getPressure()==0 || fusionResult.getTemperature()==0 || fusionResult.getTemperature()==4 || fusionResult.getHumidity()==4){
                tempLevel = Math.max(tempLevel, 1);
                sb.append("温湿度或气压异常，请注意！");
            }
        }
        if(available && fusionResult.isEnvNormal()){
            DataSupport.deleteAll(EnvironmentTable.class);
        }
        if(fusionResult!=null && fusionResult.heartAvailable){
            available = true;
            if(fusionResult.getHeartState()==0){
                tempLevel = Math.max(tempLevel, 2);
                sb.append("心率过低，请注意！");
            }else if(fusionResult.getHeartState()==4){
                tempLevel = Math.max(tempLevel, 3);
                sb.append("心率过高，请注意！");
            }
        }
        if(available && tempLevel!=-1 && (tempLevel!=lastNotifyLevel||!lastNotifyString.equals(sb.toString()))){
            lastNotifyLevel = tempLevel;
            lastNotifyString = sb.toString();
            showNotification(tempLevel, "体征/环境 异常", lastNotifyString);
        }else if(available && tempLevel == -1 && lastNotifyLevel!=tempLevel){
            lastNotifyLevel = tempLevel;
            lastNotifyString = "体征/环境 正常";
            showNotification(0, "数据融合结果", lastNotifyString);
        }
    }

    private void showNotification(int level, String title, String content) {
            Intent intent = new Intent(this, FusionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager notificationManager = (NotificationManager) getSystemService
                (NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.danger)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND);
        switch (level){
            case 0: //正常
                builder.setSmallIcon(R.drawable.healty);
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_zero));
                break;
            case 1: //较严重
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_one));
                break;
            case 2:
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_two));
                break;
            case 3:
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_three));
                break;
            default:
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_one));
                break;
        }
        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }

    private void speedChange() {
        if(fusionResult!=null && fusionResult.envAvailable){
            if(fusionResult.isEnvNormal()){  //环境无异常
                if(ENV_SPEED_CURRENT < ENV_SPEED_MAX){
                    ENV_SPEED_CURRENT+=2;   //每次检测正常速率减慢2S
                    if(ENV_SPEED_CURRENT > ENV_SPEED_MAX)
                        ENV_SPEED_CURRENT = ENV_SPEED_MAX;
                    //修改速率
                    sendChangeInfo_ENV("正常",ENV_SPEED_CURRENT);
                }
            }else{                 //环境异常
                ENV_SPEED_CURRENT = ENV_SPEED_DEFAULT;
                //修改速率
                sendChangeInfo_ENV("异常",ENV_SPEED_CURRENT);
            }
        }
    }

    private void sendChangeInfo_ENV(final String str, final int speed) {
        List<BleDevice> devices = BleManager.getInstance().getAllConnectedDevice();
        BleDevice device = null;
        for(BleDevice device1 : devices){
            if(device1.getName().contains("BEAN")){//是环境传感器
                device = device1;
                break;
            }
        }
        if(device!=null){
            byte[] buf = {Byte.valueOf(speed+"", 10)};
            BleManager.getInstance().write(device, com.clj.fastble.UUIDs.UUID_ENVIRONMENT_Service, com.clj.fastble.UUIDs.UUID_ENVIRONMENT_Char_Write, buf,
                    new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            Log.d(TAG, str+"修改速率成功-->"+speed);
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            Log.d(TAG, str+"修改速率成功-->"+speed);
                        }
                    });

        }
    }

    public static FusionState getFusionResult(){
        return fusionResult;
    }

}
