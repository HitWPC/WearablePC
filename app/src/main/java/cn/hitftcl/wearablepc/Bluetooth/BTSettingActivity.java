package cn.hitftcl.wearablepc.Bluetooth;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.callback.ConnectCallback;
import cn.hitftcl.ble.callback.ScanCallback;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.PERMISSION;
import cn.hitftcl.wearablepc.Utils.RequestPermission;

public class BTSettingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = "debug001";
    private ProgressDialog progressDialog;
    private Button scanBtn, stopScanBtn;
    private ListView scanDevices;
    //搜索结果列表
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private static List<BluetoothDevice> connedDevices = new ArrayList<>();
    //BLE 模块
    private BleController mBleController;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btsetting);

        // TODO 第一步：检查BLE兼容性，申请权限
        checkBLE();

        // TODO 第二步：初始化
        mBleController = BleController.getInstance().init(this);

        // TODO 获取控件，监听按钮点击事件
        scanDevices = findViewById(R.id.mDeviceList1);

        scanBtn = findViewById(R.id.startScan);
        stopScanBtn = findViewById(R.id.stopScan);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanDevices();
            }
        });
    }

    /**'
     * 扫描外设
     */
    private void scanDevices() {

        showProgressDialog("请稍后", "正在搜索设备");

        mBleController.scanBle(0, new ScanCallback() {
            @Override
            public void onSuccess() {
                hideProgressDialog();

                if (bluetoothDevices.size() > 0) {
                    Log.d(TAG, "设备数量"+bluetoothDevices.size());
                    scanDevices.setAdapter(new DeviceListAdapter(BTSettingActivity.this, bluetoothDevices));
                    scanDevices.setOnItemClickListener(BTSettingActivity.this);
                } else {
                    Toast.makeText(BTSettingActivity.this, "未搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScanning(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (!bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device);
                    Log.d(TAG,"NAME: "+device.getName()+"  ADDR: "+device.getAddress());
                }

            }
        });
    }

    public void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, title, message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }


    /**
     * 检查BLE，申请权限
     */
    private void checkBLE() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        RequestPermission.requestPermission(this, PERMISSION.BLUETOOTH);
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        showProgressDialog("请稍后", "正在连接设备");

        // TODO 点击条目后,获取地址，根据地址连接设备
        String address = bluetoothDevices.get(i).getAddress();
        mBleController.connect(0, address, new ConnectCallback() {
            @Override
            public void onConnSuccess() {
                hideProgressDialog();
                Toast.makeText(BTSettingActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(MainActivity.this,SendAndReciveActivity.class));
            }

            @Override
            public void onConnFailed() {
                hideProgressDialog();
                Toast.makeText(BTSettingActivity.this, "连接超时，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
