package cn.hitftcl.wearablepc.Model;

import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.util.Date;

import cn.hitftcl.wearablepc.Utils.Constant;

/**
 * Created by anyihao on 2017/11/25.
 */
public class BDTable extends DataSupport {
    private int id;
    private double longitude;//经度
    private double latitude; //纬度
    private String recordDate; //接收日期
//    private int NorS;   //0表示北纬  1表示南纬
//    private int WorE;  //0表示西经   1表示东经
    private String IP;

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public BDTable() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BDTable( double longitude, double latitude, Date recordDate, String IP) {

        this.longitude = longitude;
        this.latitude = latitude;
        this.recordDate = Constant.dateFormat.format(recordDate);
        this.IP = IP;
    }



    public void setLongitude(double longitude){
        this.longitude = longitude;
    }
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public void setRecordDate(Date date){
        this.recordDate = Constant.dateFormat.format(recordDate);
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
        try {
            return Constant.dateFormat.parse(recordDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(System.currentTimeMillis()-24*60*3600);
        }
    }
//    public int getNorS(){
//        return NorS;
//    }
//    public int getWorE(){
//        return WorE;
//    }


}
