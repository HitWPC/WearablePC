package cn.hitftcl.wearablepc.ActionRecognition.common;

import android.os.Environment;
import android.util.Log;


import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.hitftcl.wearablepc.ActionRecognition.model.Accelerometer;
import cn.hitftcl.wearablepc.ActionRecognition.model.FeaVector;
import cn.hitftcl.wearablepc.ActionRecognition.model.Gyroscope;
import cn.hitftcl.wearablepc.ActionRecognition.model.Magnetometer;
import cn.hitftcl.wearablepc.ActionRecognition.model.MemoryTime;


/**
 * Created by hzf on 2018/3/3.
 */

public class FileOperateUtil {
    public static final int GROUP_NUM = 60;
    public static ArrayList<String> timeList = new ArrayList<>();
    /**
     * 创建文件夹
     */
    public static void createDirs(){
        File file = new File(Environment.getExternalStorageDirectory() + "/ARecognition");
        if(!file.exists()){
            file.mkdirs();
        }
        File sonFile = new File(Environment.getExternalStorageDirectory() + "/ARecognition/raw data");
        if(!sonFile.exists()){
            sonFile.mkdirs();
        }
    }

    /**
     * 将特征写入文件作为SVM的输入
     */
    public static void writeFeaVector(ArrayList<Integer> idList){
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "test.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            Iterator<Integer> iterator = idList.iterator();
            while(iterator.hasNext()) {

                ArrayList<Double> list = DataSupport.find(FeaVector.class, iterator.next()).getFeature();
                StringBuilder builder = new StringBuilder();
                builder.append(1);
                for(int j = 0; j < list.size(); j++){
                    builder.append(" ").append(j).append(":").append(list.get(j));
                }

                writer.write(builder.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 将测试数据加入训练数据集
     * @param mData
     */
    public static void appendFeaVector(List<FeaVector> mData){
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "feature.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            Iterator<FeaVector> iterator = mData.iterator();

            while (iterator.hasNext()){
                FeaVector feaVector = iterator.next();
                ArrayList<Double> list = feaVector.getFeature();
                StringBuilder builder = new StringBuilder();
                builder.append(feaVector.getOrigin());
                for(int j = 0; j < list.size(); j++){
                    builder.append(" ").append(j).append(":").append(list.get(j));
                }
                writer.write(builder.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 将所有数据的特征写入文件
     * @param list
     */
    public static void printFeaVector(List<FeaVector> list){
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "aaa.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for(int i = 0; i < list.size(); i++){
                FeaVector feaVector = list.get(i);
                StringBuilder builder = new StringBuilder();
                builder.append(feaVector.getCategory());
                ArrayList<Double> feature = feaVector.getFeature();
                for(int j = 0; j < feature.size(); j++){
                    builder.append(",").append(feature.get(j));
                }

                writer.write(builder.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 将传感器数据写入文件
     */
//    public static void printSensorData(int label, int id, List<Accelerometer> mAcc, List<Gyroscope> mGyro, List<Magnetometer> mMag){
//        //生成文件夹
//        String parent = Environment.getExternalStorageDirectory() + "/ARecognition/raw data/" + RecognitionActivity.activityName(label)+"/exp" + id;
//        File path = new File(parent);
//        if(!path.exists()){
//            path.mkdirs();
//        }
//        try{
//            File file = new File(parent, "acc.txt");
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//            for(int i = 0; i < mAcc.size(); i++){
//                Accelerometer accelerometer = mAcc.get(i);
//                StringBuilder builder = new StringBuilder();
//                builder.append(accelerometer.getX()).append(",").append(accelerometer.getY()).append(",").append(accelerometer.getZ()).append(",")
//                        .append(new SimpleDateFormat("MM-dd HH:mm:ss").format(accelerometer.getTime()));
//
//                writer.write(builder.toString());
//                writer.write("\n");
//            }
//            writer.flush();
//            writer.close();
//
//            file = new File(parent, "gyro.txt");
//            writer = new BufferedWriter(new FileWriter(file));
//            for(int i = 0; i < mGyro.size(); i++){
//                Gyroscope gyroscope = mGyro.get(i);
//                StringBuilder builder = new StringBuilder();
//                builder.append(gyroscope.getX()).append(",").append(gyroscope.getY()).append(",").append(gyroscope.getZ()).append(",")
//                        .append(new SimpleDateFormat("MM-dd HH:mm:ss").format(gyroscope.getTime()));
//
//                writer.write(builder.toString());
//                writer.write("\n");
//            }
//            writer.flush();
//            writer.close();
//
//            file = new File(parent, "mag.txt");
//            writer = new BufferedWriter(new FileWriter(file));
//            for(int i = 0; i < mMag.size(); i++){
//                Magnetometer magnetometer = mMag.get(i);
//                StringBuilder builder = new StringBuilder();
//                builder.append(magnetometer.getX()).append(",").append(magnetometer.getY()).append(",").append(magnetometer.getZ()).append(",")
//                        .append(new SimpleDateFormat("MM-dd HH:mm:ss").format(magnetometer.getTime()));
//
//                writer.write(builder.toString());
//                writer.write("\n");
//            }
//            writer.flush();
//            writer.close();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }


    /**
     * 清空文件
     * @param fileName 文件名
     */
    public static void clearFile(String fileName){
        //将文件清空
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/ARecognition", fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("");
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 删除文件
     * @param fileName
     */
    public static void deleteFile(String fileName){
        File file = new File(Environment.getExternalStorageDirectory() + "/ARecognition", fileName);
        if(file.exists()){
            file.delete();
        }
    }


    /**
     * 读取识别结果并存入数据库
     */
    public static void readResult(ArrayList<Integer> idList){

        //读取识别结果
        try {
            File f = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "result.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            String readLine = "";
            Iterator<Integer> iterator = idList.iterator();
            while ((readLine = bufferedReader.readLine()) != null && iterator.hasNext()){
                if(!readLine.isEmpty()){
                    int res = (int)Double.parseDouble(readLine);
                    //记录分类结果
                    FeaVector feaVector = new FeaVector();
                    feaVector.setCategory(res);
                    feaVector.setOrigin(res);
                    feaVector.update(iterator.next());
                }
            }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 读取聚类结果
     * @return
     */
    public static List<String> readCluster(){
        List<String> res = new ArrayList<>();
        try {
            File f = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "cluster.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = bufferedReader.readLine()) != null){
                if(!readLine.isEmpty()){
                    Log.d(FileOperateUtil.class.getSimpleName(), readLine);
                    res.add(readLine);
                }
            }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }


    /**
     * 存储响应时间
     * @param lastIndex
     */
    public static void saveMemoryAndTime(int lastIndex, int mode){
        DecimalFormat df = new DecimalFormat("#.00");

        //time
        Iterator<String> iter = timeList.iterator();
        int fSum = 0, cSum = 0, svmFreq = 0;
        while (iter.hasNext()) {
            String temp = iter.next();
            String title = temp.substring(0, temp.indexOf(":"));
            if (title.equals("feature")) {
                //feature
                fSum += Integer.parseInt(temp.substring(temp.lastIndexOf(":") + 1));
            } else if (title.equals("classify")) {
                //classifier
                svmFreq += Integer.parseInt(temp.substring(temp.indexOf(":") + 1, temp.lastIndexOf(":")));
                cSum += Integer.parseInt(temp.substring(temp.lastIndexOf(":") + 1));
            }
        }
        String time = df.format((double) fSum / GROUP_NUM + (double) cSum / GROUP_NUM);

        Log.d(FileOperateUtil.class.getSimpleName(), "mode = " + mode);
        MemoryTime memoryTime = new MemoryTime(lastIndex, time, svmFreq, timeList.size(), mode);
        if(memoryTime.save()){
            Log.d(FileOperateUtil.class.getSimpleName(), "memoryTime saved");
            timeList.clear();
        }
    }

    /**
     * 打印电池电量
     */
    public static void printBattery(){
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/ARecognition", "battery.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(ContextUtil.getBatteryLevel() + "\t");
            writer.write(new SimpleDateFormat("MM-dd HH:mm").format(System.currentTimeMillis()));
            writer.write("\n");
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
