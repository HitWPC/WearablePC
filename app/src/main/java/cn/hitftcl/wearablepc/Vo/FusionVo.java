package cn.hitftcl.wearablepc.Vo;

public class FusionVo {

    private String username;

    private String heartstate;

    private String temperature;

    private String humidity;

    private String pressure;

    private String so;

    private String no;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHeartstate() {
        return heartstate;
    }

    public void setHeartstate(String heartstate) {
        this.heartstate = heartstate;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getSo() {
        return so;
    }

    public void setSo(String so) {
        this.so = so;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public FusionVo(String username, String heartstate, String temperature,String pressure, String humidity, String so, String no) {
        this.username = username;
        this.heartstate = heartstate;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure=pressure;
        this.so = so;
        this.no = no;
    }
}
