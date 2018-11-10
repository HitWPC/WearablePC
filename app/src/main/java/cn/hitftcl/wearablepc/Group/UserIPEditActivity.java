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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import net.vidageek.mirror.dsl.Mirror;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.Set;

import cn.hitftcl.wearablepc.IndexGrid.IndexActivity;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.Utils.Constant;

public class UserIPEditActivity extends AppCompatActivity {
    public final String TAG = "debug001";

    private EditText mEditTextUsername;

    private EditText mEditTextIP;

    private EditText mEditTextPort;

    private EditText mEditBlueMac;
    private Button useSelf;
    private Button mButtonEdit;

    private Button mButtonDelete;

    private CheckBox captainCheck;

    private boolean isCaptain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo_edit_activity);
        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_userip_edit);
        toolbar.setTitle(R.string.group_member_edit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final int userId = intent.getIntExtra("user_id", 0);
        final int position = intent.getIntExtra("position", -1);
        final UserIPInfo userIPInfo = DataSupport.find(UserIPInfo.class, userId);

        mEditTextUsername = (EditText)findViewById(R.id.id_username);
        mEditTextUsername.setText(userIPInfo.getUsername());
        mEditTextUsername.setEnabled(false);

        mEditTextIP = (EditText)findViewById(R.id.id_ip);
        mEditTextIP.setText(userIPInfo.getIp());

        mEditBlueMac = (EditText) findViewById(R.id.mac_adress);
        mEditBlueMac.setText(String.valueOf(userIPInfo.getBlueMac()));

        mEditTextPort = (EditText)findViewById(R.id.id_port);
        mEditTextPort.setText(String.valueOf(userIPInfo.getPort()));

        useSelf = (Button)findViewById(R.id.user_self_mac);
        mButtonEdit = (Button)findViewById(R.id.id_btn_edit);
        mButtonDelete = (Button)findViewById(R.id.id_btn_delete);

        captainCheck = findViewById(R.id.captain_check);
        Log.d(TAG, "     "+userIPInfo.isCaptain());
        captainCheck.setChecked(userIPInfo.isCaptain());

        captainCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isCaptain = b;
                Log.d(TAG,"check changed="+isCaptain);
            }
        });

        mButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = mEditTextIP.getText().toString().trim();
                int port = Integer.parseInt(mEditTextPort.getText().toString().trim());
                String BlueMac = mEditBlueMac.getText().toString().trim();
//                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
//                boolean isMacExisted = false;
//                Log.d("本机蓝牙地址",""+mBluetoothAdapter.getAddress().equals(BlueMac));
//
//                for(BluetoothDevice bonddevice:devices) {
//                    if(bonddevice.getAddress().equals(BlueMac)){
//                        isMacExisted = true;
//                        break;
//                    }
//                }
//                if(mBluetoothAdapter.getAddress().equals(BlueMac))
//                    isMacExisted = true;
                boolean isMacExisted = true;
                Log.d("本机蓝牙地址",""+ isMacExisted);
                if(isMacExisted){
                    userIPInfo.setBlueMac(BlueMac);
                    userIPInfo.setIp(ip);
                    userIPInfo.setPort(port);
                    userIPInfo.setCaptain(isCaptain);
                    if(userIPInfo.getType()==0){
                        Constant.MY_IP = ip;
                        Constant.MY_PORT = port;
                    }
//                    int res = userIPInfo.update(userId);
//                    int res = userIPInfo.updateAll("id=?",String.valueOf(userId));
                    boolean res = userIPInfo.save();
                    Log.d(TAG,isCaptain+" "+res);
                    Log.d(TAG, "shujuku  "+DataSupport.find(UserIPInfo.class, userId).isCaptain());
                    List<UserIPInfo> userIPInfo1 = DataSupport.findAll(UserIPInfo.class);
                    if(userIPInfo1.size()==1){
                        Intent intent1 = new Intent(UserIPEditActivity.this, IndexActivity.class);
                        startActivity(intent1);
                    }else{
                        Intent intent = new Intent();
                        intent.putExtra("result", "edit");
                        intent.putExtra("position", position);
                        intent.putExtra("ip", ip);
                        intent.putExtra("port", port);
                        intent.putExtra("blueMac", BlueMac);
                        intent.putExtra("isCaptain", isCaptain);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
                else{
                    Toast.makeText(MyApplication.getContext(), "请填写正确的蓝牙地址！", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //小组成员删除
        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userIPInfo.getType()!=UserIPInfo.TYPE_SELF){
                    DataSupport.delete(UserIPInfo.class, userId);
                    Intent intent = new Intent();
                    intent.putExtra("result", "delete");
                    intent.putExtra("position", position);
                    setResult(RESULT_OK, intent);
                    finish();
                }else
                    Toast.makeText(MyApplication.getContext(),"当前用户为本人，不能删除！",Toast.LENGTH_SHORT).show();

            }
        });
        useSelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                Object bluetoothManagerService = new Mirror().on(mBluetoothAdapter).get().field("mService");
                if (bluetoothManagerService == null) {
                    return;
                }
                Object address = new Mirror().on(bluetoothManagerService).invoke().method("getAddress").withoutArgs();
                if (address != null && address instanceof String) {
                    mEditBlueMac.setText((String)address);
                } else {
                    return;
                }
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
