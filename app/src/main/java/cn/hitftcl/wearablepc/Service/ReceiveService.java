package cn.hitftcl.wearablepc.Service;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import cn.hitftcl.wearablepc.BDMap.BD_Partner_Singleton;
import cn.hitftcl.wearablepc.BDMap.MapActivity;
import cn.hitftcl.wearablepc.DataFusion.FusionState;
import cn.hitftcl.wearablepc.IndexGrid.IndexActivity;
import cn.hitftcl.wearablepc.Message.ImageActivity;
import cn.hitftcl.wearablepc.Model.BDTable;
import cn.hitftcl.wearablepc.Model.EnvironmentTable;
import cn.hitftcl.wearablepc.Model.Msg;
import cn.hitftcl.wearablepc.Model.Secret;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.NetWork.TransType;
import cn.hitftcl.wearablepc.Utils.Constant;
import cn.hitftcl.wearablepc.Utils.EncryptUtil;
import cn.hitftcl.wearablepc.Utils.MediaTypeJudgeUtil;

/**
 * Created by Administrator on 2018/7/11.
 */

public class ReceiveService extends Service {

    public final static  String TAG = "debug001";

    private Thread thread = null;

    public final static String ACTION_MESSAGE_RECEIVE =
            "com.hitwearable.LOCAL_BROADCAST_SECRET";
    public final static String ACTION_SYN_COMMAND =
            "com.hitwearable.ACTION_SYN_COMMAND";

    private final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());

    final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);

    ServerSocket serverSocket = null;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Socket socket = null;
                InputStream inputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;

                try {
                    serverSocket = new ServerSocket(self.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //TODO 等待接收网络传输数据
                while(true){
                    try {
                        //建立连接
//                        serverSocket.setReuseAddress(true);
                        while(serverSocket == null){
                            try {
                                serverSocket = new ServerSocket(self.getPort());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        socket = serverSocket.accept();
                        //发送者信息
                        String senderAddr = socket.getInetAddress().getHostAddress();
                        UserIPInfo sender = DataSupport.where("ip = ?", senderAddr).findFirst(UserIPInfo.class);
                        Log.d(TAG,"New connection accepted " + socket.getInetAddress()+":" + socket.getPort());
                        //获取输入流
                        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        //获得输入
                        String  type = EncryptUtil.decryptPassword(dataInputStream.readUTF());
                        Msg msg=null;
                        if(type.equals(TransType.TEXT_TYPE.name())){
                            //TODO 接收到文本信息
                            String content = EncryptUtil.decryptPassword(dataInputStream.readUTF());
                            msg=saveMsg(self, sender, content, Msg.CATAGORY_TEXT, System.currentTimeMillis());
                            broadcastUpdate(ACTION_MESSAGE_RECEIVE);
                            Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(500);
                            updateUI(msg);
                        }else if(type.equals(TransType.SENSOR_TYPE.name())){
                            //TODO 接收到传感器数据
                            String content = EncryptUtil.decryptPassword(dataInputStream.readUTF());
                            Gson gson = new Gson();
                            EnvironmentTable environmentTable =  gson.fromJson(content, new TypeToken<ArrayList<EnvironmentTable>>(){}.getType());
                            Log.d(TAG, content);

                        }else if(type.equals(TransType.BD_TYPE.name())){
                            //TODO 接收到北斗数据
                            String content = EncryptUtil.decryptPassword(dataInputStream.readUTF());
                            Gson gson = new Gson();
                            ArrayList<BDTable> BD_list =  gson.fromJson(content, new TypeToken<ArrayList<BDTable>>(){}.getType());
                            Log.d(TAG, content);
                            BD_Partner_Singleton.getInstance().setBD_Map(BD_list); //将队友北斗数据存入缓存

                        }else if(type.equals(TransType.FUSION_RES.name())){
                            //TODO 接收到融合数据
                            Log.d(TAG, "接收到融合数据");
                            String content=EncryptUtil.decryptPassword(dataInputStream.readUTF());
                            Gson gson=new Gson();
                            FusionState fusionState=gson.fromJson(content,FusionState.class);
                            Boolean exist=false;
                            for (Map.Entry<String,FusionState> entry: IndexActivity.fusionStateMap.entrySet()){
                                if (entry.getKey().equals(sender.getIp())){
                                    entry.setValue(fusionState);
                                    exist=true;
                                    break;
                                }
                            }
                            if (exist==false){
                                IndexActivity.fusionStateMap.put(sender.getIp(),fusionState);
                            }

                        } else if(type.equals(TransType.FILE_TYPE.name())){
                            //TODO 接收到文件类型数据
                            String fileName = EncryptUtil.decryptPassword(dataInputStream.readUTF());
//                            Log.d(TAG, "接收到文件类型数据 "+ fileName);
//                            String prefix=fileName.substring(fileName.lastIndexOf(".")+1);
//                            Log.d(TAG, "prefix of received file: "+ prefix);
//                            int catagory = 0;
//                            String content = "";
//                            String dirName = "";
//                            switch(prefix){
//                                case "amr":
//                                    catagory = Msg.CATAGORY_VOICE;
//                                    content = "[语音]";
//                                    dirName = "/voice";
//                                    break;
//                                case "jpg":
//                                    catagory = Msg.CATAGORY_IMAGE;
//                                    content = "[图片]";
//                                    dirName = "/image";
//                                    break;
//                                case "mp4":
//                                case "avi":
//                                    catagory = Msg.CATAGORY_VIDEO;
//                                    content = "[视频]";
//                                    dirName = "/video";
//                                    break;
//                                default:
//                                    break;
//                            }
                            int catagory = 0;
                            String content = "";
                            String dirName = "";
                            File file = null;
                            Log.d(TAG, "filaName----->"+fileName);
                            if(MediaTypeJudgeUtil.isImageFileType(fileName)){
                                catagory = Msg.CATAGORY_IMAGE;
                                content = "[图片]";
                                dirName = Constant.imageInfoPath;
                                File imageDir = new File(dirName);
                                if(!imageDir.exists()){
                                    imageDir.mkdirs();
                                }
                                file = new File(imageDir, fileName.substring(fileName.lastIndexOf("/")+1));
                                Log.d(TAG, "file is null?"+(file==null));
                            }else if(MediaTypeJudgeUtil.isAudioFileType(fileName)){
                                catagory = Msg.CATAGORY_VOICE;
                                content = "[语音]";
                                dirName = Constant.voiceInfoPath;
                                File voiceDir = new File(dirName);
                                if(!voiceDir.exists()){
                                    voiceDir.mkdirs();
                                }
                                file=new File(fileName);
                            } else if(MediaTypeJudgeUtil.isVideoFileType(fileName)){
                                catagory = Msg.CATAGORY_VIDEO;
                                content = "[视频]";
                                dirName = Constant.videoInfoPath;
                                File videoDir = new File(dirName);
                                if(!videoDir.exists()){
                                    videoDir.mkdirs();
                                }
                                file = new File(videoDir, fileName.substring(fileName.lastIndexOf("/")+1));
                            }


                            Log.d(TAG, "savePath: " + file.getAbsolutePath() +"content类型：  "+content);
                            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(file))));

                            int bufferSize = 1024;
                            byte[] buf = new byte[bufferSize];
                            while (true) {
                                int read = 0;
                                if (dataInputStream != null) {
                                    read = dataInputStream.read(buf);
                                    Log.d(TAG, "read:______>"+read);
                                }

                                if (read == -1) {
                                    break;
                                }
                                dataOutputStream.write(buf, 0, read);
                            }
                            dataOutputStream.flush();
                            long current = System.currentTimeMillis();
                            msg=saveMsg(self, sender, file.getAbsolutePath(), catagory, current);

                            Secret secret = DataSupport.where("user_id = ?", String.valueOf(sender.getId())).findFirst(Secret.class);
                            if (secret != null) {
                                secret.setContent(content);
                                secret.setTime(current);
                                secret.save();
                            } else {
                                Secret addSecret = new Secret(sender.getId(), sender.getUsername(), "[语音]", current);
                                addSecret.save();
                            }

                            broadcastUpdate(ACTION_MESSAGE_RECEIVE);
                            Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(500);
                            updateUI(msg);

                            if(content.equals("[图片]")){
                                Log.d(TAG, "准备跳转");
                                Intent intent = new Intent(ReceiveService.this, ImageActivity.class);
                                intent.putExtra("IP_INFO", sender.getUsername());
                                intent.putExtra("PATH_INFO", file.getAbsolutePath());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity(intent);
                            }
                        }else if(type.equals(TransType.SYN_COMMAND.name())){
                            //TODO 同步消息
                            String content = EncryptUtil.decryptPassword(dataInputStream.readUTF());
                            if(!Constant.isMapActivityFront){
                                //TODO MapActivity 不在前台
                                Intent mapIntent = new Intent(ReceiveService.this, MapActivity.class);
                                mapIntent.putExtra("Syn_Content", content);
                                startActivity(mapIntent);
                            }else{
                                broadcastUpdate(ACTION_SYN_COMMAND, content);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG,"RecieveService:error########################");
                    }  finally {
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
        });
        thread.start();
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
        return null;
    }

    @Override
    public void onDestroy() {
        stopSelf();
//        if(thread!=null){
//            thread.interrupt();
//        }
        super.onDestroy();
        stopSelf();
    }

    /**
     * 保存条密信息到数据库
     * @param self
     * @param sender
     * @param content
     */
    private Msg saveMsg(UserIPInfo self, UserIPInfo sender, String content, int type, long current){
        Msg msg = new Msg();
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

    private void broadcastUpdate(final String action, String content){
        final Intent intent = new Intent(action);
        intent.putExtra("Syn_Content", content);
        this.sendBroadcast(intent);
    }

}
