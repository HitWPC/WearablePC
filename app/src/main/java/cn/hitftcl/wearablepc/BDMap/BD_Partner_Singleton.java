package cn.hitftcl.wearablepc.BDMap;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

import cn.hitftcl.wearablepc.Model.BDTable;

/**
 * Created by Administrator on 2018/7/23.
 */

public class BD_Partner_Singleton {

    private static HashMap<String, LatLng>  BD_Map= new HashMap<>();

    private BD_Partner_Singleton(){}

    private static class Holder{
        private static final BD_Partner_Singleton instance = new BD_Partner_Singleton();
    }

    public static final BD_Partner_Singleton getInstance(){
        return Holder.instance;
    }

    public void setBD_Map(String IP, LatLng latLng){
        BD_Map.put(IP, latLng);
    }

    public void setBD_Map(ArrayList<BDTable> list){
        for(BDTable bdTable : list){
            BD_Map.put(bdTable.getIP(), new LatLng(bdTable.getLongitude(), bdTable.getLatitude()));
        }
    }

    public HashMap<String, LatLng> getBD_Map(){
        return BD_Map;
    }
}
