package cn.hitftcl.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cn.hitftcl.ble.callback.BleDeviceScanCallback;
import cn.hitftcl.ble.callback.ConnectCallback;
import cn.hitftcl.ble.callback.OnReceiverCallback;
import cn.hitftcl.ble.callback.OnWriteCallback;
import cn.hitftcl.ble.callback.ScanCallback;
import cn.hitftcl.ble.request.ReceiverRequestQueue;

/**
 * Description：
 * Author: Hansion  www.hansion.win
 * Time: 2017/2/13 9:43
 */
public class BleController {

    private static String LOGTAG = "H_Ble_Lib";
    public static final String  TAG = "debug001";

    //BleController实例
    private static BleController sBleManager;
    private Context mContext;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mAdapter;
    //BluetoothDevice和Gatt的映射
    private HashMap<BluetoothDevice,BluetoothGatt> ConnectedDvices = new HashMap<>();

    private BluetoothGatt mBluetoothGatt;
    private HashMap<BluetoothDevice,BluetoothGattCharacteristic> DevicesGattChar = new HashMap<>();
    private BluetoothGattCharacteristic gattCharacteristic;

    private BleGattCallback mGattCallback;
    private OnWriteCallback writeCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    //发起连接是否有响应
    private boolean isConnectResponse = false;
    //获取到所有服务的集合
    private HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap = new HashMap<>();
    //保存所有设备的所有服务
    private HashMap<BluetoothGatt, HashMap<String, Map<String, BluetoothGattCharacteristic>>> devicesServicesMap = new HashMap<>();
    //默认扫描时间：5s
    private static final int SCAN_TIME = 5000;
    //默认连接超时时间:6s
    private static final int CONNECTION_TIME_OUT = 10000;
    //是否是用户手动断开
//    private boolean isBreakByMyself = false;
    //连接结果的回调
    private ConnectCallback connectCallback;
    //读操作请求队列
    private ReceiverRequestQueue mReceiverRequestQueue = new ReceiverRequestQueue();
    //此属性一般不用修改
    private static final String BLUETOOTH_NOTIFY_D = "00002902-0000-1000-8000-00805f9b34fb";
    //接收到数据的广播
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_UUID =
            "com.example.bluetooth.le.EXTRA_UUID";
    public final static String ACTION_CONNDEVICE_CHANGE =
            "com.example.bluetooth.le.ACTION_CONNDEVICE_CHANGE";


    //-----------------------------  对外公开的方法 ----------------------------------------------



    /**
     * 获取BleController实例对象
     * @return
     */
    public synchronized static BleController getInstance() {
        if (null == sBleManager) {
            sBleManager = new BleController();
        }
        return sBleManager;
    }


    /**
     * 进行初始化
     * @param context
     * @return
     */
    public BleController init(Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (null == mBluetoothManager) {
                Log.e(LOGTAG, "BluetoothManager init error!");
            }

            mAdapter = mBluetoothManager.getAdapter();
            if (null == mAdapter) {
                Log.e(LOGTAG, "BluetoothManager init error!");
            }

            mGattCallback = new BleGattCallback();
        }
        return this;
    }


    /**
     * 扫描设备
     * 当传入的time值为0以下时默认扫描时间为5秒
     */
    public void scanBle(int time, final ScanCallback scanCallback) {
        if (!isEnable()) {
            mAdapter.enable();
            Log.e(LOGTAG, "Bluetooth is not open!");
        }
//        if (null != mBluetoothGatt) {
//            mBluetoothGatt.close();
//        }
        reset();
        final BleDeviceScanCallback bleDeviceScanCallback = new BleDeviceScanCallback(scanCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //time后停止扫描
                mAdapter.stopLeScan(bleDeviceScanCallback);
                scanCallback.onSuccess();
            }
        }, time <= 0 ? SCAN_TIME : time);
        mAdapter.startLeScan(bleDeviceScanCallback);
    }


    /**
     * 连接设备
     *
     * @param connectionTimeOut 连接超时时间,默认是6秒.当赋值为0或更小值时用默认值
     * @param devicesAddress    想要连接的设备地址
     */
    public void connect(final int connectionTimeOut, final String devicesAddress, ConnectCallback connectCallback) {
        BluetoothDevice remoteDevice = mAdapter.getRemoteDevice(devicesAddress);
        if (null == remoteDevice) {
            Log.e(LOGTAG, "No device found at this address：" + devicesAddress);
            return;
        }

        this.connectCallback = connectCallback;

//        if (null != mBluetoothGatt) {
//            mBluetoothGatt.close();
//        }
        reset();

        mBluetoothGatt = remoteDevice.connectGatt(mContext, true, mGattCallback);//自动连接

        ConnectedDvices.put(remoteDevice,mBluetoothGatt);
        Log.e(LOGTAG, "connecting mac-address:" + devicesAddress);
        delayConnectResponse(connectionTimeOut,remoteDevice);
    }


    /**
     * 发送数据
     *
     * @param buf
     * @param writeCallback
     */
    public void writeBuffer(byte[] buf, OnWriteCallback writeCallback) {
        this.writeCallback = writeCallback;
        if (!isEnable()) {
            writeCallback.onFailed(OnWriteCallback.FAILED_BLUETOOTH_DISABLE);
            Log.e(LOGTAG, "FAILED_BLUETOOTH_DISABLE");
            return;
        }

        if (gattCharacteristic == null) {
//            gattCharacteristic = getBluetoothGattCharacteristic(BLUETOOTH_S, BLUETOOTH_WRITE_C);
        }

        if (null == gattCharacteristic) {
            writeCallback.onFailed(OnWriteCallback.FAILED_INVALID_CHARACTER);
            Log.e(LOGTAG, "FAILED_INVALID_CHARACTER");
            return;
        }

        //设置数组进去
        gattCharacteristic.setValue(buf);

        //发送
        boolean b = mBluetoothGatt.writeCharacteristic(gattCharacteristic);

        Log.e(LOGTAG, "send:" + b + "data：" + bytesToHexString(buf));
    }

    /**
     * 发送数据
     * @param bluetoothDevice
     * @param buf
     * @param writeCallback
     */

    public void writeBuffer_Device(BluetoothDevice bluetoothDevice, String serviceUUID, String characterUUID, byte[] buf, OnWriteCallback writeCallback) {
        this.writeCallback = writeCallback;
        if (!isEnable()) {
            writeCallback.onFailed(OnWriteCallback.FAILED_BLUETOOTH_DISABLE);
            Log.e(LOGTAG, "FAILED_BLUETOOTH_DISABLE");
            return;
        }
        BluetoothGatt gatt = ConnectedDvices.get(bluetoothDevice);
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(bluetoothDevice, serviceUUID, characterUUID);

        if (null == characteristic) {
            writeCallback.onFailed(OnWriteCallback.FAILED_INVALID_CHARACTER);
            Log.e(LOGTAG, "FAILED_INVALID_CHARACTER");
            return;
        }

        //设置数组进去
        characteristic.setValue(buf);


        //发送
        boolean b = false;
        if(gatt!=null){
            b = gatt.writeCharacteristic(characteristic);
        }else{
            Log.e(LOGTAG, "*************************gatt is null");
        }


        Log.e(LOGTAG, "send:" + b + "data：" + bytesToHexString(buf));
    }

    /**
     * 返回已连接设备的HashMap
     * @return
     */
    public HashMap<BluetoothDevice,BluetoothGatt> getConnectedDvices(){
        return  ConnectedDvices;
    }

    /**
     * 返回设备和监听的属性
     * @return
     */
    public HashMap<BluetoothDevice, BluetoothGattCharacteristic> getDevicesGattChar(){
        return DevicesGattChar;
    }



    /**
     * 设置读取数据的监听
     *
     * @param requestKey
     * @param onReceiverCallback
     */
    public void registReciveListener(String requestKey, OnReceiverCallback onReceiverCallback) {
        mReceiverRequestQueue.set(requestKey, onReceiverCallback);
    }

    /**
     * 移除读取数据的监听
     *
     * @param requestKey
     */
    public void unregistReciveListener(String requestKey) {
        mReceiverRequestQueue.removeRequest(requestKey);
    }

    /**
     * 手动断开Ble连接
     */
    public void closeBleConn(BluetoothDevice bluetoothDevice) {
        disConnection(bluetoothDevice);
//        isBreakByMyself = true;
        gattCharacteristic = null;
//        mBluetoothManager = null;
    }







    //----------------------------------  私有方法 ----------------------------------------------


    /**
     * 将byte数组转为16进制字符串 此方法主要目的为方便Log的显示
     */
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv.toUpperCase()).append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * 当前蓝牙是否打开
     */
    private boolean isEnable() {
        if (null != mAdapter) {
            return mAdapter.isEnabled();
        }
        return false;
    }


    /**
     * 复位
     */
    private void reset() {
        isConnectResponse = false;
        servicesMap.clear();
    }

    /**
     * 如果连接connectionTimeOut时间后还没有响应,手动关掉连接.
     *
     * @param connectionTimeOut
     */
    private void delayConnectResponse(int connectionTimeOut, final BluetoothDevice bluetoothDevice) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isConnectResponse) {
                    Log.e(LOGTAG, "connect timeout");
                    disConnection(bluetoothDevice);
//                    reConnect();
                }
//                else {
//                    isBreakByMyself = false;
//                }
            }
        }, connectionTimeOut <= 0 ? CONNECTION_TIME_OUT : connectionTimeOut);
    }


    /**
     * 断开连接
     */
    private void disConnection(BluetoothDevice bluetoothDevice) {
        if (null == mAdapter || null == mBluetoothGatt) {
            Log.e(LOGTAG, "disconnection error maybe no init");
            return;
        }
        BluetoothGatt bluetoothGatt = ConnectedDvices.get(bluetoothDevice);
        devicesServicesMap.remove(bluetoothGatt);
        bluetoothGatt.disconnect();
        ConnectedDvices.remove(bluetoothDevice);
        reset();
    }




    /**
     * 蓝牙GATT连接及操作事件回调
     */
    private class BleGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
//                isBreakByMyself = false;
                Log.d(LOGTAG,"onConnectionStateChange  success");
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {   //断开连接
                //断开连接后从已连接列表中删除该Device
                for(Map.Entry<BluetoothDevice, BluetoothGatt> entry : ConnectedDvices.entrySet()){
                    if(entry.getValue().equals(gatt)){
                        entry.getValue().close();
                        ConnectedDvices.remove(entry.getKey());
                        break;
                    }
                }
//                if (!isBreakByMyself) {
//                    reConnect();
//                }
                reset();
            }
            broadcastUpdate(ACTION_CONNDEVICE_CHANGE);
        }

        //服务被发现了
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (null != mBluetoothGatt && status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = mBluetoothGatt.getServices();
                int serviceSize = services.size();
                Log.d(LOGTAG,"onServicesDiscovered :"+serviceSize);
                for (int i = 0; i < serviceSize; i++) {
                    HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
                    BluetoothGattService bluetoothGattService = services.get(i);
                    String serviceUuid = bluetoothGattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    int characteristicSize = characteristics.size();
                    Log.d(LOGTAG,"Service "+i+":  "+characteristicSize+"   service uuid :"+serviceUuid);
                    for (int j = 0; j < characteristicSize; j++) {
                        Log.d(LOGTAG,"characteristic:  "+ characteristics.get(j).getUuid().toString());
                        charMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
                        if (characteristics.get(j).getUuid().toString().equals(UUIDs.UUID_ENVIRONMENT_Char_Notify)
                                || characteristics.get(j).getUuid().toString().equals(UUIDs.UUID_BD_Char)) {
                            if (enableNotification(true, characteristics.get(j))) {
                                isConnectResponse = true;
                                connSuccess();
                            } else {
                                reConnect();
                            }
                        }
                    }
                    servicesMap.put(serviceUuid, charMap);
                }
                devicesServicesMap.put(gatt, servicesMap);
                // TODO　打印搜索到的服务
//                printServices(mBluetoothGatt);
            }
        }

        //收到数据
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//            if (null != mReceiverRequestQueue) {
//                HashMap<String, OnReceiverCallback> map = mReceiverRequestQueue.getMap();
//                final byte[] rec = characteristic.getValue();
//                Log.d(TAG, characteristic.getStringValue(0));
//                for (String key : mReceiverRequestQueue.getMap().keySet()) {
//                    final OnReceiverCallback onReceiverCallback = map.get(key);
//                    runOnMainThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            onReceiverCallback.onRecive(rec);
//                        }
//                    });
//                }
//            }
        }

        //描述符被写了
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }

        //描述符被读了
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }

        //发送数据结果
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (null != writeCallback) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onSuccess();
                        }
                    });
                    Log.d(TAG, "Send data success!");
                } else {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onFailed(OnWriteCallback.FAILED_OPERATION);
                        }
                    });
                    Log.d(TAG, "Send data failed!");
                }
            }
        }
    }


    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || characteristic == null)
            return false;
        if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable))
            return false;
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_NOTIFY_D));
        if (clientConfig == null)
            return false;

        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBluetoothGatt.writeDescriptor(clientConfig);
    }


    //打印所有搜索到的服务 此方法用于硬件方未提供UUID时查询所有UUID
    private void printServices(BluetoothGatt gatt) {
        if (gatt != null) {
            Iterator i$ = gatt.getServices().iterator();

            while (i$.hasNext()) {
                BluetoothGattService service = (BluetoothGattService) i$.next();
                Log.i(LOGTAG, "service: " + service.getUuid());
                Iterator i$1 = service.getCharacteristics().iterator();

                while (i$1.hasNext()) {
                    BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) i$1.next();
                    Log.d("LOGTAG", "  characteristic: " + characteristic.getUuid() + " value: " + Arrays.toString(characteristic.getValue()));
                    Iterator i$2 = characteristic.getDescriptors().iterator();

                    while (i$2.hasNext()) {
                        BluetoothGattDescriptor descriptor = (BluetoothGattDescriptor) i$2.next();
                        Log.v("LOGTAG", "        descriptor: " + descriptor.getUuid() + " value: " + Arrays.toString(descriptor.getValue()));
                    }
                }
            }
        }
    }

    /**
     * 根据服务UUID和特征UUID,获取一个特征{@link BluetoothGattCharacteristic}
     *
     * @param serviceUUID   服务UUID
     * @param characterUUID 特征UUID
     */
    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(BluetoothDevice bluetoothDevice, String serviceUUID, String characterUUID) {
        if (!isEnable()) {
            throw new IllegalArgumentException(" Bluetooth is no enable please call BluetoothAdapter.enable()");
        }
//        if (null == mBluetoothGatt) {
//            Log.e(LOGTAG, "mBluetoothGatt is null");
//            return null;
//        }
        BluetoothGatt bluetoothGatt = ConnectedDvices.get(bluetoothDevice);
        if(bluetoothGatt==null){
            Log.d(TAG, "设备已经断开连接");
            return null;
        }

        HashMap<String, Map<String, BluetoothGattCharacteristic>> ServicesMap = devicesServicesMap.get(bluetoothGatt);
        if(ServicesMap==null){
            return null;
        }

        //找特定服务
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = ServicesMap.get(serviceUUID);
        if (null == bluetoothGattCharacteristicMap) {
            Log.e(LOGTAG, "Not found the serviceUUID!");
            return null;
        }

        //找特定特征
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
        BluetoothGattCharacteristic gattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            if (characterUUID.equals(entry.getKey())) {
                gattCharacteristic = entry.getValue();
                break;
            }
        }
        return gattCharacteristic;
    }

    private void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            if (mHandler != null) {
                mHandler.post(runnable);
            }
        }
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    // TODO 此方法断开连接或连接失败时会被调用。可在此处理自动重连,内部代码可自行修改，如发送广播
    private void reConnect() {
        if(connectCallback != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    connectCallback.onConnFailed();
                }
            });
        }

        Log.e(LOGTAG, "Ble disconnect or connect failed!");
    }

    // TODO 此方法Notify成功时会被调用。可在通知界面连接成功,内部代码可自行修改，如发送广播
    private void connSuccess() {
        if(connectCallback != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    connectCallback.onConnSuccess();
                }
            });
        }
        Log.e(LOGTAG, "Ble connect success!");
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, data);
            intent.putExtra(EXTRA_UUID,characteristic.getUuid().toString());
        }
        mContext.sendBroadcast(intent);
    }

}
