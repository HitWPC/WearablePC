package cn.hitftcl.wearablepc.Utils;

import android.content.Context;
import android.content.Intent;

public class BroadCastUtil {
    public final static String sensorAction = "broadcast.sensor.action";
    public static void broadcastUpdate(Context context,final String action, String contentName,String content){
        final Intent intent = new Intent(action);
        intent.putExtra(contentName, content);
        context.sendBroadcast(intent);
    }
}
