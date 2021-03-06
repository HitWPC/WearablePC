package cn.hitftcl.wearablepc.NetWork;


/**
 * Created by hzf on 2017/5/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.litepal.crud.DataSupport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import cn.hitftcl.wearablepc.Model.Msg;
import cn.hitftcl.wearablepc.Model.Secret;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.Utils.EncryptUtil;

/**
 * 网络操作工具类
 */
public class NetworkUtil {
    private static final String TAG = "debug001";
    private static final int INPUT_STREAM_READ_TIMEOUT = 3000;
    private static final int CONNECT_TIMEOUT = 5000;

    private final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
    private final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());

    private static final Executor exec = Executors.newCachedThreadPool();//使用可缓存线程池

    static class SendCallable implements Callable<Boolean> {
        private String addr;
        private int port;
        private TransType type;
        private String content;
        private String _content;

        public SendCallable(String addr, int port, TransType type, String content, String _content) {
            this.addr = addr;
            this.port = port;
            this.type = type;
            this.content = content;
            this._content = _content;
        }

        @Override
        public Boolean call() throws Exception {
            String typeName = type.name();
            Socket mSocket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(addr, port);
            // 设置连接超时时间
            try {
//                Log.d(TAG, "addr:" + addr);
                mSocket.connect(socketAddress, CONNECT_TIMEOUT);
//                Log.d(TAG, "connected...");

                // 设置读流超时时间，必须在获取流之前设置
                mSocket.setSoTimeout(INPUT_STREAM_READ_TIMEOUT);
                DataOutputStream dataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                dataOutputStream.writeUTF(EncryptUtil.encryptPassword(typeName));
                dataOutputStream.writeUTF(content);

                //发送文件类型

                if (typeName.equals(TransType.FILE_TYPE.name())) {
                    DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(_content)));

                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    while (true) {
                        int readLength = 0;
                        if (dataInputStream != null) {
                            readLength = dataInputStream.read(buffer);
                        }
                        if (readLength == -1) {
                            break;
                        }
                        dataOutputStream.write(buffer, 0, readLength);
                    }
                    dataOutputStream.flush();

                    dataInputStream.close();
                    dataInputStream = null;

                }
                //发送文本类型
                else if (typeName.equals(TransType.TEXT_TYPE.name()) || type.equals(TransType.FUSION_RES.name())) {
                    dataOutputStream.flush();
                }
                //发送传感器数据类型 转换后的Gson（String类型）
                else if (typeName.equals(TransType.SENSOR_TYPE.name()) || typeName.equals(TransType.BD_TYPE.name())) {
                    dataOutputStream.flush();
                }

                //关闭流和socket
                dataOutputStream.close();
                mSocket.close();

                dataOutputStream = null;
                mSocket = null;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    /**
     * 发送数据
     * @param addr
     * @param port
     * @param type
     * @param _content
     */
    public static Boolean sendByTCP(final String addr, final int port, final TransType type, final String _content){
        final String content = EncryptUtil.encryptPassword(_content);
//        Log.d(TAG, "加密前："+_content);
//        Log.d(TAG, "加密后："+content);
        SendCallable sendCallable = new SendCallable(addr, port, type, content,_content);
        FutureTask<Boolean> oneTask = new FutureTask<Boolean>(sendCallable);
        new Thread(oneTask).start();
        try{
            Boolean res = oneTask.get();
            return res;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return  false;
        }
    }

    /**
     * 接收线程：处理接收到的数据
     */


    /**
     * 接收文件
     * @param targetIP 目标IP
     * @param fileReceivePort 文件接收端口
     * @param filePath 文件存储路径
     */
    @Deprecated
    public static void receiveFileBySocket(final String targetIP, final int fileReceivePort, final String filePath) {
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());
        new Thread() {
            public void run() {
                try {
                    //创建socket，连接发送端
                    Socket receiveSocket = new Socket(targetIP, fileReceivePort);
                    //数据流操作
                    DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(receiveSocket.getInputStream()));

                    String savePath = new StringBuilder(filePath).append("/").append(dataInputStream.readUTF()).toString();
                    DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));

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
                    Log.d("NetworkUtil", "接受完毕");
                    //更新数据库
                    Msg msg = new Msg(savePath, Msg.TYPE_RECEIVED, System.currentTimeMillis());
                    msg.save();
                    //通知更新数据
                    Intent intent=new Intent("com.hitwearable.LOCAL_BROADCAST");
                    intent.putExtra("msg", msg);
                    localBroadcastManager.sendBroadcast(intent);

                    dataOutputStream.close();
                    dataInputStream.close();
                    receiveSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    /**
     * 发送文件
     * @param sendPort 文件发送端口
     * @param filePath 文件所在路径
     */
    @Deprecated
    public static void sendFileBySocket(final int sendPort, final String filePath){
        new Thread()
        {
            public void run()
            {
                ServerSocket serverSocket = null;
                try
                {
                    //创建socket
                    serverSocket = new ServerSocket(sendPort, 1);
                    Log.d("NetworkUtil", "等待接收端连接");
                    Socket sendSocket = serverSocket.accept();
                    Log.d("NetworkUtil", "接收端完成连接");
                    //数据流操作
                    DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
                    DataOutputStream dataOutputStream = new DataOutputStream(sendSocket.getOutputStream());

                    File tempFile = new File(filePath);
                    dataOutputStream.writeUTF(tempFile.getName());
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    while (true)
                    {
                        int readLength = 0;
                        if (dataInputStream != null)
                        {
                            readLength = dataInputStream.read(buffer);
                        }
                        if (readLength == -1)
                        {
                            break;
                        }
                        dataOutputStream.write(buffer, 0, readLength);
                    }
                    dataOutputStream.flush();
                    //关闭流和socket
                    dataOutputStream.close();
                    dataInputStream.close();
                    sendSocket.close();
                    serverSocket.close();
                }
                catch(BindException bindException){
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 接收文本
     * @param textLocalPort 文本接收端口
     */
    @Deprecated
    public static void receiveTextByDatagram(final int textLocalPort){
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());
        new Thread()
        {
            public void run()
            {
                    try
                    {
                        DatagramSocket ds = new DatagramSocket(textLocalPort);
                        byte[] buf = new byte[1024];
                        DatagramPacket dp = new DatagramPacket(buf,buf.length);
                        ds.receive(dp);

                        //更新数据库
                        Msg msg = new Msg(new String(dp.getData(), 0, dp.getLength(), "GBK"), Msg.TYPE_RECEIVED, System.currentTimeMillis(), Msg.CATAGORY_TEXT);
                        msg.save();
                        //通知更新数据
                        Intent intent=new Intent("com.hitwearable.LOCAL_BROADCAST");
                        intent.putExtra("msg", msg);
                        localBroadcastManager.sendBroadcast(intent);
                        ds.close();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
            }
        }.start();
    }

    /**
     * 发送文本
     * @param messageSend
     * @param targetIP
     * @param textTargetPort
     */
    public static void sendTextByDatagram(final String messageSend, final String targetIP, final int textTargetPort){
        new Thread() {
            @Override
            public void run() {
                try {
                    DatagramSocket ds = new DatagramSocket();
                    DatagramPacket dp = new DatagramPacket(messageSend.getBytes(), messageSend.getBytes().length,
                            InetAddress.getByName(targetIP), textTargetPort);
                    ds.send(dp);

                    ds.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public static String getSystemTime(){
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
        Date curdate = new Date(System.currentTimeMillis());
        String str = format.format(curdate);
        return str;
    }

    /**
     * 获取本机IP
     * @param context
     * @return
     */
    public static String getLocalIP(Context context){
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    private static String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }


    public static final int TYPE_NONE = -1;
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_WIFI = 1;

    /**
     * 获取网络状态
     *
     * @param context
     * @return one of TYPE_NONE, TYPE_MOBILE, TYPE_WIFI
     * @permission android.permission.ACCESS_NETWORK_STATE
     */
    public static final int getNetWorkStates(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            return TYPE_NONE;//没网
        }

        int type = activeNetworkInfo.getType();
        switch (type) {
            case ConnectivityManager.TYPE_MOBILE:
                return TYPE_MOBILE;//移动数据
            case ConnectivityManager.TYPE_WIFI:
                return TYPE_WIFI;//WIFI
            default:
                break;
        }
        return TYPE_NONE;
    }

}
