package cn.hitftcl.wearablepc.ActionRecognition.classifier;

import android.os.Environment;
import android.util.Log;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.hitftcl.wearablepc.ActionRecognition.model.FeaVector;
import cn.hitftcl.wearablepc.ActionRecognition.common.FileOperateUtil;
import cn.hitftcl.wearablepc.Service.ActionOriginService;

//import umich.cse.yctung.androidlibsvm.LibSVM;

/**
 * Created by hzf on 2018/2/27.
 * 支持向量机
 */

public class SVM {

    //可缓存线程池
    public static final Executor exec = Executors.newCachedThreadPool();

    //活动类别数
    public static final int NUM_OF_ACTIVITY = 8;


    /**
     * 运行大量计算任务并将结果保存到数据库
     */
    public static void calculate(ArrayList<Integer> copyList) {
        Log.d(SVM.class.getSimpleName(), copyList.toString());
        //将待分类数据保存到文件
        FileOperateUtil.writeFeaVector(copyList);
        //分类
        forecast(ActionOriginService.SAMPLE_FREQ);
        //读取文件并记录分类结果
        FileOperateUtil.readResult(copyList);
    }

    /**
     * 分类
     * @param list 特征
     * @param id 该条特征的id
     */
    public static void classify(ArrayList<Double> list, int id, int freq) {
        long flag = System.currentTimeMillis();
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "test.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            StringBuilder builder = new StringBuilder();
            builder.append(1);
            for (int j = 0; j < list.size(); j++) {
                builder.append(" ").append(j).append(":").append(list.get(j));
            }

            writer.write(builder.toString());
            writer.write("\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        forecast(freq);

        if(id != 0) {
            try {
                File f = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "result.txt");
                BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                String readLine = "";
                while ((readLine = bufferedReader.readLine()) != null) {
                    if (!readLine.isEmpty()) {
                        int res = (int) Double.parseDouble(readLine);
                        //记录分类结果
                        FeaVector feaVector = new FeaVector();
                        feaVector.setCategory(res);
                        feaVector.setOrigin(res);
                        feaVector.update(id);
                        Log.d("test",feaVector.toString());
                    }
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileOperateUtil.timeList.add("classify:1:" + (System.currentTimeMillis() - flag));
        }
    }

    /**
     * 预测活动类别
     */
    public static void forecast(int freq){
        String modelName = "";

        if(freq == ActionOriginService.SAMPLE_FREQ){
            modelName = "model50.txt";}
//        else if(freq == AdaptiveService.BEST_FREQ){
//            modelName = "model20.txt";
//        }
//        LibSVM svm = LibSVM.getInstance();
//        String appFolderPath = Environment.getExternalStorageDirectory() + "/ARecognition/";
//        //若还未训练出SVM模型
//        File f = new File(appFolderPath + "model.txt");
//        if(!f.exists()){
//            svm.train("-c 256.0 -g 0.125 -v 10 " + appFolderPath + "feature.txt " + appFolderPath + "model.txt");
//        }
//        svm.predict(appFolderPath + "test.txt " + appFolderPath + "model.txt " + appFolderPath + "result.txt");
        String appFolderPath = Environment.getExternalStorageDirectory() + "/ARecognition/";
        //"-t 0"表示使用线性核
        String[] testArgs = {appFolderPath + "test.txt", appFolderPath + modelName, appFolderPath + "result.txt"};

//        //首次scale
//        File scaleFile = new File(appFolderPath + "scale.txt");
//        if(!scaleFile.exists()){
//            svm_scale scale = new svm_scale();
//            String[] scaleArgs = {"-s", appFolderPath + "scale.txt", appFolderPath + "feature.txt"};
//            try {
//                scale.main(scaleArgs);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        //标准化
//        svm_scale scale = new svm_scale();
//        String[] scaleArgs = {"-r", appFolderPath + "scale.txt", appFolderPath + "test.txt"};
//        try {
//            scale.main(scaleArgs);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        svm_predict predict = new svm_predict();
        try {
            predict.main(testArgs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 训练20Hz和50Hz两种模型
     */
    public static void train(){
        String appFolderPath = Environment.getExternalStorageDirectory() + "/ARecognition/";
        //"-t 0"表示使用线性核
        String[] trainArgs = {"-t", "0", appFolderPath + "feature50.txt", appFolderPath + "model50.txt"};
        //若还未训练出SVM模型
        File f = new File(appFolderPath + "model50.txt");
        if(!f.exists()) {
            svm_train train = new svm_train();
            try {
                train.main(trainArgs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //"-t 0"表示使用线性核
        String[] trainArgs10 = {"-t", "0", appFolderPath + "feature20.txt", appFolderPath + "model20.txt"};
        //若还未训练出SVM模型
        File f10 = new File(appFolderPath + "model20.txt");
        if(!f10.exists()) {
            svm_train train10 = new svm_train();
            try {
                train10.main(trainArgs10);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
