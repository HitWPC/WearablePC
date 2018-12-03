package com.clj.fastble.callback;



import java.util.List;

import com.clj.fastble.data.BleDevice;

public abstract class BleScanCallback implements BleScanPresenterImp {

    public abstract void onScanFinished(List<BleDevice> scanResultList);

    public void onLeScan(BleDevice bleDevice) {
    }
}
