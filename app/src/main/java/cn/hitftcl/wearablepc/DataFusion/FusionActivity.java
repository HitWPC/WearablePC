package cn.hitftcl.wearablepc.DataFusion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.hitftcl.wearablepc.BDMap.MapActivity;
import cn.hitftcl.wearablepc.NetWork.FusionService;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

public class FusionActivity extends AppCompatActivity {

    FusionState fusionState = null;

//    Button shownoti = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fusion);

        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_secret);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("数据融合结果");


//        shownoti = findViewById(R.id.showniti);
//        shownoti.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showNotification(2, "我是标题","我是内容");
//            }
//        });

        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    fusionState = FusionService.getFusionResult();
                    if(fusionState!=null){
                        parseFusionRes(fusionState);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

//    public void showNotification(int level, String title, String content) {
//        Intent intent = new Intent(this, MapActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService
//                (NOTIFICATION_SERVICE);
//        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
//
//            // Configure the notification channel.
//            notificationChannel.setDescription("Channel description");
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
//            notificationChannel.enableVibration(true);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(content)
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.drawable.danger)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_SOUND);
//        switch (level){
//            case 1: //较严重
//                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_one));
//                break;
//            case 2:
//                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_two));
//                break;
//            case 3:
//                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_three));
//                break;
//            default:
//                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.level_one));
//                break;
//        }
//        Notification notification = builder.build();
//        notificationManager.notify(1, notification);
//    }


    private void parseFusionRes(FusionState fusionState) {
        if(fusionState.heartAvailable){
            if(fusionState.isBodyNormal()){
                ((TextView)findViewById(R.id.body)).setText("正常");
            }else{
                ((TextView)findViewById(R.id.body)).setText("异常");
            }
            switch(fusionState.getHeartState()){
                case 0:
                    ((TextView)findViewById(R.id.heart)).setText("偏低");
                    break;
                case 1:
                    ((TextView)findViewById(R.id.heart)).setText("正常偏低");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.heart)).setText("正常");
                    break;
                case 3:
                    ((TextView)findViewById(R.id.heart)).setText("正常偏高");
                    break;
                case 4:
                    ((TextView)findViewById(R.id.heart)).setText("偏高");
                    break;
            }
        }

        if(fusionState.envAvailable){
            if(fusionState.isEnvNormal()){
                ((TextView)findViewById(R.id.environment)).setText("正常");
            }else{
                ((TextView)findViewById(R.id.environment)).setText("异常");
            }

            switch(fusionState.getTemperature()){
                case 0:
                    ((TextView)findViewById(R.id.temperature)).setText("偏低");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.temperature)).setText("正常");
                    break;
                case 4:
                    ((TextView)findViewById(R.id.temperature)).setText("偏高");
                    break;
            }
            switch(fusionState.getHumidity()){
                case 0:
                    ((TextView)findViewById(R.id.humidity)).setText("偏低");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.humidity)).setText("正常");
                    break;
                case 4:
                    ((TextView)findViewById(R.id.humidity)).setText("偏高");
                    break;
            }
            switch(fusionState.getPressure()){
                case 0:
                    ((TextView)findViewById(R.id.pressure)).setText("偏低");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.pressure)).setText("正常");
                    break;
                case 4:
                    ((TextView)findViewById(R.id.pressure)).setText("偏高");
                    break;
            }
            switch(fusionState.getNo()){
                case 0:
                    ((TextView)findViewById(R.id.no)).setText("正常");
                    break;
                case 1:
                    ((TextView)findViewById(R.id.no)).setText("正常偏高");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.no)).setText("偏高，对喉部刺激较大，请注意防护");
                    break;
                case 3:
                    ((TextView)findViewById(R.id.no)).setText("过高，短时间暴露容易引起死亡，请尽快撤离");
                    break;
            }
            switch(fusionState.getSo2()){
                case 0:
                    ((TextView)findViewById(R.id.so2)).setText("正常");
                    break;
                case 1:
                    ((TextView)findViewById(R.id.so2)).setText("正常偏高");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.so2)).setText("偏高，对眼睛、鼻子、咽喉有刺激，请注意防护");
                    break;
                case 3:
                    ((TextView)findViewById(R.id.so2)).setText("过高，长时间暴露可能有生命危险，请尽快撤离");
                    break;
            }
        }
        if(fusionState.bdAvailable){
            ((TextView)findViewById(R.id.position)).setText("纬度:"+fusionState.getBD_Position().latitude+" ,经度:"+fusionState.getBD_Position().longitude);
        }
    }

    /**
     * toolbar返回按钮响应事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
