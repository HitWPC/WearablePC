package cn.hitftcl.wearablepc.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;

import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;


import java.util.ArrayList;

import cn.hitftcl.wearablepc.ActionRecognition.common.FileOperateUtil;
import cn.hitftcl.wearablepc.ActionRecognition.common.Preprocess;
import cn.hitftcl.wearablepc.ActionRecognition.common.RecognitionType;
import cn.hitftcl.wearablepc.ActionRecognition.classifier.SVM;
import cn.hitftcl.wearablepc.ActionRecognition.feature.FeatureCore;
import cn.hitftcl.wearablepc.Utils.BroadCastUtil;

/**
 * 固定采集频率50Hz
 */
public class ActionOriginService extends Service{
    public static final String TAG = "debug001";


    public static final int ORIGIN_ID = 1;

    private SensorManager mSensorManager;

    private Sensor mSensorAcc;

    //一个时间窗口内加速度数据以及时间
    private ArrayList<Double> accX = new ArrayList<>();
    private ArrayList<Double> accY = new ArrayList<>();
    private ArrayList<Double> accZ = new ArrayList<>();
    private ArrayList<Long> timeList = new ArrayList<>();

    //正常采样率
    public static final int SAMPLE_FREQ = 60;
    public static final int SAMPLE_DELAY = 20000;

    //2秒采集到的数据量
    public int windowLength = 2 * SAMPLE_FREQ;

    //Service工作次数
    public int peakCount = 60;

    //计数器
    private int count = 0;

    private boolean isDestroyed = false;
    private PowerManager.WakeLock mWakeLock;

    private MyBroadcastReceive mReceiver;

    //最近的一条数据ID
    private int lastIndex;

    private MediaPlayer mp;

    public ActionOriginService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.d(ActionOriginService.class.getSimpleName(), "onDestroy");


        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }

        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadCastUtil.sensorAction);
        mReceiver = new MyBroadcastReceive();
        registerReceiver(mReceiver, filter);


        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 清空所有List数据
     */
    private void clearAllList(){
        accX.clear();
        accY.clear();
        accZ.clear();
        timeList.clear();
    }

    public class MyBroadcastReceive extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case BroadCastUtil.sensorAction:
                    String content = intent.getStringExtra("sensorData");
                    Log.d(TAG, content);
                    ActionData value = parseContent(content);
                    recg(value);
                    break;
                default:
                    break;
            }
        }
    }

    private ActionData parseContent(String content) {
        String[] temp = content.split(" ");
        for (String s : temp) {
            Log.d(TAG,"temp->"+s);
        }

        float x = Float.parseFloat(temp[3]);
        float y = Float.parseFloat(temp[4]);
        float z = Float.parseFloat(temp[5]);
        return new ActionData(x,y,z);
    }

    public void recg(ActionData actionData){
        float[] values = actionData.getAcc();
        //当前时间
        long time = System.currentTimeMillis();
        //一次活动识别的运行时间 = peakCount * 2
        if(count >= peakCount){

            final int lastIdBackup = lastIndex;
            SVM.exec.execute(new Runnable() {
                @Override
                public void run() {
                    FileOperateUtil.saveMemoryAndTime(lastIdBackup, RecognitionType.ALGORITHM_ORIGIN);
                }
            });
            count = 0;
        }else {
            if (accX.size() == windowLength) {
                Log.d(ActionOriginService.class.getSimpleName(), "startTime = " + timeList.get(0) + ",endTime = " + timeList.get(timeList.size() - 1));
                lastIndex = FeatureCore.calculateFeature(true, RecognitionType.ALGORITHM_ORIGIN,
                        Preprocess.medFilt(accX), Preprocess.medFilt(accY), Preprocess.medFilt(accZ), timeList.get(0), timeList.get(timeList.size() - 1));

                clearAllList();
                count++;
                Log.d(ActionOriginService.class.getSimpleName(), "count = " + count);
            } else {
                accX.add((double) values[0]);
                accY.add((double) values[1]);
                accZ.add((double) values[2]);
                timeList.add(time);

            }
        }
    }

    class ActionData{
        private float[] acc;
        private float[] geo;
        public ActionData(float x, float y, float z){
            acc = new float[3];
            acc[0] = x;
            acc[1] = y;
            acc[2] = z;
        }
        public float[]  getAcc(){
            return acc;
        }

        public float[] getGeo() {
            return geo;
        }
    }
}

