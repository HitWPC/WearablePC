package cn.hitftcl.wearablepc.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import cn.hitftcl.wearablepc.Model.UserIPInfo;

public class BroadcastService extends Service {

    public final static  String TAG = "debug001";
    private DatagramSocket ds = null;

    public static final String Broadcast_Service_Action = "BROADCAST_SERVICE_ACTION";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
//
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
                while (true){
                    try {
                        if(ds==null){
                            ds = new DatagramSocket(null);
                            ds.setReuseAddress(true);
                            ds.bind(new InetSocketAddress(8005));
                        }
                        byte[] buf = new byte[1024];
                        DatagramPacket dp = new DatagramPacket(buf,buf.length);
                        ds.receive(dp);
                        String receiveInfo = new String(dp.getData(), 0, dp.getLength(), "GBK");
                        String[] temp = receiveInfo.split(" ");
                        synchronized (BroadcastService.class){
                            UserIPInfo user = DataSupport.where("username = ?", temp[0]).findFirst(UserIPInfo.class);
                            boolean isCaptain = temp[3].equals("false")?false:true;
                            if(user==null){
                                UserIPInfo my = DataSupport.where("type=0").findFirst(UserIPInfo.class);
                                if(temp[0].equals("commander")){
                                    if(!my.isCaptain()){
                                        continue;
                                    }
                                }
                                UserIPInfo u = new UserIPInfo(temp[0], temp[1], Integer.parseInt(temp[2]));
                                u.setCaptain(temp[3].equals("true")?true:false);
                                if(u.save()){
                                    Log.d(TAG, "新增队员成功:"+receiveInfo);
                                    //发送广播通知页面修改
                                    Intent intent = new Intent();
                                    intent.setAction(Broadcast_Service_Action);
                                    intent.putExtra("personInfo", new Gson().toJson(u));
                                    sendBroadcast(intent);
                                }
                            }else if(!user.getIp().equals(temp[1]) || user.getPort()!=Integer.parseInt(temp[2]) || user.isCaptain()!=isCaptain){
                                Log.d(TAG, "修改队员成功:"+receiveInfo);
                                System.out.println(user);
                                System.out.println(receiveInfo);
                                user.setIp(temp[1]);
                                user.setPort(Integer.parseInt(temp[2]));
                                user.setCaptain(temp[3].equals("true")?true:false);
                                if(user.save()){
                                    //发送广播通知页面修改
                                    Intent intent = new Intent();
                                    intent.setAction(Broadcast_Service_Action);
                                    intent.putExtra("personInfo", new Gson().toJson(user));
                                    sendBroadcast(intent);
                                }
                            }
                        }


                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        if(ds!=null){
                            ds.close();
                            ds = null;
                        }
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
