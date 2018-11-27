package cn.hitftcl.wearablepc.Utils;

import android.os.Environment;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;

import cn.hitftcl.wearablepc.Model.UserIPInfo;

/**
 * Created by Administrator on 2018/7/24.
 */

public class Constant {
    public static boolean isMapActivityFront = false;
    public static String MY_IP = "";
    public static int MY_PORT = 0;
    public static final String mapInfoPath = Environment.getExternalStorageDirectory() + "/HitWearable/map";
    public static final String imageInfoPath = Environment.getExternalStorageDirectory() + "/HitWearable/image";
    public static final String videoInfoPath = Environment.getExternalStorageDirectory() + "/HitWearable/video";
    public static final String voiceInfoPath = Environment.getExternalStorageDirectory() + "/HitWearable/voice";
    public static final String userPasswordPath    = Environment.getExternalStorageDirectory() + "/HitWearable/userPassword";
    public static final String serviceInfoPath    = Environment.getExternalStorageDirectory() + "/HitWearable/serviceInfo";
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    static {
        UserIPInfo me = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
        if(me!=null){
            MY_IP = me.getIp();
            MY_PORT = me.getPort();
        }
    }
}
