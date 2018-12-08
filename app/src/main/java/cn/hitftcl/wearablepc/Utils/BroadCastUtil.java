package cn.hitftcl.wearablepc.Utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;

import cn.hitftcl.wearablepc.MyApplication;

/**
 * Created by Administrator on 2018/11/29.
 */

public class BroadCastUtil {

    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_UUID =
            "com.example.bluetooth.le.EXTRA_UUID";

    /**
     * 动作传感器相关
     */
    public static final String onlineBroadcast = "BROADCAST.ONLINEACK.ACTION";

    /**
     * 动作传感器相关
     */
    public static final String sensorAction = "BROADCAST.SENSOR.ACTION";

    /**
     *蓝牙设备连接改变
     */
    public static final String btDeviceConnAction = "BROADCAST.BTDEVICECONN.ACTION";

    /**
     * 解析到心电数据
     */
    public static final String drawECGAction = "BROADCAST.ECG.ACTION";

    /**
     * 收到蓝牙数据
     */
    public static final String notifyDataChanged = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    /**
     * 删除指挥端的广播
     */
    public static final String deleteCommander = "BROADCAST.DELETE.COMMANDER.ACTION";

    public static void broadcastUpdate(final String action, String name, String content){
        final Intent intent = new Intent(action);
        intent.putExtra(name, content);
        MyApplication.getContext().sendBroadcast(intent);
    }

    public static void broadcastUpdate(final String action){
        final Intent intent = new Intent(action);
        MyApplication.getContext().sendBroadcast(intent);
    }

    public static void broadcastUpdate(final String action, byte[] data, final String uuid) {
        final Intent intent = new Intent(action);
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, data);
            intent.putExtra(EXTRA_UUID,uuid);
        }
        MyApplication.getContext().sendBroadcast(intent);
    }

}
