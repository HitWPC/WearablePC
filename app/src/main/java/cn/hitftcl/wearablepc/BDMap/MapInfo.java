package cn.hitftcl.wearablepc.BDMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MapInfo implements Serializable{
    private Mlatlng map_center;
    private float zoom;
    private float tilt; //倾斜角度
    private float bearing; //方向
    private HashMap<String,ArrayList<Mlatlng>> mapMessage;

    public MapInfo(Mlatlng map_center, float zoom, float tilt, float bearing, HashMap<String, ArrayList<Mlatlng>> mapMessage) {
        this.map_center = map_center;
        this.zoom = zoom;
        this.tilt = tilt;
        this.bearing = bearing;
        this.mapMessage = mapMessage;
    }

    public MapInfo() {
    }

    public Mlatlng getMap_center() {
        return map_center;
    }

    public void setMap_center(Mlatlng map_center) {
        this.map_center = map_center;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public HashMap<String, ArrayList<Mlatlng>> getMapMessage() {
        return mapMessage;
    }

    public void setMapMessage(HashMap<String, ArrayList<Mlatlng>> mapMessage) {
        this.mapMessage = mapMessage;
    }

    public float getTilt() {
        return tilt;
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }
}
