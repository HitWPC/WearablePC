package cn.hitftcl.wearablepc.Utils;

import android.os.Environment;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.HashMap;

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
    public static final HashMap<Integer,String> actionTcategory = new HashMap<>();
    public static final HashMap<String,Integer> actionTcategory2 = new HashMap<>();
    static {
        UserIPInfo me = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
        if(me!=null){
            MY_IP = me.getIp();
            MY_PORT = me.getPort();
        }
        actionTcategory.put(1,"sitting");
        actionTcategory.put(2,"standing");
        actionTcategory.put(3,"lying");
        actionTcategory.put(4,"upstairs");
        actionTcategory.put(5,"downstairs");
        actionTcategory.put(6,"walking");
        actionTcategory.put(7,"running");
        actionTcategory.put(8,"quickWalking");

        actionTcategory2.put("sitting",1);
        actionTcategory2.put("standing",2);
        actionTcategory2.put("lying",3);
        actionTcategory2.put("upstairs",4);
        actionTcategory2.put("downstairs",5);
        actionTcategory2.put("walking",6);
        actionTcategory2.put("running",7);
        actionTcategory2.put("quickWalking",8);
    }
}
