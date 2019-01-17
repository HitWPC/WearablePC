package cn.hitftcl.wearablepc.Bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.UUIDs;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.callback.ConnectCallback;
import cn.hitftcl.ble.callback.ScanCallback;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.BroadCastUtil;

/**
 * Created by Administrator on 2018/6/22.
 */

public class ScanFragment extends Fragment {

    public static final String TAG = "debug001";
    private ProgressDialog progressDialog;
    private Button scanBtn;
    private ListView scanDevices;
    //搜索结果列表
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private List<Map<String, String>> btDevices = new ArrayList<>();
    //BLE 模块
    private BleController mBleController;
//    private DeviceListAdapter deviceListAdapter;
    private DeviceListAdapter mDeviceAdapter = null;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scan, container,false);

        // TODO 第一步：检查BLE兼容性，申请权限

        // TODO 第二步：初始化
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        // TODO 获取控件，监听按钮点击事件

        scanBtn = v.findViewById(R.id.startScan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
        mDeviceAdapter = new DeviceListAdapter(getActivity());
        scanDevices = v.findViewById(R.id.mDeviceList);
        scanDevices.setAdapter(mDeviceAdapter);

        scanDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // TODO 点击条目后,获取地址，根据地址连接设备
                connect(mDeviceAdapter.getItem(i));
            }
        });
        return v;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void startScan() {
        if(!BleManager.getInstance().isBlueEnable()){
            BleManager.getInstance().enableBluetooth();
        }
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                showProgressDialog("请稍后", "正在搜索设备");
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                hideProgressDialog();
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new MyBleGattCallback(bleDevice));
    }

    class MyBleGattCallback extends BleGattCallback{

        private BleDevice bleDevice;

        public MyBleGattCallback(BleDevice bleDevice){
            this.bleDevice = bleDevice;
        }

            @Override
            public void onStartConnect() {
                showProgressDialog("请稍后", "正在连接"+bleDevice.getName());
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                final  BleDevice bleDevice1 = bleDevice;
                hideProgressDialog();
                Toast.makeText(getActivity(), bleDevice.getName()+"连接成功", Toast.LENGTH_LONG).show();
                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                BroadCastUtil.broadcastUpdate(BroadCastUtil.btDeviceConnAction);
                OpenNotify(bleDevice);
                sendInsToAction(bleDevice);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                Toast.makeText(MyApplication.getContext(), bleDevice.getName()+"断开连接："+BleManager.getInstance().isConnected(bleDevice),Toast.LENGTH_SHORT).show();
                BroadCastUtil.broadcastUpdate(BroadCastUtil.btDeviceConnAction);
//                if(bleDevice.getName().contains("Ring-Node")){
//                    BleManager.getInstance().connect(bleDevice, new MyBleGattCallback(bleDevice));
//                }

//                if (isActiveDisConnected) {
//                    Toast.makeText(MainActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
//                    ObserverManager.getInstance().notifyObserver(bleDevice);
//                }
            }
    }

    private void sendInsToAction(final BleDevice bleDevice) {
        if(bleDevice.getName().contains("Ring")){

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    byte[] buf = {0x01,0x01,0x02,0x00,0x03,0x00,0x00,0x0a};
                    BleManager.getInstance().write(bleDevice, UUIDs.UUID_Action_Char_Service, UUIDs.UUID_Action_Char_Write, buf,
                            new BleWriteCallback(){
                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                    Log.d(TAG, "write success");
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    Log.d(TAG, "write fail");
                                }
                            });
                }
            }).start();

        }
    }

    private void OpenNotify(final BleDevice bleDevice) {
        List<BluetoothGattService> bluetoothGattServices = BleManager.getInstance().getBluetoothGatt(bleDevice).getServices();
        for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
            List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = bluetoothGattService.getCharacteristics();
            for (final BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristics) {
                if((bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_BD_Service) && bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_BD_Char))
                        || (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_ENVIRONMENT_Service) && bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_ENVIRONMENT_Char_Notify))
                        || (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_Heart_Service) && bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_Heart_Char_Notify))
//                        || (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_Heart_Service) && bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_ECG_Char_Notify))
                        || (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_Action_Char_Service) && bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_Action_Char_Notify))
                        || (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_Reliable_Service) && bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUIDs.UUID_Reliable_Char_Notify))){
                    BleManager.getInstance().notify(bleDevice, bluetoothGattService.getUuid().toString(), bluetoothGattCharacteristic.getUuid().toString(),
                           new MyNotifyCallback(bleDevice, bluetoothGattCharacteristic));
                }
            }
        }


    }

    class MyNotifyCallback extends BleNotifyCallback{

        private BleDevice bleDevice;
        private BluetoothGattCharacteristic bluetoothGattCharacteristic;

        public MyNotifyCallback(BleDevice bleDevice, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            this.bleDevice = bleDevice;
            this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
        }

        public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
            return bluetoothGattCharacteristic;
        }

        public void setBluetoothGattCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
        }

        public BleDevice getBleDevice() {
            return bleDevice;
        }

        public void setBleDevice(BleDevice bleDevice) {
            this.bleDevice = bleDevice;
        }

        @Override
        public void onNotifySuccess() {
            Log.d(TAG, bleDevice.getName()+" notify success"+" "+bluetoothGattCharacteristic.getUuid().toString());
        }

        @Override
        public void onNotifyFailure(final BleException exception) {
            Log.d(TAG, bleDevice.getName()+" notify fail"+" "+bluetoothGattCharacteristic.getUuid().toString());
        }

        @Override
        public void onCharacteristicChanged(byte[] data) {
            BroadCastUtil.broadcastUpdate(BroadCastUtil.notifyDataChanged, data, bluetoothGattCharacteristic.getUuid().toString());
        }
    }


    public void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(getContext(), title, message, true, false);
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


}
