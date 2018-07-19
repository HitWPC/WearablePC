package cn.hitftcl.wearablepc.Utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;

public class RequestPermission {

    public static void requestPermission(Activity context, PERMISSION permisson){
        switch (permisson){
            case AUDIO:
                if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(context,new String[]{
                            android.Manifest.permission.RECORD_AUDIO},1);
                }
                break;
            case STORGE:
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(context,new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
                break;
            case BLUETOOTH:
                if (Build.VERSION.SDK_INT >= 23){
                    int check = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (check != PermissionChecker.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(context,new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},1);
                    }
                }
                break;
            default:
                break;
        }
    }
}



