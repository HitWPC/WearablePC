package cn.hitftcl.wearablepc.ServiceManage;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/11/23.
 */

public class ServiceInfo implements Serializable{
    boolean auto;
    boolean all;
    boolean netService;
    boolean btReceiveService;
    boolean dataSendService;
    boolean dataFusionService;

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "auto=" + auto +
                ", all=" + all +
                ", netService=" + netService +
                ", btReceiveService=" + btReceiveService +
                ", dataSendService=" + dataSendService +
                ", dataFusionService=" + dataFusionService +
                '}';
    }

    public ServiceInfo(){

    }

    public ServiceInfo(boolean auto, boolean all, boolean netService, boolean btReceiveService, boolean dataSendService, boolean dataFusionService) {
        this.auto = auto;
        this.all = all;
        this.netService = netService;
        this.btReceiveService = btReceiveService;
        this.dataSendService = dataSendService;
        this.dataFusionService = dataFusionService;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public boolean isNetService() {
        return netService;
    }

    public void setNetService(boolean netService) {
        this.netService = netService;
    }

    public boolean isBtReceiveService() {
        return btReceiveService;
    }

    public void setBtReceiveService(boolean btReceiveService) {
        this.btReceiveService = btReceiveService;
    }

    public boolean isDataSendService() {
        return dataSendService;
    }

    public void setDataSendService(boolean dataSendService) {
        this.dataSendService = dataSendService;
    }

    public boolean isDataFusionService() {
        return dataFusionService;
    }

    public void setDataFusionService(boolean dataFusionService) {
        this.dataFusionService = dataFusionService;
    }
}
