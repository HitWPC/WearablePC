package cn.hitftcl.wearablepc.BDMap;

import android.util.Log;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Utils.Constant;

/**
 * Created by Administrator on 2018/7/23.
 */

public class BD_Partner_Singleton {

    private static ConcurrentHashMap<String, LatLng>  BD_Map= new ConcurrentHashMap<>();


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
            if(!bdTable.getIP().equals(Constant.MY_IP)){
                Log.d("debug001",bdTable.getLatitude() + "  "+ bdTable.getLongitude());
                BD_Map.put(bdTable.getIP(), new LatLng(bdTable.getLatitude(), bdTable.getLongitude()));
            }

        }
    }

    public ConcurrentHashMap<String, LatLng> getBD_Map(){
        return BD_Map;
    }

    public ArrayList<BDTable> getBDArrayList(){
        ArrayList<BDTable> res = new ArrayList<>();
        for(Map.Entry<String, LatLng> temp : BD_Map.entrySet()){
            res.add(new BDTable(temp.getValue().longitude, temp.getValue().latitude, temp.getKey()));
        }
        return res;
    }
}
