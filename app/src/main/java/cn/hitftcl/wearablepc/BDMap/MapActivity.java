package cn.hitftcl.wearablepc.BDMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import cn.hitftcl.wearablepc.Model.SynMessage;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.NetWork.NetworkUtil;
import cn.hitftcl.wearablepc.NetWork.ReceiveService;
import cn.hitftcl.wearablepc.NetWork.TransType;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.BitmapUtil;
import cn.hitftcl.wearablepc.Utils.Constant;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

import static cn.hitftcl.wearablepc.NetWork.ReceiveService.ACTION_SYN_COMMAND;

public class MapActivity extends AppCompatActivity {
    public static final String TAG = "debug001";

    public float INI_ZOOM = 18.5f;
    public LatLng INI_LATLNG = new LatLng(39.92421163425557,116.39786526560786); //天安门

    private MapView mMapView = null;
    private AMap aMap = null;
    private UiSettings mUiSettings;

    private Button locationBtn;
    private Button synBtn;

    private List<UserIPInfo> group;
    private final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);

    private Timer timer = new Timer();
    private TimerTask task = null;

    private Bitmap selfBitmap;

    private static mBroadcastReceiver mBroadcastReceiver = null;
    private static IntentFilter intentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent myIntent = getIntent();
        String intent_content = myIntent.getStringExtra("Syn_Content");
        if(intent_content!=null && !intent_content.equals("")){
            SynMessage synMessage = new Gson().fromJson(intent_content, new TypeToken<SynMessage>(){}.getType());
            INI_ZOOM = synMessage.getZoom();
            INI_LATLNG = synMessage.getLatLng();
        }

        //找到除自己以外的其他队员信息
        group = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_OTHER)).find(UserIPInfo.class);

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
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                INI_LATLNG, INI_ZOOM, 30, 0)));

        //获取表示自己头像的Bitmap
        selfBitmap = BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.self_location_icon),80,80);


        //TODO 获取定位按钮
        locationBtn = findViewById(R.id.location_bt);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //TODO 获取同步按钮
        synBtn = findViewById(R.id.synBtn);
        synBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //找到除自己以外的其他队员信息
                group = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_OTHER)).find(UserIPInfo.class);

                float zoom = aMap.getCameraPosition().zoom;
                LatLng latLng_center = aMap.getCameraPosition().target;
                SynMessage synMessage = new SynMessage(zoom, latLng_center);
                final String content = new Gson().toJson(synMessage);
                ThreadPool.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        for (UserIPInfo user : group){
                            NetworkUtil.sendByTCP(user.getIp(),user.getPort(),TransType.SYN_COMMAND, content);
                        }
                    }
                });
            }
        });

        //TODO 设置定时器任务，每间隔1S重新绘制地图队员位置
        task= new TimerTask() {
            @Override
            public void run() {
//                Log.d(TAG, "更新队友坐标");

                ConcurrentHashMap<String, LatLng> map = BD_Partner_Singleton.getInstance().getBD_Map();  //要更新的坐标
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
        Constant.isMapActivityFront = true;
        if(mBroadcastReceiver==null){
            mBroadcastReceiver = new mBroadcastReceiver();
            // 2. 设置接收广播的类型
            intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_SYN_COMMAND);
            // 3. 动态注册：调用Context的registerReceiver（）方法
        }
        registerReceiver(mBroadcastReceiver, intentFilter);

    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        Constant.isMapActivityFront = false;
        unregisterReceiver(mBroadcastReceiver);
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

    class mBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case ACTION_SYN_COMMAND:
                    String synContent = intent.getStringExtra("Syn_Content");
                    SynMessage synMessage = new Gson().fromJson(synContent, new TypeToken<SynMessage>(){}.getType());
                    aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                            synMessage.getLatLng(), synMessage.getZoom(), 30, 0)));
                    break;
                default:
                    break;
            }
        }
    }

}
