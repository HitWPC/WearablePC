package cn.hitftcl.wearablepc.ActionRecognition.model;

import org.litepal.crud.DataSupport;

/**
 * 记录每天各个活动的总时间
 * Created by hzf on 2018/4/10.
 */

public class LineChartModel extends DataSupport{
    private int id;
    private String today;
    private long sitTime;
    private long runTime;
    private long walkTime;

    public LineChartModel(String today, long sitTime, long runTime, long walkTime) {
        this.today = today;
        this.sitTime = sitTime;
        this.runTime = runTime;
        this.walkTime = walkTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public long getSitTime() {
        return sitTime;
    }

    public void setSitTime(long sitTime) {
        this.sitTime = sitTime;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public long getWalkTime() {
        return walkTime;
    }

    public void setWalkTime(long walkTime) {
        this.walkTime = walkTime;
    }
}
