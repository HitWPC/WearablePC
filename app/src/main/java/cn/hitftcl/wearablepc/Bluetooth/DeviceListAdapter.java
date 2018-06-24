package cn.hitftcl.wearablepc.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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

    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private Context mContext;

    public DeviceListAdapter(Context context, List<BluetoothDevice> bluetoothDevices) {
        Log.d(TAG, "DeviceListAdapter的构造函数");
        mContext = context;
        this.bluetoothDevices = bluetoothDevices;
    }

    @Override
    public int getCount() {
        return bluetoothDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return bluetoothDevices.get(i);
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
        name.setText(bluetoothDevices.get(position).getName()==null?"未知设备":bluetoothDevices.get(position).getName());
        address.setText(bluetoothDevices.get(position).getAddress());
        return holder.getConvertView();
    }
}
