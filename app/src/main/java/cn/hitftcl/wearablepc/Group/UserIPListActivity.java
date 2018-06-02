package cn.hitftcl.wearablepc.Group;

import android.content.Context;
import android.content.Intent;
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

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.hitftcl.wearablepc.Message.SecretActivity;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.R;

public class UserIPListActivity extends AppCompatActivity {
    private static final String TAG = UserIPListActivity.class.getSimpleName();

    private static final int REQUEST_EDIT = 1;
    private static final int REQUEST_ADD = 2;

    private UserIPAdapter mAdapter;
    private List<UserIPInfo> mData = new ArrayList<>();
    private ListView mListView;
    private Button mButton;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_EDIT:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "REQUEST_EDIT");
                    String result = data.getStringExtra("result");
                    int pos = data.getIntExtra("position", -1);
                    String ip = data.getStringExtra("ip");
                    int port = data.getIntExtra("port", -1);
                    String blueMac = data.getStringExtra("blueMac");
                    if(result.equals("delete")){
                        mData.remove(pos);
                        mAdapter.notifyDataSetChanged();
                    }else if(result.equals("edit")){
                        mData.get(pos).setIp(ip);
                        mData.get(pos).setPort(port);
                        mData.get(pos).setBlueMac(blueMac);
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