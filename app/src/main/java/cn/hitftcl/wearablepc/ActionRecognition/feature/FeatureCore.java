package cn.hitftcl.wearablepc.ActionRecognition.feature;

import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import cn.hitftcl.wearablepc.ActionRecognition.model.FeaVector;
import cn.hitftcl.wearablepc.ActionRecognition.common.FileOperateUtil;
import cn.hitftcl.wearablepc.ActionRecognition.common.RecognitionType;
import cn.hitftcl.wearablepc.ActionRecognition.classifier.Center;
import cn.hitftcl.wearablepc.ActionRecognition.classifier.SVM;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.Service.ActionAdaptiveService;
import cn.hitftcl.wearablepc.Service.ActionOriginService;

/**
 * Created by hzf on 2018/2/25.
 * 提取特征
 */

public class FeatureCore {

    //窗口大小，0表示整个序列作为一个窗口
    public static final int WIN_SIZE = 0;

    //窗口移动步数
    public static final int STEP_SIZE = 0;

    //标准化时用到的方差
    public static ArrayList<Double> std;

    //标准化时用到的均值
    public static ArrayList<Double> mean;

    private static ArrayList<Integer> idList = new ArrayList<>();

    private static LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());;

    /**
     * 获取特征
     * @param arr
     * @param axis
     * @return
     */
    public static ArrayList<Double> getFeature(ArrayList<Double> arr, String axis){

        return new FeatureCalculate(arr).allFeature();
    }


    /**
     * 提取时序特征
     * @param seq 时序数据
     * @param winSize 窗口大小
     * @param stepSize 窗口移动大小
     * @param axis 哪一轴
     * @return 特征列表
     */
    public static ArrayList<Double> sequenceFeature(ArrayList<Double> seq, int winSize, int stepSize, String axis) {
//        LogUtil.d(FeatureCore.class.getSimpleName(), axis + " = " + seq.toString());
        //无窗口
        if(winSize == 0){
            return getFeature(seq, axis);
        }
        //有窗口，暂时没用到
        ArrayList<Double> featureMat = new ArrayList<>();
        int len = seq.size();

        int j = 0;
        while(j < len - stepSize){
            ArrayList<Double> window = new ArrayList<>();
            for(int i = j; i < j + winSize; j++){
                window.add(seq.get(i));
            }

            featureMat.addAll(getFeature(window, axis));
            j += stepSize;
            window.clear();
        }
        return featureMat;
    }


    /**
     * 计算三轴加速度数据的特征并保存到数据库
     * @param saveDB 是否将特征向量存入数据库
     * @param type 采用的算法类型
     * @param xList x轴原始数据
     * @param yList y轴原始数据
     * @param zList z轴原始数据
     * @param startTime 第一个数据产生的时间
     * @param endTime 最后一个数据产生的时间
     */
    public static int calculateFeature(boolean saveDB, final int type, ArrayList<Double> xList, ArrayList<Double> yList, ArrayList<Double> zList, long startTime, long endTime){
        long start = System.currentTimeMillis();
        final ArrayList<Double> combineList = new ArrayList<>();
        combineList.addAll(sequenceFeature(xList, WIN_SIZE, STEP_SIZE, "x"));
        combineList.addAll(sequenceFeature(yList, WIN_SIZE, STEP_SIZE, "y"));
        combineList.addAll(sequenceFeature(zList, WIN_SIZE, STEP_SIZE, "z"));
        combineList.addAll(FeatureCalculate.timeCov(xList, yList, zList));
//        LogUtil.d(FeatureCore.class.getSimpleName(), "fea = " + combineList.toString());

        normlize(combineList, type);

        //预分类
        int centerResult = 0;
        if(type == RecognitionType.ALGORITHM_ADAPTIVE){
            //预分类
            centerResult = Center.classify(combineList);
            Log.d("test-与分类", centerResult+"");
        }
        if(saveDB) {
            FileOperateUtil.timeList.add("feature:" + (System.currentTimeMillis() - start));
        }

        //广播通知当前用户状态
//        if(centerResult != 0) {
//            Intent intent = new Intent("com.example.hzf.recognition.ADAPTIVE");
//            intent.putExtra("result", centerResult);
//            localBroadcastManager.sendBroadcast(intent);
//            LogUtil.d(FeatureCore.class.getSimpleName(), "broadcast sent");
//        }

        //特征存储数据库
        int saveId = 0;
        if(saveDB) {
            Log.d(FeatureCore.class.getSimpleName(), "startTime = " + startTime + ",endTime = " + endTime);
            final FeaVector feaVector = new FeaVector(null, startTime, endTime);
            if (centerResult != 0) {
                feaVector.setCategory(centerResult);
                feaVector.setOrigin(centerResult);
            }
            feaVector.save();
            saveId = feaVector.getId();
        }

        //原始算法
        if(type == RecognitionType.ALGORITHM_ORIGIN) {
            final int finalSaveId1 = saveId;
            SVM.exec.execute(new Runnable() {
                @Override
                public void run() {
                    SVM.classify(combineList, finalSaveId1, ActionOriginService.SAMPLE_FREQ);
                }
            });
        }
        else if(type == RecognitionType.ALGORITHM_ADAPTIVE){
            if(centerResult == 0){
                final int finalSaveId = saveId;
                SVM.exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        SVM.classify(combineList, finalSaveId, ActionAdaptiveService.BEST_FREQ);
                    }
                });
            }
        }
        return saveId;
    }

    /**
     * 标准化
     * @param combineList
     */
    public static void normlize(ArrayList<Double> combineList, int type){
        String normName = "";
        if(type == RecognitionType.ALGORITHM_ORIGIN){
            normName = "norm50.txt";
        }else if(type == RecognitionType.ALGORITHM_ADAPTIVE){
            normName = "norm20.txt";
        }
        //标准化
        if(mean == null || mean.size() == 0 || std == null || std.size() == 0){
            mean = new ArrayList<>();
            std = new ArrayList<>();
            //读取标准化的参数
            try {
                File f = new File(Environment.getExternalStorageDirectory() + "/ARecognition", normName);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                String readLine = "";
                readLine = bufferedReader.readLine();
                if(!readLine.isEmpty()){
                    String[] split = readLine.split(",");
                    for(int i = 0; i < 36; i++){
                        mean.add(Double.valueOf(split[i]));
                    }
                }
                readLine = bufferedReader.readLine();
                if(!readLine.isEmpty()){
                    String[] split = readLine.split(",");
                    for(int i = 0; i < 36; i++){
                        std.add(Double.valueOf(split[i]));
                    }
                }
                bufferedReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        for(int i = 0; i < combineList.size(); i++){
            double norm = (combineList.get(i) - mean.get(i)) / std.get(i);
            combineList.set(i, norm);
        }
//        LogUtil.d(FeatureCore.class.getSimpleName(), "norm = " + combineList.toString());
    }
}
