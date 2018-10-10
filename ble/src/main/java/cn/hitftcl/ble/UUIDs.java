package cn.hitftcl.ble;

/**
 * Created by Administrator on 2018/6/21.
 */

public class UUIDs {
    //TODO 这里是硬件提供的各种UUID 一定要根据自己的情况进行修改
    /**
     * 薄膜键盘
     */
    public static final String UUID_KEYPad = "00002a19-0000-1000-8000-00805f9b34fb";

    /**
     * 北斗NOTIFY  Service
     */
    public static final String UUID_BD_Service = "0000ffe0-0000-1000-8000-00805f9b34fb";

    /**
     * 北斗NOTIFY  Characteristic
     */
    public static final String UUID_BD_Char = "0000ffe1-0000-1000-8000-00805f9b34fb";


    /**
     * 环境NOTIFY  Service
     */
    public static final String UUID_ENVIRONMENT_Service = "0000fff0-0000-1000-8000-00805f9b34fb";

    /**
     * 环境Write  Characteristic
     */
    public static final String UUID_ENVIRONMENT_Char_Write = "0000fff1-0000-1000-8000-00805f9b34fb";

    /**
     * 环境NOTIFY  Characteristic
     */
    public static final String UUID_ENVIRONMENT_Char_Notify = "0000fff6-0000-1000-8000-00805f9b34fb";


    /**
     * 心率NOTIFY Service
     */
    public static final String UUID_Heart_Service = "0000180d-0000-1000-8000-00805f9b34fb";

    /**
     * 心率NOTIFY Characteristic
     */
    public static final String UUID_Heart_Char_Notify = "00002a37-0000-1000-8000-00805f9b34fb";
}
