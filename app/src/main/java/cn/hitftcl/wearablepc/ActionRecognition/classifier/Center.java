package cn.hitftcl.wearablepc.ActionRecognition.classifier;

import android.os.Environment;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * 预分类算法
 * Created by hzf on 2018/4/28.
 */

public class Center {
    public static double[][] centerVec;
    public static double[] threshold;

    public static int classify(ArrayList<Double> list){
        readParameter();

        //预分类
        int minIndex = 0;
        double min = Double.MAX_VALUE;
        for(int i = 1; i < centerVec.length; i++){
            double[] vec = centerVec[i];
            double dist = 0;
            for(int j = 0; j < list.size(); j++){
//                LogUtil.d(Center.class.getSimpleName(), "vec[" + j + "] = " + vec[j]);
//                LogUtil.d(Center.class.getSimpleName(), "list[" + j + "] = " + list.get(j));

                dist += (list.get(j) - vec[j]) * (list.get(j) - vec[j]);
            }
            dist = Math.sqrt(dist);
            if(dist < min){
                minIndex = i;
                min = dist;
            }
            Log.d(Center.class.getSimpleName(), "dist " + i + " = " + dist);
        }
        return min < threshold[minIndex] ? minIndex : 0;
        //预分类dist结果不理想，目前将识别率较低的三个活动用SVM
//        if(minIndex != 4 && minIndex != 5 && minIndex != 8) {
//            return minIndex;
//        }else{
//            return 0;
//        }
    }

    /**
     * 读取中心点和阈值
     */
    public static void readParameter(){
        //读取参数文件
        if(centerVec == null || threshold == null){
            centerVec = new double[9][36];
            threshold = new double[9];
            try {
                File f = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "center20.txt");
                BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                String readLine = "";
                while ((readLine = bufferedReader.readLine()) != null) {
                    if (!readLine.isEmpty()) {
                        String[] split = readLine.split(" ");
                        String[] vec = split[1].split(",");
                        double[] format = new double[vec.length];
                        for (int i = 0; i < format.length; i++) {
                            format[i] = Double.parseDouble(vec[i]);
                        }
                        centerVec[Integer.parseInt(split[0])] = format;
                    }
                }
                bufferedReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                File f = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "threshold20_90per.txt");
                BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                String readLine = "";
                while((readLine = bufferedReader.readLine()) != null) {
                    if (!readLine.isEmpty()) {
                        String[] split = readLine.split(" ");
                        threshold[Integer.parseInt(split[0])] = Double.parseDouble(split[1]);
                    }
                }
                bufferedReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
