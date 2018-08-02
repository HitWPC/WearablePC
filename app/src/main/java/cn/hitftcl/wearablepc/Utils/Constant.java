package cn.hitftcl.wearablepc.Utils;

import org.litepal.crud.DataSupport;

import cn.hitftcl.wearablepc.Model.UserIPInfo;

/**
 * Created by Administrator on 2018/7/24.
 */

public class Constant {
    public static boolean isMapActivityFront = false;
    public static String MY_IP = "";
    public static int MY_PORT = 0;

    static {
        UserIPInfo me = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
        if(me!=null){
            MY_IP = me.getIp();
            MY_PORT = me.getPort();
        }
    }
}