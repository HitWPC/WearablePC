package cn.hitftcl.wearablepc.Model;

import com.amap.api.maps.model.LatLng;

/**
 * Created by Administrator on 2018/7/24.
 */

public class SynMessage {
    private float zoom;
    private LatLng latLng;

    public SynMessage(float zoom, LatLng latLng){
        this.latLng = latLng;
        this.zoom = zoom;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
