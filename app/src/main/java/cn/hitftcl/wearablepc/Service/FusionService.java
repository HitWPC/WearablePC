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

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.UUIDs;
import cn.hitftcl.ble.callback.OnWriteCallback;
import cn.hitftcl.wearablepc.BDMap.MapActivity;
import cn.hitftcl.wearablepc.DataFusion.DataFusionUtil;
import cn.hitftcl.wearablepc.DataFusion.FusionState;
import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.EnvironmentTable;
import cn.hitftcl.wearablepc.Model.HeartTable;
import cn.hitftcl.wearablepc.R;

/**
 * Created by Administrator on 2018/11/1.
 */

public class FusionService extends Service {
    private static final String TAG = "debug001";

    private BleController bleController;

    private Timer timer = null;
    private TimerTask timerTask = null;
    private static int Timer_Interval = 3000;

    public static FusionState fusionResult = null;

    public static int ENV_SPEED_DEFAULT = 1;  //  1次/1秒
    public static int ENV_SPEED_MAX = 16;
    public static int ENV_SPEED_CURRENT = 1;

    public static int lastNotifyLevel = 0;

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
        bleController = BleController.getInstance().init(this);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                HeartTable heartTable = DataSupport.findLast(HeartTable.class);
                EnvironmentTable environmentTable = DataSupport.findLast(EnvironmentTable.class);
                BDTable bdTable = DataSupport.findLast(BDTable.class);
                fusionResult = DataFusionUtil.situation1Fusion(heartTable, environmentTable, bdTable);
                judgeAndShowNotification();
                speedChange();
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    private void judgeAndShowNotification() {
        int tempLevel = -1;
        StringBuilder sb = new StringBuilder();
        if(fusionResult!=null && fusionResult.envAvailable){
            if(fusionResult.getSo2()==3 || fusionResult.getNo()==3){
                tempLevel = Math.max(tempLevel, 3);
                if(fusionResult.getSo2()==3){
                    sb.append("SO2浓度过高，请尽快撤离！");
                }
                if(fusionResult.getNo()==3){
                    sb.append("NO浓度过高，请尽快撤离！");
                }
            }else if(fusionResult.getSo2()==2 || fusionResult.getNo()==2 ){
                tempLevel = Math.max(tempLevel, 2);
                if(fusionResult.getSo2()==3){
                    sb.append("SO2浓度偏高，请注意防护！");
                }
                if(fusionResult.getNo()==3){
                    sb.append("NO浓度偏高，请注意防护！");
                }
            }else if(fusionResult.getPressure()==4 || fusionResult.getPressure()==0 || fusionResult.getTemperature()==0 || fusionResult.getTemperature()==4 || fusionResult.getHumidity()==4){
                tempLevel = Math.max(tempLevel, 1);
                sb.append("温湿度气压异常，请注意！");
            }
        }
        if(fusionResult!=null && fusionResult.heartAvailable){
            if(fusionResult.getHeartState()==0){
                tempLevel = Math.max(tempLevel, 2);
                sb.append("心率过低，请注意！");
            }else if(fusionResult.getHeartState()==4){
                tempLevel = Math.max(tempLevel, 1);
                sb.append("心率过高，请注意！");
            }
        }
        if(tempLevel!=-1 && tempLevel!=lastNotifyLevel){
            lastNotifyLevel = tempLevel;
            showNotification(tempLevel, "体征/环境 异常", sb.toString());
        }
    }

    private void showNotification(int level, String title, String content) {
        Intent intent = new Intent(this, MapActivity.class);
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

    private void sendChangeInfo_ENV(String str, int speed) {
        Log.d(TAG, str+"修改的速率为："+speed);
        final byte[] buf = new byte[1];
        switch (speed){
            case 1:
                buf[0] =0x01;
                break;
            case 2:
                buf[0]  = 0x02;
                break;
            case 3:
                buf[0] = 0x03;
                break;
            case 4:
                buf[0] = 0x04;
                break;
            case 5:
                buf[0] = 0x05;
                break;
            case 6:
                buf[0] = 0x06;
                break;
            case 7:
                buf[0] = 0x07;
                break;
            case 8:
                buf[0] = 0x08;
                break;
            case 9:
                buf[0] = 0x09;
                break;
            case 10:
                buf[0] = 0x0a;
                break;
            case 11:
                buf[0] = 0x0b;
                break;
            case 12:
                buf[0] = 0x0c;
                break;
            case 13:
                buf[0] = 0x0d;
                break;
            case 14:
                buf[0] = 0x0e;
                break;
            case 15:
                buf[0] = 0x0f;
                break;
            case 16:
                buf[0] = 0x10;
                break;
            default:
                buf[0] = 0x01;
                break;
        }
        HashMap<BluetoothDevice, BluetoothGatt> map = bleController.getConnectedDvices();
        BluetoothDevice device = null;
        for(Map.Entry<BluetoothDevice, BluetoothGatt> entry:map.entrySet()){
            BluetoothDevice temp = entry.getKey();
            if(temp.getName().contains("BEAN")){//是环境传感器
                device = temp;
                break;
            }
        }
        if(device!=null){
            bleController.writeBuffer_Device(device, UUIDs.UUID_ENVIRONMENT_Service, UUIDs.UUID_ENVIRONMENT_Char_Write,buf, new OnWriteCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "修改速率="+buf[0]+"成功");
                }

                @Override
                public void onFailed(int state) {
                    Log.d(TAG, "修改速率失败");
                }
            });
        }
    }

    public static FusionState getFusionResult(){
        return fusionResult;
    }

}
