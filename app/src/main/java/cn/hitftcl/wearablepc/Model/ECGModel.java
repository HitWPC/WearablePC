package cn.hitftcl.wearablepc.Model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Administrator on 2018/12/6.
 */

public class ECGModel extends DataSupport {

    private int id;
    private long time ;
    private int value;
    private String IP;

    public ECGModel(Date time, int value, String IP) {
        this.time = time.getTime();
        this.value = value;
        this.IP = IP;
    }

    public ECGModel(long time, int value, String IP) {
        this.time = time;
        this.value = value;
        this.IP = IP;
    }



}
