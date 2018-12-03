package cn.hitftcl.wearablepc.ActionRecognition.model;

import org.litepal.crud.DataSupport;

/**
 * 用于首页显示每段活动
 * Created by hzf on 2018/4/4.
 */

public class Result {
    private int category;
    private long start;
    private long end;
    private int beginId;
    private int endId;

    public Result(int category, long start, long end, int beginId, int endId) {
        this.category = category;
        this.start = start;
        this.end = end;
        this.beginId = beginId;
        this.endId = endId;
    }

    public int getCategory() {
        return category;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getBeginId(){
        return beginId;
    }

    public int getEndId(){
        return endId;
    }

    public int getWrong(){
        int wrong = DataSupport.where("id >= ? and id <= ? and category != origin", String.valueOf(beginId), String.valueOf(endId)).count(FeaVector.class);
        return wrong;
    }
}
