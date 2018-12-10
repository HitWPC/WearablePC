package cn.hitftcl.wearablepc.Bluetooth;

import java.util.LinkedList;

import cn.hitftcl.wearablepc.Model.ECGModel;

/**
 * Created by Administrator on 2018/12/6.
 */

public class ECGLinkedList {

    public static LinkedList<ECGModel> ecgModelList = new LinkedList<>();

    public static synchronized void add(ECGModel ecgModel){
        while(ecgModelList.size()>60){
            ecgModelList.poll();
        }
        ecgModelList.offer(ecgModel);
    }

    public static synchronized  LinkedList<ECGModel> getEcgModelList(){
        LinkedList<ECGModel> linkedList =  new LinkedList<>();
        linkedList.addAll(ecgModelList);
        return linkedList;
    }
}
