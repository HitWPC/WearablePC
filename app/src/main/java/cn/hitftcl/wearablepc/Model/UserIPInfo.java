package cn.hitftcl.wearablepc.Model;

import org.litepal.crud.DataSupport;

/**
 * 用户IP地址信息
 * Created by hzf on 2017/11/8.
 */

public class UserIPInfo extends DataSupport {
    public static final int TYPE_SELF = 0;//自己
    public static final int TYPE_OTHER = 1;//普通用户


    private int id;
    private String username;
    private String password;
    private String ip;
    private int port;
    private String BlueMac;
    private int type;
    private int isCaptain;  //0——》false 非队长

    public UserIPInfo(){

    }

    public UserIPInfo(int id, String username, String password, String ip, int port, String blueMac, int type, boolean isCaptain) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
        BlueMac = blueMac;
        this.type = type;
        this.isCaptain = isCaptain?1:0;
    }

    public UserIPInfo(String username, String ip, int port, String BlueMac, boolean isCaptain) {
        this.username = username;
        this.password = "";
        this.ip = ip;
        this.port = port;
        this.BlueMac = BlueMac;
        this.type = TYPE_OTHER;
        this.isCaptain = isCaptain?1:0;
    }
    public UserIPInfo(String username, String ip, int port) {
        this.username = username;
        this.password = "";
        this.ip = ip;
        this.port = port;
        this.type = TYPE_OTHER;
    }
    public UserIPInfo(String username, String password){
        this.username = username;
        this.password = password;
        this.type = TYPE_SELF;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBlueMac() {
        return BlueMac;
    }

    public void setBlueMac(String blueMac) {
        BlueMac = blueMac;
    }

    public void setCaptain(boolean is_captain){
        isCaptain = is_captain? 1 : 0;
    }

    public boolean isCaptain(){
        return isCaptain>0;
    }

    @Override
    public String toString() {
        return "UserIPInfo{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", BlueMac='" + BlueMac + '\'' +
                ", type=" + type +
                ", isCaptain=" + isCaptain +
                '}';
    }
}
