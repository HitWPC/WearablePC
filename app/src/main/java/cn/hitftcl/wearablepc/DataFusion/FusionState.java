package cn.hitftcl.wearablepc.DataFusion;

import com.amap.api.maps.model.LatLng;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import cn.hitftcl.wearablepc.Utils.Constant;

/**
 * Created by Administrator on 2018/10/30.
 */

public class FusionState implements Serializable{
    public boolean heartAvailable = false;
    public boolean envAvailable = false;
    public boolean bdAvailable = false;

    private boolean isBodyNormal;
    private boolean isEnvNormal;

    /**
     * 体征
     */
    private int heartState;   //0->偏低 1->正常偏低 2->正常 3->正常偏高 4->偏高
    /**
     * 环境
     */
    private int temperature;
    private int humidity;
    private int pressure;
    private int so2;
    private int no;

    private LatLng BD_Position;
    private String IP;
    private String fusionTime;

    public FusionState() {
    }

    public FusionState(boolean isBodyNormal, boolean isEnvNormal, int heartState, int temperature, int humidity, int pressure, int so2, int no, LatLng BD_Position, String IP, Date fusionTime) {
        this.isBodyNormal = isBodyNormal;
        this.isEnvNormal = isEnvNormal;
        this.heartState = heartState;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.so2 = so2;
        this.no = no;
        this.BD_Position = BD_Position;
        this.IP = IP;
        this.fusionTime = Constant.dateFormat.format(fusionTime);
    }

    public LatLng getBD_Position() {
        return BD_Position;
    }

    public void setBD_Position(LatLng BD_Position) {
        this.BD_Position = BD_Position;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public Date getFusionTime() {
        try {
            return Constant.dateFormat.parse(fusionTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(System.currentTimeMillis()-24*60*3600);

        }
    }

    public void setFusionTime(Date fusionTime) {
        this.fusionTime = Constant.dateFormat.format(fusionTime);
    }

    public boolean isBodyNormal() {
        return isBodyNormal;
    }

    public void setBodyNormal(boolean bodyNormal) {
        isBodyNormal = bodyNormal;
    }

    public boolean isEnvNormal() {
        return isEnvNormal;
    }

    public void setEnvNormal(boolean envNormal) {
        isEnvNormal = envNormal;
    }

    public int getHeartState() {
        return heartState;
    }

    public void setHeartState(int heartState) {
        this.heartState = heartState;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getSo2() {
        return so2;
    }

    public void setSo2(int so2) {
        this.so2 = so2;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    @Override
    public String toString() {
        return "FusionState{" +
                "heartAvailable=" + heartAvailable +
                ", envAvailable=" + envAvailable +
                ", bdAvailable=" + bdAvailable +
                ", isBodyNormal=" + isBodyNormal +
                ", isEnvNormal=" + isEnvNormal +
                ", heartState=" + heartState +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", so2=" + so2 +
                ", no=" + no +
                ", BD_Position=" + BD_Position +
                ", IP='" + IP + '\'' +
                ", fusionTime=" + fusionTime +
                '}';
    }
}
