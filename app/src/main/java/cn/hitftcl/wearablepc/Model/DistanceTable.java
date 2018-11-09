package cn.hitftcl.wearablepc.Model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Administrator on 2018/11/8.
 */

public class DistanceTable extends DataSupport{
    private int id;
    private double distance;
    private Date date;
    private String IP;

    public DistanceTable(){
        
    }

    public DistanceTable(double distance, Date date, String IP) {
        this.distance = distance;
        this.date = date;
        this.IP = IP;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }
}
