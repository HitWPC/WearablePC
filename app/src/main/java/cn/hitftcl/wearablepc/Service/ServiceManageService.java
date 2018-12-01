package cn.hitftcl.wearablepc.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.clj.fastble.BleManager;

import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.wearablepc.NetWork.NetworkUtil;
import cn.hitftcl.wearablepc.ServiceManage.ServiceManageActivity;

public class ServiceManageService extends Service {
    public static final String TAG = "debug001";

    private NetworkChangedReceiver networkChangedReceiver=null;

    private Timer timer = null;
    private TimerTask timerTask = null;
    private static int Timer_Interval = 3000;

    public ServiceManageService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "服务管理程序开启了");
        networkChangedReceiver = new NetworkChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangedReceiver, intentFilter);
        timer.schedule(timerTask,0, Timer_Interval);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        if(timerTask==null){
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(BleManager.getInstance().getAllConnectedDevice().size()>0){
                        if(!ServiceManageActivity.serviceInfo.isBtReceiveService()){
                            startService(ServiceManageActivity.sensorDataService);
                            ServiceManageActivity.serviceInfo.setBtReceiveService(true);
                            Log.d(TAG,"传感器可用，已开启蓝牙接收服务");
                        }
                        if(!ServiceManageActivity.serviceInfo.isDataFusionService()){
                            startService(ServiceManageActivity.fusionService);
                            ServiceManageActivity.serviceInfo.setDataFusionService(true);
                            Log.d(TAG,"传感器可用，已开启数据融合服务");
                        }
                        if(!ServiceManageActivity.serviceInfo.isDataSendService()){
                            startService(ServiceManageActivity.sendDataService);
                            ServiceManageActivity.serviceInfo.setDataSendService(true);
                            Log.d(TAG,"传感器可用，已开启数据上报服务");
                        }
                    }else{
                        if(ServiceManageActivity.serviceInfo.isBtReceiveService()){
                            stopService(ServiceManageActivity.sensorDataService);
                            ServiceManageActivity.serviceInfo.setBtReceiveService(false);
                            Log.d(TAG,"传感器不可用，已关闭蓝牙接收服务");
                        }
                        if(ServiceManageActivity.serviceInfo.isDataFusionService()){
                            stopService(ServiceManageActivity.fusionService);
                            ServiceManageActivity.serviceInfo.setDataFusionService(false);
                            Log.d(TAG,"传感器不可用，已关闭数据融合服务");
                        }
                        if(ServiceManageActivity.serviceInfo.isDataSendService()){
                            stopService(ServiceManageActivity.sendDataService);
                            ServiceManageActivity.serviceInfo.setDataSendService(false);
                            Log.d(TAG,"传感器不可用，已关闭数据上报服务");
                        }
                    }
                }
            };
        }

    }


    public class NetworkChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int netWorkStates = NetworkUtil.getNetWorkStates(context);

            switch (netWorkStates) {
                case NetworkUtil.TYPE_NONE:
                    //断网了
                    if(ServiceManageActivity.serviceInfo.isNetService()){
                        stopService(ServiceManageActivity.netReceiveService);
                        ServiceManageActivity.serviceInfo.setNetService(false);
                        Log.d(TAG,"网络不可用，已关闭网络通信服务");
                    }
                    break;
                case NetworkUtil.TYPE_MOBILE:
                    //打开了移动网络
                case NetworkUtil.TYPE_WIFI:
                    //打开了WIFI
                    if(!ServiceManageActivity.serviceInfo.isNetService()){
                        if(ServiceManageActivity.netReceiveService==null){
                            ServiceManageActivity.netReceiveService = new Intent(getApplicationContext(), ReceiveService.class);
                        }
                        startService(ServiceManageActivity.netReceiveService);
                        ServiceManageActivity.serviceInfo.setNetService(true);
                        Log.d(TAG, "网络可用,已开启网络通信服务");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(networkChangedReceiver);
        Log.d(TAG, "服务管理程序关闭了");
        timer.cancel();
        if(timerTask!=null){
            timerTask.cancel();
            timerTask = null;
        }

        super.onDestroy();
    }
}
