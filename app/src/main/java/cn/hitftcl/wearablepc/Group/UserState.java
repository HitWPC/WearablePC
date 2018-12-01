package cn.hitftcl.wearablepc.Group;

import cn.hitftcl.wearablepc.Model.UserIPInfo;

/**
 * Created by Administrator on 2018/12/1.
 */

public class UserState extends UserIPInfo {
    boolean online;


    public UserState(UserIPInfo userIPInfo, boolean online) {
        super(userIPInfo.getId(), userIPInfo.getUsername(), userIPInfo.getPassword(), userIPInfo.getIp(), userIPInfo.getPort(), userIPInfo.getBlueMac(),userIPInfo.getType(), userIPInfo.isCaptain());
        this.online = online;
    }


    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
