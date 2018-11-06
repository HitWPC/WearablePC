package cn.hitftcl.wearablepc.NetWork;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.UUIDs;
import cn.hitftcl.ble.callback.OnWriteCallback;
import cn.hitftcl.wearablepc.DataFusion.DataFusionUtil;
import cn.hitftcl.wearablepc.DataFusion.FusionState;
import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.EnvironmentTable;
import cn.hitftcl.wearablepc.Model.HeartTable;

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
        bleController = BleController.getInstance().init(this);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                HeartTable heartTable = DataSupport.findLast(HeartTable.class);
                EnvironmentTable environmentTable = DataSupport.findLast(EnvironmentTable.class);
                BDTable bdTable = DataSupport.findLast(BDTable.class);
                fusionResult = DataFusionUtil.situation1Fusion(heartTable, environmentTable, bdTable);
                speedChange();
            }
        };
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
