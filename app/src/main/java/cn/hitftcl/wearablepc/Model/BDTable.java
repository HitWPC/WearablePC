package cn.hitftcl.wearablepc.Model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by anyihao on 2017/11/25.
 */
public class BDTable extends DataSupport {
    private double longitude;//经度
    private double latitude; //纬度
    private Date recordDate; //接收日期
//    private int NorS;   //0表示北纬  1表示南纬
//    private int WorE;  //0表示西经   1表示东经

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public void setRecordDate(Date date){
        this.recordDate = date;
    }
//    public void setNorS(int NorS){
//        this.NorS = NorS;
//    }
//    public void setWorE(int WorE){
//        this.WorE = WorE;
//    }
    public double getLongitude(){
        return longitude;
    }
    public double getLatitude(){
        return latitude;
    }
    public Date getRecordDate(){
        return recordDate;
    }
//    public int getNorS(){
//        return NorS;
//    }
//    public int getWorE(){
//        return WorE;
//    }


}
