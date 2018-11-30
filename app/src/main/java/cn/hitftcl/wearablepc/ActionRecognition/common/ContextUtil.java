package cn.hitftcl.wearablepc.ActionRecognition.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.hitftcl.wearablepc.MyApplication;

/**
 * Created by hzf on 2018/4/10.
 */

public class ContextUtil {

    /**
     * 内存占用
     * @return
     */
    public static double getAvailMemory() {
        ActivityManager am = (ActivityManager) MyApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            LogUtil.d(ContextUtil.class.getSimpleName(), "availMem rate = " + mi.availMem / (double)mi.totalMem);
//            return mi.availMem / (double)mi.totalMem;
//        }
        return mi.availMem;
    }

    /**
     * CPU使用率
     * @return
     */
    public static double getProcessCpuRate() {

        int rate = 0;

        try {
            String Result;
            Process process;
            process = Runtime.getRuntime().exec("top -n 1");

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = Result.split("%");
                    String[] CPUusage = CPUusr[0].split("User");
                    String[] SYSusage = CPUusr[1].split("System");

                    rate = Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim());
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(ContextUtil.class.getSimpleName(), "cpu rate = " + rate / (double)100);
        return rate / (double)100;
    }

    /**
     * 电池电量
     * @return
     */
    public static double getBatteryLevel(){
        Intent batteryInfoIntent = MyApplication.getContext()
                .registerReceiver( null ,
                        new IntentFilter( Intent.ACTION_BATTERY_CHANGED ) ) ;

        int level = batteryInfoIntent.getIntExtra( "level" , 0 );//电量（0-100）
        Log.d(ContextUtil.class.getSimpleName(), "battery level = " + level / (double)100);
        return level / (double)100;
    }
}
