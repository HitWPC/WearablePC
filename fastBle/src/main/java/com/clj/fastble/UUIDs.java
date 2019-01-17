package com.clj.fastble;

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

    /**
     * ECG NOTIFY Characteristic
     */
    public static final String UUID_ECG_Char_Notify = "0000fff3-0000-1000-8000-00805f9b34fb";

    /**
     * ECG Write Characteristic
     */
    public static final String UUID_ECG_Char_Write = "0000fff5-0000-1000-8000-00805f9b34fb";

    /**
     * 动作NOTIFY Service
     */
    public static final String UUID_Action_Char_Service = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    /**
     * 动作NOTIFY Characteristic
     */
    public static final String UUID_Action_Char_Notify = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    /**
     * 动作WRITE Characteristic
     */
    public static final String UUID_Action_Char_Write = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    /**
     * 可靠传输 Service
     */
    public static final String UUID_Reliable_Service = "19b10040-e8f2-537e-4f6c-d104768a1214";
    /**
     * 可靠传输Write Characteristic
     */
    public static final String UUID_Reliable_Char_Write = "19b10041-e8f2-537e-4f6c-d104768a1214";
    /**
     * 可靠传输Notify Characteristic
     */
    public static final String UUID_Reliable_Char_Notify = "19b10042-e8f2-537e-4f6c-d104768a1214";

    /**
     *同步 Service
     */
    public static final String UUID_Sync_Service = "19b10050-e8f2-537e-4f6c-d104768a1214";
    /**
     * 同步Write Characteristic
     */
    public static final String UUID_Sync_Char_Write = "19b10051-e8f2-537e-4f6c-d104768a1214";
    /**
     * 同步Notify Characteristic
     */
    public static final String UUID_Sync_Char_Notify = "19b10052-e8f2-537e-4f6c-d104768a1214";

}
