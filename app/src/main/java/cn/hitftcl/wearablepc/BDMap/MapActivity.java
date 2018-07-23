package cn.hitftcl.wearablepc.BDMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.BitmapUtil;

public class MapActivity extends AppCompatActivity {
    public static final String TAG = "debug001";

    public final float INI_ZOOM = 18.5f;

    private MapView mMapView = null;
    private AMap aMap = null;
    private UiSettings mUiSettings;

    private ImageButton locationBtn;
    private Button synBtn;

    private List<UserIPInfo> group;
    private final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);

    private Timer timer = new Timer();
    private TimerTask task = null;

    private Bitmap selfBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //找到除自己以外的其他队员信息
        group = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_COMMON)).find(UserIPInfo.class);

        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_secret);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        //TODO 设置UIsettings
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setCompassEnabled(true);  //指南针

        //TODO 设置中心点以及缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(INI_ZOOM));

        //获取表示自己头像的Bitmap
        selfBitmap = BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.self_location_icon),80,80);


        //获取定位按钮
        locationBtn = findViewById(R.id.location_bt);

        //获取同步按钮
        synBtn = findViewById(R.id.synBtn);
        synBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"缩放级别="+aMap.getCameraPosition().zoom);
                CameraUpdateFactory.zoomTo(INI_ZOOM);
//                final String content = "";  //同步数据
//                ThreadPool.getInstance().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        for(UserIPInfo user : group){
//                            NetworkUtil.sendByTCP(user.getIp(), user.getPort(), TransType.SYN_COMMAND, content);
//                        }
//                    }
//                });
            }
        });

        //TODO 设置定时器任务，每间隔1S重新绘制地图队员位置
        task= new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "更新队友坐标");

                HashMap<String, LatLng> map = BD_Partner_Singleton.getInstance().getBD_Map();  //要更新的坐标
                HashMap<String, Marker> IP_Marker_Map = new HashMap<>();
                List<Marker> markers = aMap.getMapScreenMarkers();  //所有的Marker

                for(Marker m : markers){
                    if(m.getObject().equals("队员")){
                        IP_Marker_Map.put(m.getTitle(), m);
                    }
                }

                for(Map.Entry entry : map.entrySet()){
                    String IP = (String) entry.getKey();
                    LatLng latlng = (LatLng) entry.getValue();

                    if(IP_Marker_Map.containsKey(IP)){
                        if(IP.equals(self.getIp())){
                            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    latlng, aMap.getCameraPosition().zoom, 30, 0)));
                        }
                        IP_Marker_Map.get(IP).setPosition(latlng);
                    }else{
                        if(IP.equals(self.getIp())){
                            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    latlng, aMap.getCameraPosition().zoom, 30, 0)));
                            MarkerOptions markerOptions = new MarkerOptions().position(latlng).title(IP).icon(
                                    BitmapDescriptorFactory.fromBitmap(selfBitmap)
                            );
                            aMap.addMarker(markerOptions).setObject("队员");
                        }else{
                            aMap.addMarker(new MarkerOptions().position(latlng).title(IP)).setObject("队员");
                        }
                    }
                }
            }
        };

        timer.schedule(task, 0,1000);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        timer.cancel();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * toolbar返回按钮响应事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

}
