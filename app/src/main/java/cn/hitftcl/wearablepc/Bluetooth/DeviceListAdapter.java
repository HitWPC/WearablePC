package cn.hitftcl.wearablepc.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import cn.hitftcl.wearablepc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/2/13 11:23
 */
public class DeviceListAdapter extends BaseAdapter {
    public final static String TAG = "debug001";

    private List<BleDevice> bleDeviceList;
    private Context mContext;

    public DeviceListAdapter(Context context) {
        this.mContext = context;
        bleDeviceList = new ArrayList<>();
    }

    public DeviceListAdapter(Context context, List<BleDevice> bluetoothDevices) {
        Log.d(TAG, "DeviceListAdapter的构造函数");
        mContext = context;
        this.bleDeviceList = bluetoothDevices;
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        if (position > bleDeviceList.size())
            return null;
        return bleDeviceList.get(position);
    }


    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        BaseViewHolder holder = BaseViewHolder.
                getViewHolder(mContext, convertView, viewGroup, R.layout.item_device_list, position);
        TextView name = holder.getView(R.id.mDeviceName);
        TextView address = holder.getView(R.id.mDeviceMacAddress);
        name.setText(bleDeviceList.get(position).getName()==null?"未知设备":bleDeviceList.get(position).getName());
        address.setText(bleDeviceList.get(position).getMac());
        return holder.getConvertView();
    }

    public void addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearScanDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

}
