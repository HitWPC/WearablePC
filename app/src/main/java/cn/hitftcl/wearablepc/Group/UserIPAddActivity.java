package cn.hitftcl.wearablepc.Group;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.Set;

import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.R;

/**
 * 记录小组其他成员的IP信息
 * Created by hzf on 2017/11/8.
 */

public class UserIPAddActivity extends AppCompatActivity {
    private EditText mUsername;
    private EditText mIP;
    private EditText mPort;
    private EditText mBlueMac;
    private Button mButtonAdd;
    private Button mButtonClear;
    private TextView mWrongInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo_add_activity);
        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_userip_add);
        toolbar.setTitle(R.string.group_member_add);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsername = (EditText)findViewById(R.id.id_username);
        mIP = (EditText)findViewById(R.id.id_ip);
        mPort = (EditText) findViewById(R.id.id_port);
        mBlueMac = (EditText) findViewById(R.id.mac_adress);
        mButtonAdd = (Button)findViewById(R.id.id_btn_add);
        mButtonClear = (Button)findViewById(R.id.id_btn_clear);
        mWrongInput = (TextView) findViewById(R.id.id_wrong_input);

        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMacExisted = false;
                String username = mUsername.getText().toString().trim();
                String ip = mIP.getText().toString().trim();
                int port = Integer.parseInt(mPort.getText().toString().trim());
                String macAdress = mBlueMac.getText().toString().trim();
                if(DataSupport.where("username = ? or ip = ?", username, ip).find(UserIPInfo.class).size() != 0){//验证用户名和IP是否已存在
                    mWrongInput.setVisibility(View.VISIBLE);
                }else {
                    //数据库新增
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                    isMacExisted = false;
                    for(BluetoothDevice bonddevice:devices) {
                        Log.d("蓝牙名称",""+bonddevice.getName());
                        Log.d("蓝牙mxc",""+bonddevice.getAddress());
                        if(bonddevice.getAddress().equals(macAdress)){
                            isMacExisted = true;
                        }
                    }
                    if(isMacExisted){
                        UserIPInfo userIPInfo = new UserIPInfo(username, ip, port,macAdress);
                        userIPInfo.save();

                        Intent intent = new Intent();
                        intent.putExtra("id", userIPInfo.getId());
                        intent.putExtra("username", userIPInfo.getUsername());
                        intent.putExtra("ip", userIPInfo.getIp());
                        intent.putExtra("port", userIPInfo.getPort());
                        intent.putExtra("blueMac",userIPInfo.getBlueMac());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else{
                        Toast.makeText(MyApplication.getContext(), "请设置与你手机配对的蓝牙mac地址！", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        mButtonClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mUsername.setText("");
                mIP.setText("");
                mPort.setText("");
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
}
