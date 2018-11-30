package cn.hitftcl.wearablepc.Utils;

import android.content.Intent;

import cn.hitftcl.wearablepc.MyApplication;

/**
 * Created by Administrator on 2018/11/29.
 */

public class BroadCastUtil {

    public static final String sensorAction = "BROADCAST.SENSOR.ACTION";

    public static void broadcastUpdate(final String action,String name, String content){
        final Intent intent = new Intent(action);
        intent.putExtra(name, content);
        MyApplication.getContext().sendBroadcast(intent);
    }
}
