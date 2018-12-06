package cn.hitftcl.wearablepc.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.wearablepc.Vo.FusionVo;
import cn.hitftcl.wearablepc.Vo.LocationVo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommandService extends Service {
    private Timer timer = null;
    private TimerTask timerTask = null;
    private static int Timer_Interval = 3000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer.schedule(timerTask,0, Timer_Interval);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.e("command", "开始发送数据" );
                //测试数据
                    LocationVo locationVo;
                    FusionVo fusionVo;
                    locationVo=new LocationVo("123","172.1.1.1",new Double(116.427428),new Double(39.9123));
                    List<LocationVo> locationlist=new ArrayList<LocationVo>();
                    List<FusionVo> fusionVoList=new ArrayList<FusionVo>();
                    locationlist.add(locationVo);
                    locationVo=new LocationVo("234","172,1,1,2",new Double(116.397428),new Double(39.8923));
                    locationlist.add(locationVo);
                    fusionVo=new FusionVo("123","正常","偏低","正常","正常","正常","正常");
                    fusionVoList.add(fusionVo);
                    fusionVo=new FusionVo("234","偏低","正常","正常","正常","正常","正常");
                    fusionVoList.add(fusionVo);
                    PostData(locationlist,fusionVoList,"192.168.1.152");

            }
        };
    }

    void PostData(List<LocationVo> locationVoList,List<FusionVo> fusionVoList,String ip){
        Gson gson=new Gson();
        OkHttpClient client=new OkHttpClient();
        MediaType mediaType=MediaType.parse("application/json");
        RequestBody LocationBody = RequestBody
                .create(mediaType,gson.toJson(locationVoList));
        RequestBody FusionBody=RequestBody
                .create(mediaType,gson.toJson(fusionVoList));
        Request request=null;
        request= new Request.Builder()
                .url("http://"+ip+":8080/sendLocation")
                .post(LocationBody)
                .build();
        Call call=client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("command", "位置信息请求失败" );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
        request = new Request.Builder()
                    .url("http://"+ip+":8080/sendFusion")
                    .post(FusionBody)
                    .build();
        call=client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("command", "队员状态请求失败" );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
}
