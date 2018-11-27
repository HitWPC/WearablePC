package cn.hitftcl.wearablepc.Bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.callback.ConnectCallback;
import cn.hitftcl.ble.callback.ScanCallback;
import cn.hitftcl.wearablepc.R;

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
    private SimpleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scan, container,false);

        // TODO 第一步：检查BLE兼容性，申请权限
//        checkBLE();  在index界面做

        // TODO 第二步：初始化
        mBleController = BleController.getInstance().init(getContext());

        // TODO 获取控件，监听按钮点击事件
        scanBtn = v.findViewById(R.id.startScan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanDevices();
            }
        });

        scanDevices = v.findViewById(R.id.mDeviceList);
        adapter = new SimpleAdapter(getContext(), btDevices, android.R.layout.simple_list_item_2,new String[]{"名称","地址"},new int[]{android.R.id.text1,android.R.id.text2});
        scanDevices.setAdapter(adapter);

        scanDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showProgressDialog("请稍后", "正在连接设备");

                // TODO 点击条目后,获取地址，根据地址连接设备
                final String address = btDevices.get(i).get("地址");
                final int position = i;
                mBleController.connect(0, address, new ConnectCallback() {
                    @Override
                    public void onConnSuccess() {
                        hideProgressDialog();
                        Toast.makeText(getActivity(), "连接成功", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "连接成功---position----->"+address+"  "+position);
                        Log.d(TAG, "bluetoothDevices---size----->"+bluetoothDevices.size());
                        Log.d(TAG, "btDevices---size----->"+btDevices.size());
                        bluetoothDevices.remove(position);
                        btDevices.remove(position);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onConnFailed() {
                        hideProgressDialog();
                        Toast.makeText(getContext(), "连接超时，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return v;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                } else {
                    Toast.makeText(getContext(), "未搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScanning(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (!bluetoothDevices.contains(device)) {
                    bluetoothDevices.add(device);
                    Map<String, String> map = new HashMap<>();
                    map.put("名称", device.getName()==null?"未知设备":device.getName());
                    map.put("地址",device.getAddress());
                    btDevices.add(map);
                    Log.d(TAG,"NAME: "+device.getName()+"  ADDR: "+device.getAddress());
                    adapter.notifyDataSetChanged();

                }

            }
        });
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
