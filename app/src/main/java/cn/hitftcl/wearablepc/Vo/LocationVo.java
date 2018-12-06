package cn.hitftcl.wearablepc.Vo;

public class LocationVo {

    private String username;

    private String ip;

    private double longitude;

    private double latitude;

    public String getUsername() {
        return username;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocationVo(String username,String ip, double longitude, double latitude) {
        this.username = username;
        this.ip=ip;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
