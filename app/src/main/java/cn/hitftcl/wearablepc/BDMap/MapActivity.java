package cn.hitftcl.wearablepc.BDMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import cn.hitftcl.wearablepc.Group.UserIPListActivity;
import cn.hitftcl.wearablepc.Message.SecretListActivity;
import cn.hitftcl.wearablepc.Model.SynMessage;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.NetWork.NetworkUtil;
import cn.hitftcl.wearablepc.NetWork.TransType;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.BitmapUtil;
import cn.hitftcl.wearablepc.Utils.Constant;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

import static cn.hitftcl.wearablepc.NetWork.ReceiveService.ACTION_SYN_COMMAND;

public class MapActivity extends AppCompatActivity {
    public static final String TAG = "debug001";

    public float INI_ZOOM = 18.5f;
    public float INIT_TILE = 30;
    public float INIT_BEARING = 0;
    public LatLng INI_LATLNG = new LatLng(39.92421163425557,116.39786526560786); //天安门
    private MapView mMapView = null;
    private AMap aMap = null;
    private UiSettings mUiSettings;

    private Button locationBtn;
    private Button synBtn;
    private Button messageBtn;
    private Button groupBtn;
    private List<UserIPInfo> group;
    private final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);

    private Timer timer = new Timer();
    private TimerTask task = null;

    private Bitmap selfBitmap;
    private Menu mMenu=null;
    private static mBroadcastReceiver mBroadcastReceiver = null;
    private static IntentFilter intentFilter = null;

    //控制地图绘制
    private Polyline polyline;
    private List<LatLng> listPts = new ArrayList<>();
    private boolean isDrawingRoute = false, isDrawingSymbol = false;
    private String SYMBOL_TYPE_NAME = SYMBOL_TYPE.NONE.name();
    private HashMap<LatLng, String> symbolMap = new HashMap<>();
    private ArrayList<Marker> listSymbol = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);



        Intent myIntent = getIntent();
        String intent_content = myIntent.getStringExtra("Syn_Content");
        if(intent_content!=null && !intent_content.equals("")){
//            SynMessage synMessage = new Gson().fromJson(intent_content, new TypeToken<SynMessage>(){}.getType());
            Log.d(TAG, "intent_content--->"+intent_content);
            MapInfo mapInfo =  new Gson().fromJson(intent_content, new TypeToken<MapInfo>(){}.getType());
            parseMapInfo(mapInfo);
        }else{
            File mapFile = new File(Constant.mapInfoPath, "map.temp");
            if (mapFile.exists()){
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(mapFile));
                    MapInfo initMapInfo = (MapInfo)objectInputStream.readObject();
                    parseMapInfo(initMapInfo);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        groupBtn = (Button) findViewById(R.id.groupBtn);
        messageBtn =(Button) findViewById(R.id.messageBtn);
        groupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, UserIPListActivity.class);
                startActivity(intent);
            }
        });
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, SecretListActivity.class);
                startActivity(intent);
            }
        });
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
                INI_LATLNG, INI_ZOOM, INIT_TILE, INIT_BEARING)));

        //获取表示自己头像的Bitmap
        selfBitmap = BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.self_location_icon),80,80);


        //TODO 获取定位按钮
        locationBtn = findViewById(R.id.location_bt);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "SIZE:"+ listSymbol.size());

            }
        });

        //TODO 获取同步按钮
        synBtn = findViewById(R.id.synBtn);
        synBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //找到除自己以外的其他队员信息
                group = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_OTHER)).find(UserIPInfo.class);

//                float zoom = aMap.getCameraPosition().zoom;
//                LatLng latLng_center = aMap.getCameraPosition().target;
//                SynMessage synMessage = new SynMessage(zoom, latLng_center);
                MapInfo mapInfo = getMapInfo();
                final String content = new Gson().toJson(mapInfo);
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
                                    latlng, aMap.getCameraPosition().zoom, aMap.getCameraPosition().tilt, aMap.getCameraPosition().bearing)));
                        }
                        IP_Marker_Map.get(IP).setPosition(latlng);
                    }else{
                        if(IP.equals(self.getIp())){
                            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    latlng, aMap.getCameraPosition().zoom,  aMap.getCameraPosition().tilt, aMap.getCameraPosition().bearing)));
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

        //TODO 绘制线/标记
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(isDrawingRoute){
                    listPts.add(latLng);
                    // 地图上绘制路线
                    drawLine(listPts, false, false);
                }else if(isDrawingSymbol){
                    drawMarker(latLng, SYMBOL_TYPE_NAME);
                }
            }
        });
        timer.schedule(task, 0,1000);


        //TODO 坦克等标记的拖拽监听
        AMap.OnMarkerDragListener markerDragListener = new AMap.OnMarkerDragListener() {
            // 当marker开始被拖动时回调此方法, 这个marker的位置可以通过getPosition()方法返回。
            // 这个位置可能与拖动的之前的marker位置不一样。
            // marker 被拖动的marker对象。
            @Override
            public void onMarkerDragStart(Marker arg0) {
                listSymbol.remove(arg0);
            }
            // 在marker拖动完成后回调此方法, 这个marker的位置可以通过getPosition()方法返回。
            // 这个位置可能与拖动的之前的marker位置不一样。
            // marker 被拖动的marker对象。
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                listSymbol.add(arg0);
            }

            @Override
            public void onMarkerDrag(Marker marker) {}
        };
        // 绑定marker拖拽事件
        aMap.setOnMarkerDragListener(markerDragListener);

        //TODO 绘制初始地图路线、标记
        initDraw();


    }

    private void initDraw() {
        if(listPts.size()>0){
            drawLine(listPts, true, true);
        }
        listSymbol.clear();
        if(symbolMap.size()>0){
            for (Map.Entry<LatLng, String> entry : symbolMap.entrySet()){
                drawMarker(entry.getKey(), entry.getValue());
            }
        }
    }

    private void parseMapInfo(MapInfo initMapInfo) {
        INI_LATLNG = new LatLng(initMapInfo.getMap_center().getLat(),initMapInfo.getMap_center().getLng());
        INI_ZOOM = initMapInfo.getZoom();
        INIT_TILE = initMapInfo.getTilt();
        INIT_BEARING = initMapInfo.getBearing();
        HashMap<String,ArrayList<Mlatlng>> symbolRouteMap = initMapInfo.getMapMessage();
        if (symbolRouteMap.containsKey("路线")){
            List<Mlatlng> path = symbolRouteMap.get("路线");
            listPts.clear();
            for (Mlatlng mlatlng:path){
                listPts.add(new LatLng(mlatlng.getLat(),mlatlng.getLng()));
            }
        }
        symbolMap.clear();
        if (symbolRouteMap.containsKey(SYMBOL_TYPE.TANC.name())){
            List<Mlatlng> mlatlngs = symbolRouteMap.get(SYMBOL_TYPE.TANC.name());
            for(Mlatlng temp : mlatlngs){
                symbolMap.put(new LatLng(temp.getLat(),temp.getLng()), SYMBOL_TYPE.TANC.name());
            }
        }
        if (symbolRouteMap.containsKey(SYMBOL_TYPE.HELICOPTER.name())){
            List<Mlatlng> mlatlngs = symbolRouteMap.get(SYMBOL_TYPE.HELICOPTER.name());
            for(Mlatlng temp : mlatlngs){
                symbolMap.put(new LatLng(temp.getLat(),temp.getLng()), SYMBOL_TYPE.HELICOPTER.name());
            }
        }
        if (symbolRouteMap.containsKey(SYMBOL_TYPE.CAMP.name())){
            List<Mlatlng> mlatlngs = symbolRouteMap.get(SYMBOL_TYPE.CAMP.name());
            for(Mlatlng temp : mlatlngs){
                symbolMap.put(new LatLng(temp.getLat(),temp.getLng()), SYMBOL_TYPE.CAMP.name());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.map_menu, menu);
        mMenu=menu;
        mMenu.findItem(R.id.draw_route_start).setVisible(true);
        mMenu.findItem(R.id.draw_route_end).setVisible(false);
        mMenu.findItem(R.id.draw_back).setVisible(false);

        mMenu.findItem(R.id.make_symbol_start).setVisible(true);
        mMenu.findItem(R.id.make_symbol_end).setVisible(false);
        mMenu.findItem(R.id.tanc_symbol).setVisible(false);
        mMenu.findItem(R.id.helicopter_symbol).setVisible(false);
        mMenu.findItem(R.id.camp_symbol).setVisible(false);

        mMenu.findItem(R.id.route_send).setVisible(true);
        mMenu.findItem(R.id.symbol_send).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:  //返回上一页
                finish();
                break;
            case R.id.draw_route_start:
                listPts.clear();

                mMenu.findItem(R.id.draw_route_start).setVisible(false);
                mMenu.findItem(R.id.draw_route_end).setVisible(true);
                mMenu.findItem(R.id.draw_back).setVisible(true);
                mMenu.findItem(R.id.make_symbol_start).setVisible(false);

                mMenu.findItem(R.id.route_send).setVisible(false);
                mMenu.findItem(R.id.symbol_send).setVisible(false);

                isDrawingRoute = true;

                break;
            case R.id.draw_route_end:
                mMenu.findItem(R.id.draw_route_start).setVisible(true);
                mMenu.findItem(R.id.draw_route_end).setVisible(false);
                mMenu.findItem(R.id.draw_back).setVisible(false);
                mMenu.findItem(R.id.make_symbol_start).setVisible(true);

                mMenu.findItem(R.id.route_send).setVisible(true);
                mMenu.findItem(R.id.symbol_send).setVisible(true);

                isDrawingRoute = false;
                if(listPts.size()>1){
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                            BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.end_point),57,72));
                    drawMarker(listPts.get(listPts.size()-1), "终点", icon, null, null, false);
                }
                break;
            case R.id.draw_back:
                if(listPts!=null && listPts.size()>0){
                    listPts.remove(listPts.size()-1);
                    polyline.remove();
                    Log.d(TAG, ""+(polyline==null));
                    drawLine(listPts, true, false);
                }
                break;
            case R.id.route_send:
                if(listPts.size()>0){
                    //发送绘制的路线

                }else{
                    Toast.makeText(this, "请在绘制后选择发送", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.symbol_send:
                if(listSymbol.size()>0){
                    //发送绘制的点标记

                }else{
                    Toast.makeText(this, "请在绘制后选择发送", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.make_symbol_start:
                mMenu.findItem(R.id.draw_route_start).setVisible(false);
                mMenu.findItem(R.id.make_symbol_start).setVisible(false);
                mMenu.findItem(R.id.make_symbol_end).setVisible(true);
                mMenu.findItem(R.id.tanc_symbol).setVisible(true);
                mMenu.findItem(R.id.helicopter_symbol).setVisible(true);
                mMenu.findItem(R.id.camp_symbol).setVisible(true);

                mMenu.findItem(R.id.route_send).setVisible(false);
                mMenu.findItem(R.id.symbol_send).setVisible(false);

                isDrawingSymbol = true;
                break;
            case R.id.make_symbol_end:
                mMenu.findItem(R.id.make_symbol_end).setVisible(false);
                mMenu.findItem(R.id.draw_route_start).setVisible(true);
                mMenu.findItem(R.id.make_symbol_start).setVisible(true);
                mMenu.findItem(R.id.tanc_symbol).setVisible(false);
                mMenu.findItem(R.id.helicopter_symbol).setVisible(false);
                mMenu.findItem(R.id.camp_symbol).setVisible(false);

                mMenu.findItem(R.id.route_send).setVisible(true);
                mMenu.findItem(R.id.symbol_send).setVisible(true);

                isDrawingSymbol = false;
                SYMBOL_TYPE_NAME = SYMBOL_TYPE.NONE.name();
                break;
            case R.id.tanc_symbol:
                SYMBOL_TYPE_NAME = SYMBOL_TYPE.TANC.name();
                break;
            case R.id.helicopter_symbol:
                SYMBOL_TYPE_NAME = SYMBOL_TYPE.HELICOPTER.name();
                break;
            case R.id.camp_symbol:
                SYMBOL_TYPE_NAME = SYMBOL_TYPE.CAMP.name();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onStop() {
        super.onStop();
        String filePath = Constant.mapInfoPath;
        File mapDir = new File(filePath);
        if (!mapDir.exists()){
            mapDir.mkdirs();
        }
        File mapFile = new File(mapDir, "map.temp");
        if(!mapFile.exists()){
            try {
                mapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mapFile);
            MapInfo mapInfo = getMapInfo();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(mapInfo);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //定义键盘事件对程序的操控功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    private void drawMarker(LatLng latLng, String typeName){
        if(typeName.equals(SYMBOL_TYPE.TANC.name())){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                    BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.tanc),75,60));
            listSymbol.add(drawMarker(latLng, typeName, icon, null, null, true));
        }else if(typeName.equals(SYMBOL_TYPE.HELICOPTER.name())){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                    BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.helicopter),115,115));
            listSymbol.add(drawMarker(latLng, typeName, icon, null, null, true));
        }else if(typeName.equals(SYMBOL_TYPE.CAMP.name())){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                    BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.camp),90,80));
            listSymbol.add(drawMarker(latLng, typeName, icon, null, null, true));
        }
    }

    /**
     * 地图上绘制线
     * @param pts
     */
    private void drawLine(List<LatLng> pts, boolean ifCreate, boolean ifInit) {
        if(ifInit && pts.size()>1){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                    BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.start_point),57,72));
            drawMarker(pts.get(0), "起点", icon, null, null, false);
            BitmapDescriptor icon1 = BitmapDescriptorFactory.fromBitmap(
                    BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.end_point),57,72));
            drawMarker(pts.get(pts.size()-1), "终点", icon1, null, null, false);
        }else{
            if(pts.size() == 1){
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                        BitmapUtil.getAdaptBitMap(BitmapFactory.decodeResource(getResources(),R.drawable.start_point),57,72));
                drawMarker(pts.get(0), "起点", icon, null, null, false);
            }
        }

        if (pts.size() >= 2) {
            if (pts.size() == 2) {
                polyline = aMap.addPolyline((new PolylineOptions()).addAll(pts)
                        .width(10).setDottedLine(true).geodesic(true)
                        .color(Color.argb(255, 1, 1, 1)));
            } else {
                if(ifCreate){
                    polyline = aMap.addPolyline((new PolylineOptions()).addAll(pts)
                            .width(10).setDottedLine(true).geodesic(true)
                            .color(Color.argb(255, 1, 1, 1)));
                }else{
                    polyline.setPoints(pts);
                }
            }
        }
    }

    public MapInfo getMapInfo(){
        float zoom = aMap.getCameraPosition().zoom;
        float tile = aMap.getCameraPosition().tilt;
        float bearing = aMap.getCameraPosition().bearing;
        Mlatlng center = new Mlatlng(aMap.getCameraPosition().target.latitude,aMap.getCameraPosition().target.longitude);
        HashMap<String,ArrayList<Mlatlng>> symbolRouteInfo = new HashMap<>();
        if (listPts.size()>0){
            ArrayList<Mlatlng> route = new ArrayList<>();
            for (LatLng temp:listPts){
                route.add(new Mlatlng(temp.latitude,temp.longitude));
            }
            symbolRouteInfo.put("路线",route);
        }
        if (listSymbol.size()>0){
            ArrayList<Mlatlng> tancList=new ArrayList<>(),heliList=new ArrayList<>(),campList=new ArrayList<>();
            for (Marker temp:listSymbol){
                String obj = (String) temp.getObject();
                if (obj.equals(SYMBOL_TYPE.TANC.name())){
                    tancList.add(new Mlatlng(temp.getPosition().latitude,temp.getPosition().longitude));
                }
                else if (obj.equals(SYMBOL_TYPE.HELICOPTER.name())){
                    heliList.add(new Mlatlng(temp.getPosition().latitude,temp.getPosition().longitude));
                }
                else if (obj.equals(SYMBOL_TYPE.CAMP.name())){
                    campList.add(new Mlatlng(temp.getPosition().latitude,temp.getPosition().longitude));
                }
            }
            if (tancList.size()>0){
                symbolRouteInfo.put(SYMBOL_TYPE.TANC.name(),tancList);
            }
            if (heliList.size()>0){
                symbolRouteInfo.put(SYMBOL_TYPE.HELICOPTER.name(),heliList);
            }
            if (tancList.size()>0){
                symbolRouteInfo.put(SYMBOL_TYPE.CAMP.name(),campList);
            }
        }
        return new MapInfo(center,zoom,tile,bearing,symbolRouteInfo);

    }

    /**
     * 绘制标记
     * @param latLng
     * @param object
     * @param icon
     * @param title
     * @param snippet
     * @param draggable
     * @return
     */
    public Marker drawMarker(LatLng latLng, Object object, BitmapDescriptor icon, String title, String snippet, boolean draggable){
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.title(title).snippet(snippet);
        markerOption.icon(icon);
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(true);//设置marker平贴地图效果
        markerOption.draggable(draggable);
        final Marker marker =  aMap.addMarker(markerOption);
        marker.setObject(object);
        return marker;
    }

    class mBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case ACTION_SYN_COMMAND:
                    String synContent = intent.getStringExtra("Syn_Content");
                    Log.d(TAG, synContent);
                    MapInfo mapInfo = new Gson().fromJson(synContent, new TypeToken<MapInfo>(){}.getType());
                    parseMapInfo(mapInfo);
                    aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                            INI_LATLNG, INI_ZOOM, INIT_TILE, INIT_BEARING)));
                    initDraw();
                    break;
                default:
                    break;
            }
        }
    }

}
