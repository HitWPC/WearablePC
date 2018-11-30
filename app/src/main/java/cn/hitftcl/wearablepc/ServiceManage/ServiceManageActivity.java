package cn.hitftcl.wearablepc.ServiceManage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.suke.widget.SwitchButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import cn.hitftcl.wearablepc.Service.SensorDataService;
import cn.hitftcl.wearablepc.Service.FusionService;
import cn.hitftcl.wearablepc.Service.ReceiveService;
import cn.hitftcl.wearablepc.Service.SendDataService;
import cn.hitftcl.wearablepc.Service.ServiceManageService;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.Constant;

public class ServiceManageActivity extends AppCompatActivity implements SwitchButton.OnCheckedChangeListener {
    public static final String TAG = "debug001";
    private static SwitchButton autoManage, allManage,netService,btReceiveService,dataSendService,dataFusionService;
    public static Intent sensorDataService = null, netReceiveService = null, sendDataService=null, fusionService=null;
    public static ServiceInfo serviceInfo = new ServiceInfo();

    private static Intent serviceManageService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_manage);

        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_userip_edit);
        toolbar.setTitle("系统服务管理");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        initService();

    }

    public void initService() {
        if(netReceiveService==null)
            netReceiveService = new Intent(this, ReceiveService.class);
        if(sensorDataService==null)
            sensorDataService = new Intent(this, SensorDataService.class);;
        if(sendDataService==null)
            sendDataService = new Intent(this, SendDataService.class);
        if(fusionService==null)
            fusionService = new Intent(this, FusionService.class);
        if(serviceManageService==null)
            serviceManageService = new Intent(this, ServiceManageService.class);
    }

    private void initView() {
        autoManage = findViewById(R.id.auto_button);
        allManage = findViewById(R.id.chooseAll_button);
        netService = findViewById(R.id.net_service);
        btReceiveService = findViewById(R.id.bt_receive_service);
        dataSendService = findViewById(R.id.data_send_service);
        dataFusionService = findViewById(R.id.data_fusion_service);

        autoManage.setOnCheckedChangeListener(this);
        allManage.setOnCheckedChangeListener(this);
        netService.setOnCheckedChangeListener(this);
        btReceiveService.setOnCheckedChangeListener(this);
        dataFusionService.setOnCheckedChangeListener(this);
        dataSendService.setOnCheckedChangeListener(this);

//        parseServiceFile();
        parseServiceInfo();


    }

    public static void parseServiceFile() {
        File file = new File(Constant.serviceInfoPath, "service.temp");
        if (file.exists()){
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
                ServiceInfo serviceInfo = (ServiceInfo)objectInputStream.readObject();
                System.out.println(serviceInfo);
//                parseServiceInfo(serviceInfo);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static  void parseServiceInfo() {
        if(serviceInfo.isAuto()){
            autoManage.setChecked(true);
            allManage.setChecked(false);
            allManage.setEnabled(false);
            netService.setChecked(serviceInfo.isNetService());
            netService.setEnabled(false);
            btReceiveService.setChecked(serviceInfo.isBtReceiveService());
            btReceiveService.setEnabled(false);
            dataSendService.setChecked(serviceInfo.isDataSendService());
            dataSendService.setEnabled(false);
            dataFusionService.setChecked(serviceInfo.isDataFusionService());
            dataFusionService.setEnabled(false);
            return;
        }
        if(serviceInfo.isAll()){
            allManage.setChecked(true);
            netService.setChecked(true);
            netService.setEnabled(false);
            btReceiveService.setChecked(true);
            btReceiveService.setEnabled(false);
            dataSendService.setChecked(true);
            dataSendService.setEnabled(false);
            dataFusionService.setChecked(true);
            dataFusionService.setEnabled(false);
            return;
        }
        if(serviceInfo.isNetService()){
            netService.setChecked(true);
        }
        if (serviceInfo.isDataFusionService()){
            dataFusionService.setChecked(true);
        }
        if (serviceInfo.isBtReceiveService()){
            btReceiveService.setChecked(true);
        }
        if (serviceInfo.isDataSendService()){
            dataSendService.setChecked(true);
        }

    }

    /**
     * toolbar返回按钮响应事件
     * @param item
     * @return
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

    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        switch (view.getId()){
            case R.id.auto_button:
                if (isChecked){                     //自动管理
                    //一键开启灭掉
                    allManage.setChecked(false);
                    //其余按钮不可点击
                    allManage.setEnabled(false);
                    netService.setEnabled(false);
                    btReceiveService.setEnabled(false);
                    dataSendService.setEnabled(false);
                    dataFusionService.setEnabled(false);

                    serviceInfo.setAuto(true);

                    startService(serviceManageService);  //开启自动管理服务模式

                }else{                              //手动管理
                    //所有按钮可点击
                    allManage.setEnabled(true);
                    allManage.setEnabled(true);
                    netService.setEnabled(true);
                    btReceiveService.setEnabled(true);
                    dataSendService.setEnabled(true);
                    dataFusionService.setEnabled(true);
                    serviceInfo.setAuto(false);
                    stopService(serviceManageService);
                }
                break;
            case R.id.chooseAll_button:
                if (isChecked){
                    netService.setChecked(true);
                    netService.setEnabled(false);
                    btReceiveService.setChecked(true);
                    btReceiveService.setEnabled(false);
                    dataSendService.setChecked(true);
                    dataSendService.setEnabled(false);
                    dataFusionService.setChecked(true);
                    dataFusionService.setEnabled(false);
                    serviceInfo.setAll(true);
                    if (!serviceInfo.isNetService()){
                        //TODO 开启接收网络数据的服务
                        startService(netReceiveService);
                        serviceInfo.setNetService(true);
                        Log.d(TAG, "onCheckedChanged: 开启接收网络通信服务1");
                    }
                    if (!serviceInfo.isBtReceiveService()){
                        //TODO 开启传感器数据接收服务
                        startService(sensorDataService);
                        serviceInfo.setBtReceiveService(true);
                        Log.d(TAG, "onCheckedChanged: 开启蓝牙接收服务1");
                    }
                    if (!serviceInfo.isDataSendService()){
                        //TODO 开启数据上报服务
                        startService(sendDataService);
                        serviceInfo.setDataSendService(true);
                        Log.d(TAG, "onCheckedChanged: 开启传数据上报服务1");
                    }
                    if (!serviceInfo.isDataFusionService()){
                        //TODO 开启数据融合服务
                        startService(fusionService);
                        serviceInfo.setDataFusionService(true);
                        Log.d(TAG, "onCheckedChanged: 开启数据融合服务1");
                    }
                    Toast.makeText(this,"一键开启服务成功", Toast.LENGTH_SHORT).show();
                }else {
                    if(!autoManage.isChecked()){
                        netService.setEnabled(true);
                        btReceiveService.setEnabled(true);
                        dataSendService.setEnabled(true);
                        dataFusionService.setEnabled(true);
                    }
                    serviceInfo.setAll(false);
                }
                break;
            case R.id.net_service:
                if (isChecked){
                    if (!serviceInfo.isNetService()){
                        startService(netReceiveService);
                        serviceInfo.setNetService(true);
                        Toast.makeText(this,"已开启接收网络通信服务", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onCheckedChanged: 开启接收网络通信服务2");
                    }
                }
                else {
                    stopService(netReceiveService);
                    serviceInfo.setNetService(false);
                    Toast.makeText(this,"已关闭接收网络数据的服务", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCheckedChanged: 关闭接收网络数据的服务");
                }
                break;
            case R.id.bt_receive_service:
                if (isChecked){
                    if (!serviceInfo.isBtReceiveService()){
                        //TODO 开启传感器数据接收服务
                        startService(sensorDataService);
                        serviceInfo.setBtReceiveService(true);
                        Toast.makeText(this,"已开启蓝牙接收服务", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onCheckedChanged: 开启蓝牙接收服务2");
                    }
                }
                else {
                    stopService(sensorDataService);
                    serviceInfo.setBtReceiveService(false);
                    Toast.makeText(this,"已关闭蓝牙接收服务", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCheckedChanged: 关闭蓝牙接收服务");
                }
                break;
            case R.id.data_send_service:
                if (isChecked){
                    if (!serviceInfo.isDataSendService()){
                        //TODO 开启数据上报服务
                        startService(sendDataService);
                        serviceInfo.setDataSendService(true);
                        Toast.makeText(this,"已开启数据上报服务", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onCheckedChanged: 开启传数据上报服务2");
                    }
                }
                else {
                    stopService(sendDataService);
                    serviceInfo.setDataSendService(false);
                    Toast.makeText(this,"已关闭数据上报服务", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCheckedChanged: 关闭数据上报服务");
                }
                break;
            case R.id.data_fusion_service:
                if (isChecked){
                    if (!serviceInfo.isDataFusionService()){
                        //TODO 开启数据融合服务
                        startService(fusionService);
                        serviceInfo.setDataFusionService(true);
                        Toast.makeText(this,"已开启数据融合服务", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onCheckedChanged: 开启数据融合服务");
                    }
                }
                else {
                    stopService(fusionService);
                    serviceInfo.setDataFusionService(false);
                    Toast.makeText(this,"已关闭数据融合服务", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCheckedChanged: 关闭数据融合服务");
                }
                break;
            default:
                break;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

    }

    private ServiceInfo getServiceInfo() {
        serviceInfo.setAuto(autoManage.isChecked());
        serviceInfo.setAll(allManage.isChecked());
        return serviceInfo;
    }

}
