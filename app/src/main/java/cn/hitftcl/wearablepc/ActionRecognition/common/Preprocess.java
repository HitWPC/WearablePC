package cn.hitftcl.wearablepc.ActionRecognition.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by hzf on 2018/3/25.
 */

public class Preprocess {

    /**
     * 中值滤波
     * @param data
     * @return
     */
    public static ArrayList<Double> medFilt(ArrayList<Double> data){
        ArrayList<Double> res = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            if(i == 0){
                res.add(calMed(0, 0, data.get(i), data.get(i+1), data.get(i+2)));
            }
            else if(i == 1){
                res.add(calMed(0, data.get(i-1), data.get(i), data.get(i+1), data.get(i+2)));
            }
            else if(i == data.size() - 2){
                res.add(calMed(data.get(i-2), data.get(i-1), data.get(i), data.get(i+1), 0));
            }else if(i == data.size() - 1){
                res.add(calMed(data.get(i-2), data.get(i-1), data.get(i), 0, 0));
            }else{
                res.add(calMed(data.get(i-2), data.get(i-1), data.get(i), data.get(i+1), data.get(i+2)));
            }
        }
        return res;
    }
    private static double calMed(double x1, double x2, double x3, double x4, double x5){
        double[] arr = new double[]{x1, x2, x3, x4, x5};
        Arrays.sort(arr);
        return arr[2];
    }

    public static void main(String[] args){
        double[] xwindow = {-0.1712493896484375, -0.16766357421875, -0.16766357421875, -0.160491943359375, -0.16766357421875, -0.174835205078125, -0.180816650390625, -0.180816650390625, -0.180816650390625, -0.17962646484375, -0.1700592041015625, -0.162872314453125, -0.162872314453125, -0.1700592041015625, -0.174835205078125, -0.174835205078125, -0.1868133544921875, -0.174835205078125, -0.168853759765625, -0.168853759765625, -0.160491943359375, -0.1580963134765625, -0.1580963134765625, -0.1580963134765625, -0.1580963134765625, -0.162872314453125, -0.174835205078125, -0.162872314453125, -0.162872314453125, -0.162872314453125};
        ArrayList<Double> list = new ArrayList<>();
        for(double d : xwindow){
            list.add(d);
        }
        System.out.println(medFilt(list));
    }
}
