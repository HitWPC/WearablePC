package cn.hitftcl.wearablepc.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cn.hitftcl.wearablepc.Group.UserIPListActivity;
import cn.hitftcl.wearablepc.IndexGrid.IndexActivity;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.NetWork.ReceiveService;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.Utils.Constant;
import cn.hitftcl.wearablepc.Utils.PERMISSION;
import cn.hitftcl.wearablepc.Utils.RequestPermission;


public class LoginActivity extends AppCompatActivity {
    private EditText mUsername;
    private EditText mPassword;
    private Button mButtonLogin;
    private Button mButtonClear;
    private Button mButtonRegister;
    private TextView mHintMsg;

    private UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        //TODO 申请存储器权限
        RequestPermission.requestPermission(this, PERMISSION.STORGE);
        mUsername = (EditText)findViewById(R.id.id_username);
        mPassword = (EditText)findViewById(R.id.id_password);
        mButtonLogin = (Button)findViewById(R.id.id_btn_login);
        mButtonClear = (Button)findViewById(R.id.id_btn_clear);
        mButtonRegister = (Button)findViewById(R.id.id_btn_register);
        mHintMsg = (TextView)findViewById(R.id.id_hint_msg);



        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                int login_result = EncryptionUtil.validateIdentity(username, password);
                switch (login_result){
                    case 0:
                        //打开TCP接收端口
                        if(self.getIp() != null && !self.getIp().isEmpty() && self.getPort() != 0) {
//                        NetworkUtil networkUtil = new NetworkUtil();
//                        networkUtil.receiveByTCP();

                            //打开新的Intent并清除栈里的其他Intent
                            Intent intent = new Intent(LoginActivity.this, IndexActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else {
                        Toast.makeText(MyApplication.getContext(), "请设置您的IP地址和端口号", Toast.LENGTH_SHORT).show();
                        //打开新的Intent并清除栈里的其他Intent
                        Intent intent = new Intent(LoginActivity.this, UserIPListActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        }
                        break;
                    case 1:
                        mHintMsg.setVisibility(View.VISIBLE);
                        mHintMsg.setText(R.string.field_required);
                        break;
                    case 2:
                        mHintMsg.setVisibility(View.VISIBLE);
                        mHintMsg.setText(R.string.error_uname_pwd);
                        break;
                }

            }
        });

        //清空
        mButtonClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mUsername.setText("");
                mPassword.setText("");
                mHintMsg.setVisibility(View.INVISIBLE);
            }
        });

        //注册按钮点击事件
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsername.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                int register_result = EncryptionUtil.register(username, password);
                mHintMsg.setVisibility(View.VISIBLE);
                switch (register_result){
                    case 0:
                        mHintMsg.setText(R.string.success_register);
                        self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
                        saveUserPassToFile(username,password);
                        break;
                    case 1:
                        mHintMsg.setText(R.string.field_required);
                        break;
                    case 2:
                        mHintMsg.setText(R.string.user_existed);
                        break;
                    case 3:
                        mHintMsg.setText(R.string.register_faile);
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        File userPassFile = new File(Constant.userPasswordPath, "userPassword.temp");

        if (userPassFile.exists()) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(userPassFile));
                UserPass userPass = (UserPass) objectInputStream.readObject();
                mPassword.setText(userPass.getPassword());
                mUsername.setText(userPass.getUsername());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    private void saveUserPassToFile(String username, String password){

        File userDir = new File(Constant.userPasswordPath);
        if (!userDir.exists()){
            userDir.mkdirs();
        }
        File userFile = new File(userDir, "userPassword.temp");
        if(!userFile.exists()){
            try {
                userFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {

            FileOutputStream fileOutputStream = new FileOutputStream(userFile);
            UserPass userPass = new UserPass();
            userPass.setPassword(password);
            userPass.setUsername(username);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(userPass);
            Log.d("==========","保存成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
