package cn.hitftcl.wearablepc.Bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hitftcl.ble.BleController;
import cn.hitftcl.ble.UUIDs;
import cn.hitftcl.ble.callback.OnWriteCallback;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.PickerView;

/**
 * Created by Administrator on 2018/6/22.
 */

public class ConnectedFragment extends Fragment{
    public final static String TAG = "debug001";
    private BleController mBleController;
    private HashMap<BluetoothDevice,BluetoothGatt> conn_device_hashmap;
    private List<Map<String, String>> conn_btDevices = new ArrayList<>();

    private ListView connnectedLv;

    private SimpleAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_conn, container,false);

        mBleController = BleController.getInstance().init(getContext());

        // TODO 设置列表显示
        connnectedLv = v.findViewById(R.id.connnectedLv);
        adapter = new SimpleAdapter(getContext(), conn_btDevices, android.R.layout.simple_list_item_2,new String[]{"名称","地址"},new int[]{android.R.id.text1,android.R.id.text2});
        connnectedLv.setAdapter(adapter);

//        connnectedLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                // TODO 点击条目后,获取地址，根据地址断开连接设备
//                final String address = conn_btDevices.get(i).get("地址");
//                for(Map.Entry<BluetoothDevice, BluetoothGatt> entry : conn_device_hashmap.entrySet()){
//                    BluetoothDevice device = entry.getKey();
//                    if(device.getAddress().equals(address)){
//                        mBleController.closeBleConn(device);
//                        break;
//                    }
//                }
//            }
//        });
        connnectedLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int j= i;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setMessage("确认断开连接吗");
                builder.setTitle("提示");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        arg0.dismiss();
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    final int k =j;
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        final String address = conn_btDevices.get(k).get("地址");
                        for(Map.Entry<BluetoothDevice, BluetoothGatt> entry : conn_device_hashmap.entrySet()){
                            BluetoothDevice device = entry.getKey();
                            if(device.getAddress().equals(address)){
                                mBleController.closeBleConn(device);
                                break;
                            }
                        }

                    }
                });
                builder.create().show();
                return true;
            }
        });

        connnectedLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String []numbers = new String[1];
                numbers[0] = "08";//预防不选择时返回0
                final int j= i;
//                final EditText et = new EditText(view.getContext());
                final PickerView et = new PickerView(view.getContext());
                List<String> data = new ArrayList<String>();
                for (int s = 1; s <= 15; s++)
                {
                    data.add(s<10?"0" + s:""+s);
                }
                et.setData(data);
                et.setOnSelectListener(new PickerView.onSelectListener() {
                    @Override
                    public void onSelect(String text) {
                        numbers[0] = text;
                    }
                });
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(et);
                builder.setTitle("请选择速率(单位：s)");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    final int k =j;
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        Toast.makeText(MyApplication.getContext(), numbers[0]+"s",Toast.LENGTH_LONG).show();
                        final String address = conn_btDevices.get(k).get("地址");
                        for(Map.Entry<BluetoothDevice, BluetoothGatt> entry : conn_device_hashmap.entrySet()){
                            BluetoothDevice device = entry.getKey();
                            if(device.getAddress().equals(address)){
                                Log.d(TAG,"地址："+device.getAddress());
//                                mBleController.closeBleConn(device);
                                byte temp;
                                switch (numbers[0]){
                                    case "01":
                                        temp=0x01;
                                        break;
                                    case "02":
                                        temp = 0x02;
                                        break;
                                    case "03":
                                        temp = 0x03;
                                        break;
                                    case "04":
                                        temp = 0x04;
                                        break;
                                    case "05":
                                        temp = 0x05;
                                        break;
                                    case "06":
                                        temp = 0x06;
                                        break;
                                    case "07":
                                        temp = 0x07;
                                        break;
                                    case "08":
                                        temp = 0x08;
                                        break;
                                    case "09":
                                        temp = 0x09;
                                        break;
                                    case "10":
                                        temp = 0x0a;
                                        break;
                                    case "11":
                                        temp = 0x0b;
                                        break;
                                    case "12":
                                        temp = 0x0c;
                                        break;
                                    case "13":
                                        temp = 0x0d;
                                        break;
                                    case "14":
                                        temp = 0x0e;
                                        break;
                                    case "15":
                                        temp = 0x0f;
                                        break;
                                    default:
                                        temp = 0x01;
                                        break;
                                }
//                                byte[] buf =numbers[0].getBytes();
                                final byte[] buf = new byte[1];
                                buf[0] = temp;
//                                byte[] buf = ;
                                mBleController.writeBuffer_Device(device, UUIDs.UUID_ENVIRONMENT_Service, UUIDs.UUID_ENVIRONMENT_Char_Write,buf, new OnWriteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "修改速率="+numbers[0]+"  "+buf[0]+"成功");
                                    }

                                    @Override
                                    public void onFailed(int state) {
                                        Log.d(TAG, "修改速率="+numbers[0]+"失败");
                                    }
                                });
                                break;
                            }
                        }

                    }
                });
                builder.create().show();
            }
        });

        getContext().registerReceiver(Receiver, makeIntentFilter());
        return v;
    }

    /**
     * 获取已连接设备
     */
    private void getConnected() {
        conn_btDevices.clear();
        conn_device_hashmap = mBleController.getConnectedDvices();

        for(Map.Entry<BluetoothDevice, BluetoothGatt> entry : conn_device_hashmap.entrySet()){
            String name = entry.getKey().getName()==null?"未知设备":entry.getKey().getName();
            String addr = entry.getKey().getAddress();
            Map<String, String> map = new HashMap<>();
            map.put("名称",name);
            map.put("地址",addr);
            conn_btDevices.add(map);
        }
    }

    @Override
    public void onResume() {
        getConnected();
        adapter.notifyDataSetChanged();
        Log.d(TAG, "onResume0");
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onResume1");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onResume2");
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(Receiver);
        super.onDestroy();
    }

    private final BroadcastReceiver Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleController.ACTION_CONNDEVICE_CHANGE.equals(action)) {
                getConnected();
                adapter.notifyDataSetChanged();
            }
        }
    };


    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleController.ACTION_CONNDEVICE_CHANGE);
        return intentFilter;
    }
}
