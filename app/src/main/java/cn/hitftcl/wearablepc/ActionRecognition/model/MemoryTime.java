package cn.hitftcl.wearablepc.ActionRecognition.model;

import org.litepal.crud.DataSupport;

/**
 * Created by hzf on 2018/5/1.
 */

public class MemoryTime extends DataSupport{
    private int id;
    //最后一条记录的ID
    private int lastId;

    private int mode;

    private String time;

    private int svmFreq;

    private int timeSize;

    private String power;

    public MemoryTime(int lastId, String time, int svmFreq, int timeSize, int mode) {
        this.lastId = lastId;
        this.time = time;
        this.svmFreq = svmFreq;
        this.timeSize = timeSize;
        this.mode = mode;
    }

    public int getLastId() {
        return lastId;
    }


    public String getTime() {
        return time;
    }

    public int getSvmFreq() {
        return svmFreq;
    }

    public int getTimeSize() {
        return timeSize;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
