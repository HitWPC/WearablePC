package cn.hitftcl.wearablepc.Utils.ModelOperation;

import java.util.Date;

import cn.hitftcl.wearablepc.Model.HeartTable;

/**
 * Created by Administrator on 2018/10/10.
 */

public class HeartOperation {

    public static boolean storeHeartRate(int rate, String IP){
        HeartTable heartTable = new HeartTable(rate, new Date(), IP);
        return heartTable.save();
    }
}
