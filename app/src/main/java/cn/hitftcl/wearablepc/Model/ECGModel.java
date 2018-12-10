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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }
}
