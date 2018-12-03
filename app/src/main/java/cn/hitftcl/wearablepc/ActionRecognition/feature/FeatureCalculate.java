package cn.hitftcl.wearablepc.ActionRecognition.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 计算特征
 * Created by hzf on 2018/2/27.
 */

public class FeatureCalculate {
    //时序数据
    private ArrayList<Double> data;

    //排序后数据
    private ArrayList<Double> sort;

    //用于计算信息熵
    private HashMap<Integer, Integer> entropyDic;

    //几个能用到的统计量
    private double mean;
    private double max;
    private double min;
    private int overZero;
    private double var;

    //用于计算峰度和偏度
    private double sumSq = 0., sumSq2 = 0., sum3 = 0.;

    public FeatureCalculate(ArrayList<Double> data) {
        this.data = data;
        this.sort = new ArrayList<Double>(data.size());
        this.entropyDic = new HashMap<Integer, Integer>();
        //遍历
        double sum = 0, max = data.get(0), min = data.get(0);
        int overZero = 0;
        for(int i = 0; i < data.size(); i++){
            double f = data.get(i);

            sum += f;
            max = (f > max ? f : max);
            min = (f < min ? f : min);

            if(f > 0){
                ++overZero;
            }

            sort.add(f);

            int floor = (int) Math.floor(f * 2);
            if(entropyDic.containsKey(floor)){
                entropyDic.put(floor, entropyDic.get(floor) + 1);
            }else{
                entropyDic.put(floor, 1);
            }
        }

        this.mean = sum / data.size();
        this.max = max;
        this.min = min;
        this.overZero = overZero;

        //排序
        Collections.sort(sort);
    }

    private double timeMean() {
        return this.mean;
    }

    private double timeVar() {
        double sum = 0.0f;
        for(int i = 0; i < data.size(); i++){
            double f = data.get(i);
            sum += Math.pow(f-mean, 2);
        }
        var = sum / data.size();
        return sum / data.size();
    }

    private double timeStd() {
        return Math.sqrt(this.var);
    }

    private double timeMax() {
        return this.max;
    }

    private double timeMin() {
        return this.min;
    }

    private double timeOverZero() {
        return this.overZero / (double)data.size();
    }

    private double timeRange() {
        return this.max - this.min;
    }

    private double timeMedian(){
        return median(this.sort, 0, this.sort.size()-1);
    }

    /**
     * 四分位差
     * TODO:四分位差的计算方法尚不确定
     * @return
     */
    private double timeInterquartile(){
        int mid = (0 + sort.size()-1) / 2;
        if((0 + this.sort.size()-1) % 2 != 0){
            return median(this.sort, mid + 1, this.sort.size() - 1) - median(this.sort, 0, mid);
        }else{
            return median(this.sort, mid + 1, this.sort.size() - 1) - median(this.sort, 0, mid - 1);
        }
    }

    /**
     * 绝对中位差
     * @return
     */
    private double timeMad(){
        double median = median(this.sort, 0, this.sort.size()-1);
        ArrayList<Double> mad = new ArrayList<Double>();
        for(int i = 0; i < data.size(); i++){
            mad.add(Math.abs(median - data.get(i)));
        }
        Collections.sort(mad);
        return median(mad, 0, mad.size()-1);
    }

    private double median(ArrayList<Double> list, int i, int j){
        if((i + j) % 2 == 0){
            return list.get((i + j) / 2);
        }
        return (list.get((i+j) / 2) + list.get((i+j) / 2 + 1)) / 2;
    }

    /**
     * 信息熵
     * @return
     */
    private double entropy(){
        int n = entropyDic.size();
        double ent = 0.;
        for(Map.Entry<Integer, Integer> entry: entropyDic.entrySet()){
            ent -= ((double)entry.getValue() / n) * Math.log((double)entry.getValue() / n) / Math.log(2);
        }
        return ent;
    }

    /**
     * 峰度
     * @return
     */
    private double kurtosis(){
        for(int i = 0; i < data.size(); i++){
            double f = data.get(i);

            sumSq += (f - mean) * (f - mean);
            sumSq2 += Math.pow(f - mean, 4);
            sum3 += Math.pow(f - mean, 3);
        }
        if(sumSq == .0){
            return .0;
        }
        return data.size() * sumSq2 / (sumSq * sumSq) - 3;
    }

    /**
     * 偏度
     * @return
     */
    private double skewness(){
        if(sumSq == .0){
            return .0;
        }
        return Math.pow(data.size(), 0.5) * sum3 / Math.pow(sumSq, 1.5);
    }

    /**
     * 协方差
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static ArrayList<Double> timeCov(ArrayList<Double> a, ArrayList<Double> b, ArrayList<Double> c){
        ArrayList<Double> res = new ArrayList<>();
        res.add(cov(a, b));
        res.add(cov(a, c));
        res.add(cov(b, c));
        return res;
    }
    private static double cov(ArrayList<Double> a, ArrayList<Double> b){
        double meanA = 0, meanB = 0, sum = 0;
        for(int i = 0; i < a.size(); i++){
            meanA += a.get(i);
            meanB += b.get(i);
        }
        meanA /= a.size();
        meanB /= b.size();
        for(int i = 0; i < a.size(); i++){
            sum += (a.get(i) - meanA) * (b.get(i) - meanB);
        }
        return  sum / (a.size() - 1);
    }

    /**
     * 计算特征
     * @return
     */
    public ArrayList<Double> allFeature() {
        ArrayList<Double> featureAll = new ArrayList<>();
        featureAll.add(timeMean());
        featureAll.add(timeVar());
        featureAll.add(timeMax());
        featureAll.add(timeMin());
        featureAll.add(timeRange());
        featureAll.add(timeOverZero());
        featureAll.add(timeMedian());
        featureAll.add(timeMad());
        featureAll.add(entropy());
        featureAll.add(kurtosis());
        featureAll.add(skewness());
        return featureAll;
    }
}
