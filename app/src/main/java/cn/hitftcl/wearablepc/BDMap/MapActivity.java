package cn.hitftcl.wearablepc.BDMap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;

import org.litepal.crud.DataSupport;

import java.util.List;

import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.NetWork.NetworkUtil;
import cn.hitftcl.wearablepc.NetWork.TransType;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

public class MapActivity extends AppCompatActivity {
    private MapView mMapView = null;
    private AMap aMap = null;

    private ImageButton locationBtn;
    private Button synBtn;

    private List<UserIPInfo> group;

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

        //TODO 设置中心点以及缩放级别




        //获取定位按钮
        locationBtn = findViewById(R.id.location_bt);

        //获取同步按钮
        synBtn = findViewById(R.id.synBtn);
        synBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        new LatLng(44.9,126.2), 8, 30, 0)));
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


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
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
