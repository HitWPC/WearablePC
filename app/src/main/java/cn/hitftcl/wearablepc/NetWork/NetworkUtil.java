package cn.hitftcl.wearablepc.NetWork;


/**
 * Created by hzf on 2017/5/16.
 */

import android.content.Intent;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.hitftcl.wearablepc.Model.Msg;
import cn.hitftcl.wearablepc.Model.Secret;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Model.UserIPInfo;

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

    /**
     * 接收数据1110
     */
    public void receiveByTCP(){
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                ServerSocket serverSocket = null;
                try
                {
                    int selfPort = self.getPort();
                    //ServerSocket
                    serverSocket = new ServerSocket(selfPort);
                    while(serverSocket != null && !serverSocket.isClosed()) {
                        while (true) {
                            Socket socket = serverSocket.accept();
                            Log.d("NetworkUtil", "accepted...");
                            ReceiveRunnable runnable = new ReceiveRunnable(socket);
                            exec.execute(runnable);
                        }
                    }

                    serverSocket.close();
                    serverSocket = null;
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
        }).start();
    }

    /**
     * 发送数据
     * @param addr
     * @param port
     * @param type
     * @param content
     */
    public static void sendByTCP(final String addr, final int port, final TransType type, final String content){
        exec.execute(new Runnable() {
            @Override
            public void run() {
                String typeName = type.name();
                Socket mSocket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(addr, port);
                // 设置连接超时时间
                try {
                    mSocket.connect(socketAddress, CONNECT_TIMEOUT);
                    Log.d(TAG, "connected...");

                    // 设置读流超时时间，必须在获取流之前设置
                    mSocket.setSoTimeout(INPUT_STREAM_READ_TIMEOUT);
                    DataOutputStream dataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                    dataOutputStream.writeUTF(typeName);
                    dataOutputStream.writeUTF(content);

                    //发送文件类型

                    if(typeName.equals(TransType.FILE_TYPE.name())){
                        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(content)));

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

                        dataInputStream.close();
                        dataInputStream = null;

                    }
                    //发送文本类型
                    else if(typeName.equals(TransType.TEXT_TYPE.name())){
                        dataOutputStream.flush();
                    }
                    //发送传感器数据类型 转换后的Gson（String类型）
                    else if(typeName.equals(TransType.SENSOR_TYPE.name()) || typeName.equals(TransType.BD_TYPE.name())){
                        dataOutputStream.flush();
                    }

                    //关闭流和socket
                    dataOutputStream.close();
                    mSocket.close();
                    dataOutputStream = null;
                    mSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 接收线程：处理接收到的数据
     */
    class ReceiveRunnable implements Runnable {
        private Socket mSocket;

        ReceiveRunnable(Socket socket) {
            this.mSocket = socket;
        }

        @Override
        public void run() {
            try {
                // 给读取流设置超时时间，否则会一直在read()那阻塞
                mSocket.setSoTimeout(INPUT_STREAM_READ_TIMEOUT);
                //发送方IP地址
                String senderAddr = mSocket.getInetAddress().getHostAddress();
                UserIPInfo sender = DataSupport.where("ip = ?", senderAddr).findFirst(UserIPInfo.class);

                //数据流操作
                DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));

                Msg msg = new Msg();
                String receivedType = dataInputStream.readUTF();
                if(receivedType.equals("text")){//接收的是文本
                    String content = dataInputStream.readUTF();
                    //msg表add
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
                }else if(receivedType.equals("file")){//接收的是文件
                    String fileName = dataInputStream.readUTF();
                    //后缀名不同，消息种类不同
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

                    //msg表add
                    long current = System.currentTimeMillis();
                    msg.setSender(sender.getId());
                    msg.setReceiver(self.getId());
                    msg.setPath(fileName);
                    msg.setTime(current);
                    msg.setType(Msg.TYPE_RECEIVED);
                    msg.setCatagory(catagory);
                    Log.d(TAG, "msg add result: "+ msg.save());
                    //secret表update
                    Secret secret = DataSupport.where("user_id = ?", String.valueOf(sender.getId())).findFirst(Secret.class);
                    if(secret != null){
                        secret.setContent(content);
                        secret.setTime(current);
                        Log.d(TAG, "secret update result: " + secret.save());
                    }else {
                        Secret addSecret = new Secret(sender.getId(), sender.getUsername(), content, current);
                        Log.d(TAG, "secret add result: " + addSecret.save());
                    }

                    dataOutputStream.close();
                    dataOutputStream = null;
                }
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

                dataInputStream.close();
                mSocket.close();
                dataInputStream = null;
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
    @Deprecated
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
}
