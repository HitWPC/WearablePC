package cn.hitftcl.wearablepc.ActionRecognition.model;

/**
 * Created by hzf on 2017/12/28.
 */


/**
 * 加速度计数据
 */
public class Accelerometer{
    private int id;
    private long time;
    private float x;
    private float y;
    private float z;

    public Accelerometer(long time, float x, float y, float z) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
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

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
