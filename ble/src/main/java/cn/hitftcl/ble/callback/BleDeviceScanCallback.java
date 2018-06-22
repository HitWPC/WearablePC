package cn.hitftcl.ble.callback;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * Description： ble蓝牙设备扫描回调
 * Author: Hansion
 * Time: 2016/10/11 12:21
 */
public class BleDeviceScanCallback implements BluetoothAdapter.LeScanCallback {
    private ScanCallback mScanCallback;

    public BleDeviceScanCallback(ScanCallback scanCallback) {
        this.mScanCallback = scanCallback;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (null != mScanCallback) {

            mScanCallback.onScanning(device, rssi, scanRecord);
        }
        Log.d("@@@@@@@@@@","        "+device.getName()+"  "+device.getAddress());
    }
}