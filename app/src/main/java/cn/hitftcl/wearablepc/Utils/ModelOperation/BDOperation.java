package cn.hitftcl.wearablepc.Utils.ModelOperation;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;

import cn.hitftcl.wearablepc.Model.BDTable;

public class BDOperation {
    public final static  String TAG = "ModelOperationUtil";
    public static boolean SaveBDInfo(Double longitude, Double latitude, String time){
        Log.d(TAG, "++++++++lat_double："+latitude + "   lng_double:"+longitude+"   times:"+time);

        Date date=parseTimes(time);
        BDTable bdtable = new BDTable(longitude,latitude,date);
        if (bdtable.save())
        {
            Log.d(TAG, "++++++++北斗保存成功");
            return true;
        }
        return false;

    }
    private static Date parseTimes(String hhm){
        Date datenow;
        long times =System.currentTimeMillis();//获取系统时钟，并将其转换成使用UTC时区的日期和时间
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(times);
        String date_times = date+ hhm;
        DateFormat datetime = new SimpleDateFormat("yyyyMMddhhmmss.sss");
        try {
            datenow = datetime.parse(date_times);
            System.out.println( "时间日期："+datenow);
            return datenow;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
