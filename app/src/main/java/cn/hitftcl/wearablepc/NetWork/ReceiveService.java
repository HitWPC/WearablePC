package cn.hitftcl.wearablepc.NetWork;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.EnviromentTable;
import cn.hitftcl.wearablepc.Model.Msg;
import cn.hitftcl.wearablepc.Model.Secret;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.MyApplication;

/**
 * Created by Administrator on 2018/7/11.
 */

public class ReceiveService extends Service {

    public final static  String TAG = "debug001";

    public final static String ACTION_MESSAGE_RECEIVE =
            "com.hitwearable.LOCAL_BROADCAST_SECRET";
    private final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());

    final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate - Thread ID = " + Thread.currentThread().getId());
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand - startId = " + startId + ", Thread ID = " + Thread.currentThread().getId());
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                Socket socket = null;
                InputStream inputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;

                //TODO 等待接收网络传输数据
                while(true){

                    try {
                        //建立连接
                        if(serverSocket  == null){
                            serverSocket = new ServerSocket(self.getPort());
                            serverSocket.setReuseAddress(true);
                        }

                        socket = serverSocket.accept();
                        //发送者信息
                        String senderAddr = socket.getInetAddress().getHostAddress();
                        UserIPInfo sender = DataSupport.where("ip = ?", senderAddr).findFirst(UserIPInfo.class);
                        Log.d(TAG,"New connection accepted " + socket.getInetAddress()+":" + socket.getPort());
                        //获取输入流
                        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        //获得输入
                        String  type = dataInputStream.readUTF();
                        Msg msg=null;
                        if(type.equals(TransType.TEXT_TYPE.name())){
                            //TODO 接收到文本信息
                            String content = dataInputStream.readUTF();
                            msg=saveMsg(self, sender, content, Msg.CATAGORY_TEXT);
                            broadcastUpdate(ACTION_MESSAGE_RECEIVE);
                            Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(500);
                            updateUI(msg);
                        }else if(type.equals(TransType.SENSOR_TYPE.name())){
                            //TODO 接收到传感器数据
                            String content = dataInputStream.readUTF();
                            Gson gson = new Gson();
                            EnviromentTable enviromentTable =  gson.fromJson(content, new TypeToken<ArrayList<EnviromentTable>>(){}.getType());
                            Log.d(TAG, content);

                        }else if(type.equals(TransType.BD_TYPE.name())){
                            //TODO 接收到北斗数据
                            String content = dataInputStream.readUTF();
                            Gson gson = new Gson();
                            ArrayList<BDTable> BD_list =  gson.fromJson(content, new TypeToken<ArrayList<BDTable>>(){}.getType());
                            Log.d(TAG, content);
                            for(BDTable e:BD_list){
                                Log.d(TAG, ""+e.getLongitude()+" "+e.getLatitude());
                            }

                        }else if(type.equals(TransType.FILE_TYPE.name())){
                            //TODO 接收到文件类型数据
                            String fileName = dataInputStream.readUTF();
                            Log.d(TAG, "接收到文件类型数据 "+ fileName);
                            String prefix=fileName.substring(fileName.lastIndexOf(".")+1);
                            Log.d(TAG, "prefix of received file: "+ prefix);
                            int catagory = 0;
                            String content = "";
                            String dirName = "";
                            switch(prefix){
                                case "amr":
                                    catagory = Msg.CATAGORY_VOICE;
                                    content = "[语音]";
                                    dirName = "/voice";
                                    break;
                                case "jpg":
                                    catagory = Msg.CATAGORY_IMAGE;
                                    content = "[图片]";
                                    dirName = "/image";
                                    break;
                                case "mp4":
                                case "avi":
                                    catagory = Msg.CATAGORY_VIDEO;
                                    content = "[视频]";
                                    dirName = "/video";
                                    break;
                                default:
                                    break;
                            }

                            Log.d(TAG, "savePath: " + fileName);
                            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(fileName))));

                            int bufferSize = 1024;
                            byte[] buf = new byte[bufferSize];
                            while (true) {
                                int read = 0;
                                if (dataInputStream != null) {
                                    read = dataInputStream.read(buf);
                                }
                                if (read == -1) {
                                    break;
                                }
                                dataOutputStream.write(buf, 0, read);
                            }
                            msg=saveMsg(self, sender, fileName, catagory);
                            broadcastUpdate(ACTION_MESSAGE_RECEIVE);
                            updateUI(msg);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                            if(inputStream != null){
                                inputStream.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();;
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateUI(Msg msg) {
        //通知更新UI
        if(msg.getCatagory() == Msg.CATAGORY_TEXT || msg.getCatagory() == Msg.CATAGORY_VOICE) {
            Log.d(TAG, "broadcast to secret activity");
            Intent intent = new Intent("com.hitwearable.LOCAL_BROADCAST_SECRET");
            intent.putExtra("msg", msg);
            localBroadcastManager.sendBroadcast(intent);
        }else{
            Log.d(TAG, "broadcast to image activity");
            Intent intent = new Intent("com.hitwearable.LOCAL_BROADCAST_IMAGE");
            intent.putExtra("msg", msg);
            localBroadcastManager.sendBroadcast(intent);
        }

        Log.d(TAG, "接受完毕");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind - Thread ID = " + Thread.currentThread().getId());
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy - Thread ID = " + Thread.currentThread().getId());
        super.onDestroy();
    }

    /**
     * 保存条密信息到数据库
     * @param self
     * @param sender
     * @param content
     */
    private Msg saveMsg(UserIPInfo self, UserIPInfo sender, String content, int type){
        Msg msg = new Msg();
        long current = System.currentTimeMillis();
        msg.setSender(sender.getId());
        msg.setReceiver(self.getId());
        msg.setPath(content);
        msg.setTime(current);
        msg.setType(Msg.TYPE_RECEIVED);
        msg.setCatagory(type);
        msg.save();
        //secret表update
        Secret secret = DataSupport.where("user_id = ?", String.valueOf(sender.getId())).findFirst(Secret.class);
        if(secret != null){
            secret.setContent(content);
            secret.setTime(current);
            secret.save();
        }else {
            Secret addSecret = new Secret(sender.getId(), sender.getUsername(), content, current);
            addSecret.save();
        }
        return msg;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        this.sendBroadcast(intent);
        Log.d(TAG, "已发送广播");
    }

}
