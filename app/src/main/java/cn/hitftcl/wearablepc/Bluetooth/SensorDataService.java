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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.UUIDs;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

public class SensorDataService extends Service {
    public final static  String TAG = "debug001";
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
                Log.d(TAG, new String(data));
                break;
        }



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
