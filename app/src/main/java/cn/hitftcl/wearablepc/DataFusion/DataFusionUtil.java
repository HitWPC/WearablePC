package cn.hitftcl.wearablepc.DataFusion;



import android.util.Log;

import com.amap.api.maps.model.LatLng;

import java.util.Date;
import java.util.List;

import cn.hitftcl.wearablepc.ActionRecognition.model.FeaVector;
import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.EnvironmentTable;
import cn.hitftcl.wearablepc.Model.HeartTable;

/**
 * Created by Administrator on 2018/10/30.
 */

public class DataFusionUtil {

    public final static Integer _MAX_HEART = 150;
    public final static Integer MAX_HEART = 100;
    public final static Integer MIN_HEART = 60;
    public final static Integer _MIN_HEART = 50;


    /**
     * 对体征（心率）、环境、位置信息进行融合
     * @param heartTable
     * @param environmentTable
     * @param bdTable
     * @return
     */
    public static FusionState situation1Fusion(HeartTable heartTable, EnvironmentTable environmentTable, BDTable bdTable, List<FeaVector> feaVectors){
        if(heartTable==null && environmentTable==null) return null;



        FusionState fusionState = new FusionState();
        String IP = null;
        Date fusionDate = new Date();

        //体征数据融合
        int heartrate;
        if(heartTable!=null &&  fusionDate.getTime()- heartTable.getDate().getTime()<=3000){
            int level = judgeFeaVectors(feaVectors);
            if(level==-1||level==0){
                fusionState.heartAvailable = true;
                IP = heartTable.getIP();
                heartrate = heartTable.getRate();
                if(heartrate<50){
                    fusionState.setHeartState(0);  //偏低
                    fusionState.setBodyNormal(false);
                }else if(heartrate>=50 && heartrate<60){
                    fusionState.setHeartState(1);  //正常偏低
                    fusionState.setBodyNormal(true);
                }else if(heartrate>=60 && heartrate<90 ){
                    fusionState.setHeartState(2);  //正常
                    fusionState.setBodyNormal(true);
                }else if(heartrate>=90 && heartrate<110 ){
                    fusionState.setHeartState(3);  //正常偏高
                    fusionState.setBodyNormal(true);
                }else{
                    fusionState.setHeartState(4);  //偏高
                    fusionState.setBodyNormal(false);
                }
            }else if(level==1){
                fusionState.heartAvailable = true;
                IP = heartTable.getIP();
                heartrate = heartTable.getRate();
                if(heartrate<50){
                    fusionState.setHeartState(0);  //偏低
                    fusionState.setBodyNormal(false);
                }else if(heartrate>=50 && heartrate<60){
                    fusionState.setHeartState(1);  //正常偏低
                    fusionState.setBodyNormal(true);
                }else if(heartrate>=60 && heartrate<100 ){
                    fusionState.setHeartState(2);  //正常
                    fusionState.setBodyNormal(true);
                }else if(heartrate>=100 && heartrate<120 ){
                    fusionState.setHeartState(3);  //正常偏高
                    fusionState.setBodyNormal(true);
                }else{
                    fusionState.setHeartState(4);  //偏高
                    fusionState.setBodyNormal(false);
                }
            }else if(level==2){
                fusionState.heartAvailable = true;
                IP = heartTable.getIP();
                heartrate = heartTable.getRate();
                if(heartrate<50){
                    fusionState.setHeartState(0);  //偏低
                    fusionState.setBodyNormal(false);
                }else if(heartrate>=50 && heartrate<60){
                    fusionState.setHeartState(1);  //正常偏低
                    fusionState.setBodyNormal(true);
                }else if(heartrate>=60 && heartrate<120 ){
                    fusionState.setHeartState(2);  //正常
                    fusionState.setBodyNormal(true);
                }else if(heartrate>=120 && heartrate<150 ){
                    fusionState.setHeartState(3);  //正常偏高
                    fusionState.setBodyNormal(true);
                }else{
                    fusionState.setHeartState(4);  //偏高
                    fusionState.setBodyNormal(false);
                }
            }

        }

        //环境数据融合
        if(environmentTable!=null && fusionDate.getTime()- environmentTable.getDate().getTime()<=3000){
            double temperature = environmentTable.getTemperature();
            double pressure = environmentTable.getPressure();
            double humidity = environmentTable.getHumidity();
            double so2 = environmentTable.getSO2();
            double no = environmentTable.getNO();
            fusionState.envAvailable = true;
            IP = environmentTable.getIP();
            //温度
            if(temperature>=-30.0 && temperature< 23.3){
                fusionState.setTemperature(2); //正常
            }else if(temperature<-30.0){
                fusionState.setTemperature(0); //偏低
            }else if(temperature>=23.3){
                fusionState.setTemperature(4); //偏高
            }

            //气压
            if(pressure>=86.0 && pressure<106.0){
                fusionState.setPressure(2); //正常
            }else if(pressure<86.0){
                fusionState.setPressure(0); //偏低
            }else if(pressure>=106.0){
                fusionState.setPressure(4); //偏高
            }

            //湿度
            fusionState.setHumidity(2); //默认正常

            //SO2
            if(so2>=0 && so2<3.0){
                fusionState.setSo2(0);  //正常
            }else if(so2>=3.0 && so2<6.0){
                fusionState.setSo2(1);  //正常偏高
            }else if(so2>=6.0 && so2<20){
                fusionState.setSo2(2);  //偏高，对眼睛、鼻子、咽喉有刺激
            }else if(so2>=20.0 && so2<100.0){
                fusionState.setSo2(3);  //过高， 长时间暴露可能有生命危险
            }

            //NO
            if(no>=0 && no<25.0){
                fusionState.setNo(0);  //正常
            }else if(no>=25.0 && no<50){
                fusionState.setNo(1);  //正常偏高
            }else if(no>=50.0 && no<150) {
                fusionState.setNo(2);  //偏高，对喉部刺激较大
            }else if(no>=150) {
                fusionState.setNo(3);  //过高，短时间暴露容易引起死亡
            }

            if(fusionState.getTemperature()==2 && fusionState.getPressure()==2 && fusionState.getHumidity()==2
                    && (fusionState.getSo2()==0||fusionState.getSo2()==1)
                    && (fusionState.getNo()==0||fusionState.getNo()==1)){
                fusionState.setEnvNormal(true);
            }else{
                fusionState.setEnvNormal(false);
            }
        }

        //地理位置信息融合
        if(bdTable!=null && fusionDate.getTime()- bdTable.getRecordDate().getTime()<=10000){
            fusionState.bdAvailable = true;
            IP = bdTable.getIP();
            fusionState.setBD_Position(new LatLng(bdTable.getLatitude(), bdTable.getLongitude()));
        }

        fusionState.setFusionTime(fusionDate);
        fusionState.setIP(IP);
        return fusionState;
    }

    /**
     *
     * @param feaVectors
     * @return  0->数据不可用
     */
    private static int judgeFeaVectors(List<FeaVector> feaVectors) {
        if(feaVectors==null) return -1;
        int level = 0;
        for (FeaVector feaVector : feaVectors) {
            switch (feaVector.getCategory()){
                case 2: //standing
                    break;
                case 6: //walking
                    level = Math.max(level, 1);
                    break;
                case 7: //running
                    level = Math.max(level, 2);
                    break;
            }
        }
        return level;
    }


}
