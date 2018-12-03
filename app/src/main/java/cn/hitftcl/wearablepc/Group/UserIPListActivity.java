package cn.hitftcl.wearablepc.Group;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.hitftcl.wearablepc.Message.SecretActivity;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.Service.BroadcastService;
import cn.hitftcl.wearablepc.NetWork.NetworkUtil;
import cn.hitftcl.wearablepc.R;

public class UserIPListActivity extends AppCompatActivity {
    private static final String TAG = "debug001";

    private static final int REQUEST_EDIT = 1;
    private static final int REQUEST_ADD = 2;


    private UserIPAdapter mAdapter;
    private List<UserIPInfo> mData = new ArrayList<>();
    private ListView mListView;
    private Button mButton, sendIP,manageService;
    private Boolean StartService=false;

    Intent broadcastService = null;

    UserIPInfo self =null;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grouplist_activity);

        mData = DataSupport.findAll(UserIPInfo.class);
        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_userip_list);

        toolbar.setTitle(R.string.group_member_info);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ListView
        mAdapter = new UserIPAdapter(UserIPListActivity.this, R.layout.userinfo_item, mData);
        mListView = (ListView)findViewById(R.id.id_list_user_ip);
        mListView.setAdapter(mAdapter);
        //Button
        mButton = (Button) findViewById(R.id.id_btn_plus);
        sendIP = findViewById(R.id.id_btn_sendBroadcast);
        manageService=(Button)findViewById(R.id.id_btn_manageService);

        self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
        if(self.getIp()==null || self.getPort()<1024){
            sendIP.setVisibility(View.GONE);
        }

        broadcastService = new Intent(UserIPListActivity.this,BroadcastService.class);



        //开启或关闭广播
        manageService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StartService==false){
                    StartService=true;
                    manageService.setText("关闭广播");
                    startService(broadcastService);
                    Toast.makeText(getApplicationContext(), "广播已开启", Toast.LENGTH_SHORT).show();

                }else {
                    StartService=false;
                    manageService.setText("开启广播");
                    stopService(broadcastService);
                    Toast.makeText(getApplicationContext(), "广播已关闭", Toast.LENGTH_SHORT).show();
                }

            }
        });

        sendIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    UserIPInfo selfInfo = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
                    String username = selfInfo.getUsername();
                    String MY_IP = NetworkUtil.getLocalIP(MyApplication.getContext());
                    int port = selfInfo.getPort();
                    boolean isCaptain = selfInfo.isCaptain();
                    String sendContent = username+" "+MY_IP+" "+port+" "+isCaptain;
                    NetworkUtil.sendTextByDatagram(sendContent, "255.255.255.255", 8005);
            }
        });


        //点击事件:向该成员发送条密
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserIPInfo clicked = mData.get(position);
                if(clicked.getType() != UserIPInfo.TYPE_SELF) {//当点击的用户不是自己时,进入条密语音通信界面
                    Intent intent = new Intent(UserIPListActivity.this, SecretActivity.class);
                    intent.putExtra("user_id", clicked.getId());
                    intent.putExtra("username", clicked.getUsername());
                    startActivity(intent);
                }
            }
        });

        //长按编辑该用户IP信息
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                UserIPInfo clicked = mData.get(position);
                Intent intent = new Intent(UserIPListActivity.this, UserIPEditActivity.class);
                intent.putExtra("user_id", clicked.getId());
                intent.putExtra("position", position);
                startActivityForResult(intent, REQUEST_EDIT);
                return true;
            }
        });

        //新增一个队友IP
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserIPListActivity.this, UserIPAddActivity.class);
                startActivityForResult(intent, REQUEST_ADD);
            }
        });
    }

    /**
     * 本地广播接收器
     */
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String content = intent.getStringExtra("personInfo");
            UserIPInfo user= new Gson().fromJson(content,UserIPInfo.class);
            Log.d(TAG,"before-add-size:---->"+mData.size());
            boolean flag = false;
            UserIPInfo oldUserInfo = null;
            for (UserIPInfo userIPInfo:mData){
                if (userIPInfo.getId()==user.getId()){
                    flag=true;
                    oldUserInfo = userIPInfo;
                }
            }
            System.out.println("*********"+flag);
            if (flag){
                mData.remove(oldUserInfo);
            }
            mData.add(user);
            Log.d(TAG,"after-add-size:---->"+mData.size());
            //view更新数据
            mAdapter.notifyDataSetChanged();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastService.Broadcast_Service_Action);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

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

    @Override
    protected void onStop() {
        super.onStop();
        if(StartService==true){
            stopService(broadcastService);
            Toast.makeText(this, "广播已关闭", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_EDIT:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "REQUEST_EDIT");
                    String result = data.getStringExtra("result");
                    int pos = data.getIntExtra("position", -1);
                    String ip = data.getStringExtra("ip");
                    int port = data.getIntExtra("port", -1);
                    boolean captain = data.getBooleanExtra("isCaptain", false);

                    if(result.equals("delete")){
                        mData.remove(pos);
                        mAdapter.notifyDataSetChanged();
                    }else if(result.equals("edit")){
                        Log.d(TAG, "通知修改");
                        mData.get(pos).setIp(ip);
                        mData.get(pos).setPort(port);
                        mData.get(pos).setCaptain(captain);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case REQUEST_ADD:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "REQUEST_ADD");
                    int id = data.getIntExtra("id", 0);
                    String username = data.getStringExtra("username");
                    String ip = data.getStringExtra("ip");
                    String blueMac = data.getStringExtra("blueMac");
                    int port = data.getIntExtra("port", -1);
                    UserIPInfo userIPInfo = new UserIPInfo();
                    userIPInfo.setId(id);
                    userIPInfo.setUsername(username);
                    userIPInfo.setIp(ip);
                    userIPInfo.setPort(port);
                    userIPInfo.setBlueMac(blueMac);
                    mData.add(userIPInfo);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }

    }

    public class UserIPAdapter extends ArrayAdapter<UserIPInfo> {
        private int resourceId;

        public UserIPAdapter(Context context, int resource, List<UserIPInfo> objects) {
            super(context, resource, objects);
            resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserIPInfo userIPInfo = getItem(position);
            View view;
            ViewHolder viewHolder;
            if(convertView == null){
                view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.username = (TextView)view.findViewById(R.id.id_username);
                viewHolder.ip = (TextView)view.findViewById(R.id.id_ip);
                viewHolder.port = (TextView)view.findViewById(R.id.id_port);
                viewHolder.blueMac = (TextView) view.findViewById(R.id.mac_adress);
                view.setTag(viewHolder);
            }else{
                view = convertView;
                viewHolder = (ViewHolder)view.getTag();
            }
            viewHolder.username.setText(userIPInfo.getUsername());
            if(userIPInfo.getIp() == null || userIPInfo.getIp().isEmpty()){
                viewHolder.ip.setText("未设置");
            }else {
                viewHolder.ip.setText(userIPInfo.getIp());
            }
            if(userIPInfo.getPort() == 0) {
                viewHolder.port.setText("未设置");
            }else{
                viewHolder.port.setText(String.valueOf(userIPInfo.getPort()));
            }
            if(userIPInfo.getBlueMac() == null) {
                viewHolder.blueMac.setText("未设置");
            }else{
                viewHolder.blueMac.setText(String.valueOf(userIPInfo.getBlueMac()));
            }
            if(userIPInfo.isCaptain()){
                view.setBackgroundColor(Color.LTGRAY);
            }else{
                view.setBackgroundColor(Color.parseColor("#d8e0e8"));
            }
            return view;
        }

        class ViewHolder{
            TextView username;
            TextView ip;
            TextView port;
            TextView blueMac;
        }
    }

}
