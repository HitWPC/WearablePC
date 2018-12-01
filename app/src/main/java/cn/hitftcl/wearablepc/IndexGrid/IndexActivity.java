package cn.hitftcl.wearablepc.IndexGrid;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import cn.hitftcl.wearablepc.BDMap.MapActivity;
import cn.hitftcl.wearablepc.BDMap.offlinemap.OfflineMapActivity;
import cn.hitftcl.wearablepc.Bluetooth.BluetoothActivity;
import cn.hitftcl.wearablepc.Bluetooth.ClassicBluetoothActivity;
import cn.hitftcl.wearablepc.Service.ActionAdaptiveService;
import cn.hitftcl.wearablepc.Service.SensorDataService;
import cn.hitftcl.wearablepc.DataFusion.FusionActivity;
import cn.hitftcl.wearablepc.DataFusion.FusionState;
import cn.hitftcl.wearablepc.Group.UserIPListActivity;
import cn.hitftcl.wearablepc.Message.SecretListActivity;
import cn.hitftcl.wearablepc.Service.ActionOriginService;
import cn.hitftcl.wearablepc.Service.FusionService;
import cn.hitftcl.wearablepc.Service.ReceiveService;
import cn.hitftcl.wearablepc.Service.SendDataService;
import cn.hitftcl.wearablepc.Service.ServiceManageService;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.ServiceManage.ServiceInfo;
import cn.hitftcl.wearablepc.ServiceManage.ServiceManageActivity;
import cn.hitftcl.wearablepc.Utils.Constant;

public class IndexActivity extends AppCompatActivity {
    public static Map<String,FusionState> fusionStateMap=new HashMap<String,FusionState>();
    private final static String TAG = "debug001";
    private Intent sensorDataService = null, netService = null, sendDataService =null, fusionService=null, serviceManageService=null;

    public static boolean isServiceInit=false;

    MyGridLayout grid;
    int[] srcs = { R.drawable.actions_booktag, R.drawable.actions_about,
            R.drawable.actions_message, R.drawable.actions_account, R.drawable.camera_usb, R.drawable.camera_wifi};
    String titles[] = { "地图", "数据融合", "条密", "小组", "USB摄像头", "wifi摄像头"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("哈工大可穿戴计算机");

        grid = (MyGridLayout) findViewById(R.id.list);
        grid.setGridAdapter(new MyGridLayout.GridAdatper() {
            @Override
            public View getView(int index) {
                View view = getLayoutInflater().inflate(R.layout.actions_item, null);
                ImageView iv = (ImageView) view.findViewById(R.id.iv);
                TextView tv = (TextView) view.findViewById(R.id.tv);
                iv.setImageResource(srcs[index]);
                tv.setText(titles[index]);
                return view;
            }

            @Override
            public int getCount() {
                return titles.length;
            }
        });
        grid.setOnItemClickListener(new MyGridLayout.OnItemClickListener() {

            @Override
            public void onItemClick(View v, int index) {
                switch (index){
                    case 0:
                        Intent intent0 = new Intent(IndexActivity.this, MapActivity.class);
                        startActivity(intent0);
                        break;
                    case 1:
                        Intent intent1 = new Intent(IndexActivity.this, FusionActivity.class);
                        startActivity(intent1);
                        break;
                    case 2:
                        Intent intent4 = new Intent(IndexActivity.this, SecretListActivity.class);
                        startActivity(intent4);
                        break;
                    case 3:
                        Intent intent5 = new Intent(IndexActivity.this, UserIPListActivity.class);
                        startActivity(intent5);
                        break;
                    case 4:
                        Intent intent6 = new Intent();
                        intent6.setClassName("cn.edu.hit.ftcl.wearablepc.UVCCamera", "cn.edu.hit.ftcl.wearablepc.UVCCamera.MainActivity");
                        startActivity(intent6);
                        break;
                    case 5:
                        Intent intent7 = new Intent();
                        intent7.setClassName("cn.edu.hit.ftcl.wearablepc.wificamera", "cn.edu.hit.ftcl.wearablepc.wificamera.thecamhi.main.MainActivity");
                        startActivity(intent7);
                        break;
                }
            }
        });

        checkBLE();


        //TODO 初始化服务
        if(!isServiceInit){
            File file = new File(Constant.serviceInfoPath, "service.temp");
            if (file.exists()){
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
                    ServiceInfo serviceInfo = (ServiceInfo)objectInputStream.readObject();
                    System.out.println(serviceInfo);
                    parseServiceInfo(serviceInfo);
                    isServiceInit = true;

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }else{
                Log.d(TAG, "开启服务管理……");
                if (!ServiceManageActivity.serviceInfo.isAuto()){
                    serviceManageService =new Intent(IndexActivity.this, ServiceManageService.class);
                    startService(serviceManageService);
                    ServiceManageActivity.serviceInfo.setAuto(true);
                }

            }
        }

//        //TODO 开启接收网络数据的服务
//        netService = new Intent(this, ReceiveService.class);
//        startService(netService);
//
//        //TODO 开启传感器数据接收服务
//        sensorDataService = new Intent(this, SensorDataService.class);
//        startService(sensorDataService);
//
//        //TODO 开启数据上报服务
//        sendDataService = new Intent(this, SendDataService.class);
//        startService(sendDataService);
//
//        //TODO 开启数据融合服务
//        fusionService = new Intent(this, FusionService.class);
//        startService(fusionService);

        //TODO 初始化蓝牙管理
        BleManager bleManager = BleManager.getInstance();
        bleManager.init(getApplication());

            Intent actionOriginService = new Intent(this, ActionOriginService.class);
            startService(actionOriginService);
    }

    private void parseServiceInfo(ServiceInfo serviceInfo) {
        if(ServiceManageActivity.netReceiveService==null)
            ServiceManageActivity.netReceiveService = new Intent(this, ReceiveService.class);
        if(ServiceManageActivity.sensorDataService==null)
            ServiceManageActivity.sensorDataService = new Intent(this, SensorDataService.class);
        if(ServiceManageActivity.sendDataService==null)
            ServiceManageActivity.sendDataService = new Intent(this, SendDataService.class);
        if(ServiceManageActivity.fusionService==null)
            ServiceManageActivity.fusionService = new Intent(this, FusionService.class);

        if(serviceInfo.isAuto()){
            if (!ServiceManageActivity.serviceInfo.isAuto()){
                serviceManageService =new Intent(IndexActivity.this, ServiceManageService.class);
                startService(serviceManageService);
                ServiceManageActivity.serviceInfo.setAuto(true);
                return;
            }

        }
        if(serviceInfo.isAll()){
            startService(ServiceManageActivity.netReceiveService);
            startService(ServiceManageActivity.sensorDataService);
            startService(ServiceManageActivity.sendDataService);
            startService(ServiceManageActivity.fusionService);
            ServiceManageActivity.serviceInfo.setNetService(true);
            ServiceManageActivity.serviceInfo.setBtReceiveService(true);
            ServiceManageActivity.serviceInfo.setDataSendService(true);
            ServiceManageActivity.serviceInfo.setDataFusionService(true);
            ServiceManageActivity.serviceInfo.setAll(true);
            return;
        }
        if(serviceInfo.isNetService()){
            startService(ServiceManageActivity.netReceiveService);
            ServiceManageActivity.serviceInfo.setNetService(true);
        }
        if (serviceInfo.isDataFusionService()){
            startService(ServiceManageActivity.fusionService);
            ServiceManageActivity.serviceInfo.setDataFusionService(true);
        }
        if (serviceInfo.isBtReceiveService()){
            startService(ServiceManageActivity.sensorDataService);
            ServiceManageActivity.serviceInfo.setBtReceiveService(true);
        }
        if (serviceInfo.isDataSendService()){
            startService(ServiceManageActivity.sendDataService);
            ServiceManageActivity.serviceInfo.setDataSendService(true);
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_index, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.offline_down:
                Intent offline_intent = new Intent(IndexActivity.this, OfflineMapActivity.class);
                startActivity(offline_intent);
                return true;
//            case R.id.about_us:
//                return true;
            case R.id.bleset:
                Intent btset_intent = new Intent(IndexActivity.this, BluetoothActivity.class);
                startActivity(btset_intent);
                return true;
            case R.id.classicbtset:
                Intent classic_intent = new Intent(IndexActivity.this, ClassicBluetoothActivity.class);
                startActivity(classic_intent);
                return true;
            case R.id.seviceManage:

                Intent service_intent = new Intent(IndexActivity.this, ServiceManageActivity.class);
                startActivity(service_intent);
                return true;
//            case R.id.voicetest:
//                Intent voice_intent = new Intent(IndexActivity.this, VoiceControlActivity.class);
//                startActivity(voice_intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serviceManageService != null) {
            stopService(serviceManageService);
            ServiceManageActivity.serviceInfo.setAuto(false);
            serviceManageService = null;
        }
        String filePath = Constant.serviceInfoPath;
        File dir = new File(filePath);
        if (!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(dir, "service.temp");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ServiceInfo serviceInfo = ServiceManageActivity.serviceInfo;
            System.out.println(serviceInfo);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(serviceInfo);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        stopService(sensorDataService);
//        stopService(netService);
//        stopService(sendDataService);
//        stopService(fusionService);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_0:
                Intent intent0 = new Intent(IndexActivity.this, MapActivity.class);
                startActivity(intent0);
                break;
            case KeyEvent.KEYCODE_1:
//                        Intent intent1 = new Intent(IndexActivity.this, SensorActivity.class);
//                        startActivity(intent1);
                break;
            case KeyEvent.KEYCODE_2:
//                        Intent intent2 = new Intent(IndexActivity.this, ImageActivity.class);
//                        startActivity(intent2);
                break;
            case KeyEvent.KEYCODE_3:
//                        Intent intent3 = new Intent(IndexActivity.this, FtpFileListActivity.class);
//                        startActivity(intent3);
                break;
            case KeyEvent.KEYCODE_4:
                Intent intent4 = new Intent(IndexActivity.this, SecretListActivity.class);
                startActivity(intent4);
                break;
            case KeyEvent.KEYCODE_5:
                Intent intent5 = new Intent(IndexActivity.this, UserIPListActivity.class);
                startActivity(intent5);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 检查BLE，申请权限
     */
    private void checkBLE() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            this.finish();
        }
        requestLocationPermission();
    }

    /**
     * 动态申请位置权限
     */
    private void requestLocationPermission(){
        Log.d(TAG, "requestLocationPermission: 申请权限");
        if (Build.VERSION.SDK_INT >= 23){
            int check = ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (check != PermissionChecker.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},0);
            }else{
                return;
            }
        }
    }


}