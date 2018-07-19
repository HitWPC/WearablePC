package cn.hitftcl.wearablepc.NetWork;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import cn.hitftcl.wearablepc.Model.Msg;
import cn.hitftcl.wearablepc.Model.Secret;
import cn.hitftcl.wearablepc.Model.UserIPInfo;

/**
 * Created by Administrator on 2018/7/11.
 */

public class ReceiveService extends Service {

    public final static  String TAG = "debug001";

    public final static String ACTION_MESSAGE_RECEIVE =
            "com.hitwearable.LOCAL_BROADCAST_SECRET";

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
                //TODO 等待接收网络传输数据
                while(true){
                    ServerSocket serverSocket = null;
                    Socket socket = null;
                    InputStream inputStream = null;
                    InputStreamReader inputStreamReader = null;
                    BufferedReader bufferedReader = null;

                    final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
                    int myPort = self.getPort();

                    try {
                        //建立连接
                        if(serverSocket  == null){
                            serverSocket = new ServerSocket(myPort);
                            serverSocket.setReuseAddress(true);
                        }

                        socket = serverSocket.accept();
                        //发送者信息
                        String senderAddr = socket.getInetAddress().getHostAddress();
                        UserIPInfo sender = DataSupport.where("ip = ?", senderAddr).findFirst(UserIPInfo.class);
                        Log.d(TAG,"New connection accepted " + socket.getInetAddress()+":" + socket.getPort());
                        //获取输入流
                        inputStream=socket.getInputStream();        //得到一个输入流，接收客户端传递的信息
                        inputStreamReader=new InputStreamReader(inputStream); //提高效率，将自己字节流转为字符流
                        bufferedReader=new BufferedReader(inputStreamReader);    //加入缓冲区
                        //获得输入
                        String temp=null;
                        String info="";
                        while((temp=bufferedReader.readLine())!=null){
                            info+=temp;
                        }
                        //打印信息收到的信息
                        Log.d(TAG, info);
                        String[] infos = info.split(" ");
                        if(infos[0].equals(TransType.TEXT_TYPE.name())){
                            //TODO 接收到文本信息
                            saveMsg(self, sender, infos[1]);
                            broadcastUpdate(ACTION_MESSAGE_RECEIVE);
                            Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(500);
                        }else if(infos[0].equals(TransType.SENSOR_TYPE.name())){
                            //TODO 接收到传感器数据
                        }else if(infos[0].equals(TransType.FILE_TYPE.name())){
                            //TODO 接收到文件类型数据
                            broadcastUpdate(ACTION_MESSAGE_RECEIVE);
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
    private void saveMsg(UserIPInfo self, UserIPInfo sender, String content){
        Msg msg = new Msg();
        long current = System.currentTimeMillis();
        msg.setSender(sender.getId());
        msg.setReceiver(self.getId());
        msg.setPath(content);
        msg.setTime(current);
        msg.setType(Msg.TYPE_RECEIVED);
        msg.setCatagory(Msg.CATAGORY_TEXT);
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
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        this.sendBroadcast(intent);
        Log.d(TAG, "已发送广播");
    }

}
