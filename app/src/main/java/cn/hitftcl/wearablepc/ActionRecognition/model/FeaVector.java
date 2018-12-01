package cn.hitftcl.wearablepc.ActionRecognition.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

/**
 * Created by hzf on 2018/3/3.
 * 一段时序数据的特征向量
 */

public class FeaVector extends DataSupport{
    private int id;
    private ArrayList<Double> feature;
    private long startTime;
    private long endTime;
    private int category;
    private int origin;

    public FeaVector(ArrayList<Double> feature, long startTime, long endTime) {
        this.feature = feature;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public FeaVector(ArrayList<Double> feature, long startTime, long endTime, int category, int origin) {
        this.feature = feature;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.origin = origin;
    }

    public FeaVector(){

    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int catagory) {
        this.category = catagory;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Double> getFeature() {
        return feature;
    }

    public void setFeature(ArrayList<Double> feature) {
        this.feature = feature;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "FeaVector{" +
                "id=" + id +
                ", feature=" + feature +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", category=" + category +
                ", origin=" + origin +
                '}';
    }
}
