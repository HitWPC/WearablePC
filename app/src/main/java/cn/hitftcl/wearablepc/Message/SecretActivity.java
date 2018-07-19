package cn.hitftcl.wearablepc.Message;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.hitftcl.wearablepc.Model.Expression;
import cn.hitftcl.wearablepc.Model.Msg;
import cn.hitftcl.wearablepc.Model.Secret;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.NetWork.NetworkUtil;
import cn.hitftcl.wearablepc.NetWork.ReceiveService;
import cn.hitftcl.wearablepc.NetWork.TransType;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.PERMISSION;
import cn.hitftcl.wearablepc.Utils.RequestPermission;

public class SecretActivity extends AppCompatActivity {
    public static final String TAG = "debug001";

    //蓝牙有关其他变量、常量
    private final int RESULT_CODE_BTDEVICE = 0;
    private final static int MSG_SENT_DATA = 0;
    private final static int MSG_RECEIVE_DATA = 1;
    private final static int MSG_UPDATE_UI = 2;
    private Handler mHandler;
//    private ConnectionManager mConnectionManager;
//    private ConnectionListener mConnectionListener;
    //蓝牙适配器
    BluetoothAdapter bluetoothAdapter;



    private List<Msg> mDataMsgs = new ArrayList<>();
    private List<String> mDataExpressions = new ArrayList<>();

    private LinearLayout mLayoutSecret;
    private LinearLayout mLayoutVoice;
    private LinearLayout mLayoutIndex;

    private Button mButtonVoice;
    private Button mButtonSecret;

    private Button mButtonBackVoice;
    private Button mButtonBackSecret;

    private MsgAdapter mAdapter;
    private ArrayAdapter mAdapterList;

    private MyRecyclerView mRecyclerView;

    private Button mButtonSend;

    private EditText mEditText;

    private ListView mListView;

    private AudioRecorderButton mRecorderButton;

    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;


    private final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
    private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secret_activity);
        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_secret);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //获取上一个Intent传入的数据
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", 0);
        final String username = intent.getStringExtra("username");

        //设置标题
        this.setTitle(username);

        //聊天消息数据初始化
        initMsgAndExpression(userId);

        //RecyclerView
        mRecyclerView = (MyRecyclerView) findViewById(R.id.msg_recycler_view_secret);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new MsgAdapter(mDataMsgs);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(mDataMsgs.size() - 1);

        //Button：发送
        mButtonSend = (Button)findViewById(R.id.id_button_send);
        //EditText
        mEditText = (EditText)findViewById(R.id.id_edittext) ;

        //ListView
        mAdapterList = new ArrayAdapter<String>(SecretActivity.this, android.R.layout.simple_list_item_1, mDataExpressions);
        mListView = (ListView)findViewById(R.id.id_list_expression);
        mListView.setAdapter(mAdapterList);

        //Layout
        mLayoutSecret = (LinearLayout)findViewById(R.id.id_layout_secret);
        mLayoutVoice = (LinearLayout)findViewById(R.id.id_layout_voice);
        mLayoutIndex = (LinearLayout)findViewById(R.id.id_layout_index);
        mLayoutSecret.setVisibility(View.GONE);
        mLayoutVoice.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);

        //Button：切换到语音/条密
        mButtonSecret = (Button)findViewById(R.id.id_button_secret);
        mButtonVoice = (Button)findViewById(R.id.id_button_voice);

        //Button：返回按钮
        mButtonBackSecret = (Button)findViewById(R.id.id_button_back_secret);
        mButtonBackVoice = (Button)findViewById(R.id.id_button_back_voice);

        //Button：录音按钮
        mRecorderButton = (AudioRecorderButton) findViewById(R.id.id_recorder_button);
        //动态申请录音权限
        RequestPermission.requestPermission(this, PERMISSION.AUDIO);

//       initBlue(userId);
        //ListView点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clicked = mDataExpressions.get(position);
                if(clicked.equals("+ 编辑常用短语")){
                    Intent intent = new Intent(SecretActivity.this, ExpressionListActivity.class);
                    startActivity(intent);
                }else{
                    mEditText.setText(clicked);
                }
            }
        });

        //发送按钮点击事件
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserIPInfo userIPInfo = DataSupport.find(UserIPInfo.class, userId);
                String content = mEditText.getText().toString().trim();
                if(content.isEmpty()){
                    Toast.makeText(MyApplication.getContext(), "输入不能为空", Toast.LENGTH_SHORT).show();
                }else {
                    mEditText.setText("");

                    //msg表add
                    long current = System.currentTimeMillis();
                    Msg msg = new Msg(self.getId(), userId, content, current, Msg.TYPE_SENT, Msg.CATAGORY_TEXT);
                    msg.save();
                    //secret表update
                    Secret secret = DataSupport.where("user_id = ?", String.valueOf(userId)).findFirst(Secret.class);
                    if (secret != null) {
                        secret.setContent(content);
                        secret.setTime(current);
                        secret.save();
                    } else {
                        Secret addSecret = new Secret(userId, userIPInfo.getUsername(), content, current);
                        addSecret.save();
                    }
                    //发送数据
                    NetworkUtil.sendByTCP(userIPInfo.getIp(), userIPInfo.getPort(), TransType.TEXT_TYPE, content);
                    mDataMsgs.add(msg);
                    //view更新数据
                    mAdapter.notifyItemInserted(mDataMsgs.size() - 1);
                    //设置位置
                    mRecyclerView.scrollToPosition(mDataMsgs.size() - 1);


//                    boolean ret = mConnectionManager.sendData(content.getBytes());
//                    if(!ret) {
//                        Toast.makeText(SecretActivity.this, "发送内容失败", Toast.LENGTH_SHORT).show();
//                    }
//                    else{
//                        //更新数据
//                        mDataMsgs.add(msg);
//                        mAdapter.notifyItemInserted(mDataMsgs.size() - 1);
//                        mRecyclerView.scrollToPosition(mDataMsgs.size() - 1);
//                    }


                }
            }
        });


        //录音完成后回调
        mRecorderButton.setFinishRecorderCallBack(new AudioRecorderButton.AudioFinishRecorderCallBack() {
            public void onFinish(long seconds, String filePath) {
                UserIPInfo userIPInfo = DataSupport.find(UserIPInfo.class, userId);
                //msg表add
                Msg msg = new Msg(self.getId(), userId, filePath, seconds, Msg.TYPE_SENT, Msg.CATAGORY_VOICE);
                msg.save();
                //secret表update
                Secret secret = DataSupport.where("user_id = ?", String.valueOf(userId)).findFirst(Secret.class);
                if (secret != null) {
                    secret.setContent("[语音]");
                    secret.setTime(seconds);
                    secret.save();
                } else {
                    Secret addSecret = new Secret(userId, userIPInfo.getUsername(), "[语音]", seconds);
                    addSecret.save();
                }
                //发送数据
                NetworkUtil.sendByTCP(userIPInfo.getIp(), userIPInfo.getPort(), TransType.FILE_TYPE, filePath);

                mDataMsgs.add(msg);
                //view更新数据
                mAdapter.notifyItemInserted(mDataMsgs.size() - 1);
                //设置位置
                mRecyclerView.scrollToPosition(mDataMsgs.size() - 1);
            }
        });

        //切换到语音发送
        mButtonVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutIndex.setVisibility(View.GONE);
                mLayoutVoice.setVisibility(View.VISIBLE);
            }
        });

        //切换到条密发送
        mButtonSecret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutIndex.setVisibility(View.GONE);
                mLayoutSecret.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
            }
        });

        //回到主界面
        mButtonBackSecret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutSecret.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mLayoutIndex.setVisibility(View.VISIBLE);
            }
        });
        mButtonBackVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutVoice.setVisibility(View.GONE);
                mLayoutIndex.setVisibility(View.VISIBLE);
            }
        });

        //注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.hitwearable.LOCAL_BROADCAST_SECRET");
        localReceiver = new LocalReceiver(userId);
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

        this.registerReceiver(mMsgUpdateReceiver, msgUpdateIntentFilter());
        Log.d(TAG, "注册了");
    }

    @Override
    public void onBackPressed() {//按下返回后重新进入SecretListActivity
        Intent intent = new Intent(SecretActivity.this, SecretListActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 本地广播接收器
     */
    class LocalReceiver extends BroadcastReceiver {
        private int userId;
        LocalReceiver(int userId){
            this.userId = userId;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Msg msg = (Msg)bundle.getSerializable("msg");
            if(msg.getSender() == userId) {//如果收到的消息是当前用户发送的
                mDataMsgs.add(msg);
                //view更新数据
                mAdapter.notifyItemInserted(mDataMsgs.size() - 1);
                //设置位置
                mRecyclerView.scrollToPosition(mDataMsgs.size() - 1);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        MediaPlayerManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaPlayerManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaPlayerManager.release();
        localBroadcastManager.unregisterReceiver(localReceiver);
        this.unregisterReceiver(mMsgUpdateReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("SecretActivity", "onRestart");

        mDataExpressions = new ArrayList<>();
        //数据库查询常用短语
        List<Expression> expressionList = DataSupport.findAll(Expression.class);
        for(Expression e : expressionList){
            mDataExpressions.add(e.getContent());
        }
        mDataExpressions.add("+ 编辑常用短语");
        mAdapterList = new ArrayAdapter<String>(SecretActivity.this, android.R.layout.simple_list_item_1, mDataExpressions);
        mListView = (ListView)findViewById(R.id.id_list_expression);
        mListView.setAdapter(mAdapterList);
    }

    /**
     * 初始化消息列表和常用短语
     */
    private void initMsgAndExpression(int userId){
        //数据库查询指定队友的条密和语音
        mDataMsgs = DataSupport
                .where("(catagory = ? or catagory = ?) and (receiver = ? or sender = ?)", String.valueOf(Msg.CATAGORY_TEXT), String.valueOf(Msg.CATAGORY_VOICE), String.valueOf(userId), String.valueOf(userId))
                .find(Msg.class);
        //数据库查询常用短语
        List<Expression> expressionList = DataSupport.findAll(Expression.class);
        for(Expression e : expressionList){
            mDataExpressions.add(e.getContent());
        }
        mDataExpressions.add("+ 编辑常用短语");
    }
//    /**
//        初始化蓝牙
//     */
//    private void initBlue(final int userId){
//        //蓝牙初始化
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if(!bluetoothAdapter.isEnabled()){
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivity(intent);
//            finish();
//            return;
//        }

//        //设置可被其他蓝牙设备发现
//        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            //设置为一直开启
//            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
//            startActivity(intent);
//        }
//        mConnectionListener = new ConnectionListener() {
//            @Override
//            public void onConnectStateChange(int oldState, int State) {
//                //连接状态的变化通知给UI线程，请UI线程处理
//                mHandler.obtainMessage(MSG_UPDATE_UI).sendToTarget();
//                System.out.println("更新UI界面");
//            }
//
//            @Override
//            public void onListenStateChange(int oldState, int State) {
//                //监听状态的变化通知给UI线程，请UI线程处理
//                mHandler.obtainMessage(MSG_UPDATE_UI).sendToTarget();
//            }
//
//            @Override
//            public void onSendData(boolean suc, byte[] data) {
//                //将发送的数据交给UI线程，请UI线程处理
//                mHandler.obtainMessage(MSG_SENT_DATA, suc?1:0, 0, data).sendToTarget();
//            }
//
//            @Override
//            public void onReadData(byte[] data) {
//                //将收到的数据交给UI线程，请UI线程处理
//                mHandler.obtainMessage(MSG_RECEIVE_DATA,  data).sendToTarget();
//            }
//        };
//        //给主线程服务的Handler
//        mHandler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//
//                switch (msg.what) {
//                    case MSG_SENT_DATA: {
//                        //UI线程处理发送成功的数据，
//                        //把文字内容展示到主界面上
//                        //获取发送的文字内容
//                    }
//                    break;
//
//                    case MSG_RECEIVE_DATA: {
//                        //UI线程处理接收到的对方发送的数据，
//                        //把文字内容展示到主界面上
//                        byte [] data = (byte []) msg.obj;
//                        if(data != null) {
//                            long current = System.currentTimeMillis();
//                            Msg mwssage = new Msg(userId,self.getId(), new String(data), current, Msg.TYPE_RECEIVED, Msg.CATAGORY_TEXT);
//                            mDataMsgs.add(mwssage);
//                            //view更新数据
//                            mAdapter.notifyItemInserted(mDataMsgs.size() - 1);
//                            //设置位置
//                            mRecyclerView.scrollToPosition(mDataMsgs.size() - 1);
//                        }
//                    }
//                    break;
//
//                    case MSG_UPDATE_UI: {
//                        //更新界面上的菜单等显示状态
//                    }
//                    break;
//                }
//            }
//        };
//        mConnectionManager = new ConnectionManager(mConnectionListener);
//        mConnectionManager.startListen();
//        UserIPInfo userIPInfo = DataSupport.find(UserIPInfo.class, userId);
//        mConnectionManager.connect(userIPInfo.getBlueMac());
//    }

    /**
     * toolbar返回按钮响应事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }


    private final BroadcastReceiver mMsgUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ReceiveService.ACTION_MESSAGE_RECEIVE.equals(action)) {     //收到数据
                //希望自动刷新消息，未实现

            }

        }
    };

    private static IntentFilter msgUpdateIntentFilter() {                        //注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ReceiveService.ACTION_MESSAGE_RECEIVE);
        return intentFilter;
    }

}
