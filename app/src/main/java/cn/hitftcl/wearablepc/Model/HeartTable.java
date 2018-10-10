package cn.hitftcl.wearablepc.Model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Administrator on 2018/10/10.
 */

public class HeartTable extends DataSupport {
    private int id;
    private int rate;
    private Date date;
    private String IP;

    public HeartTable(){}

    public HeartTable(int rate, Date date, String IP) {
        this.rate = rate;
        this.date = date;
        this.IP = IP;
    }

    public HeartTable(int id, int rate, Date date) {
        this.id = id;
        this.rate = rate;
        this.date = date;
    }

    public HeartTable(int rate, Date date) {
        this.rate = rate;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
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
