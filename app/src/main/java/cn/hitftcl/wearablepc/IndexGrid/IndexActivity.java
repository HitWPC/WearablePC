package cn.hitftcl.wearablepc.IndexGrid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;

import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.hitftcl.wearablepc.BDMap.MapActivity;
import cn.hitftcl.wearablepc.BDMap.offlinemap.OfflineMapActivity;
import cn.hitftcl.wearablepc.Bluetooth.BluetoothActivity;
import cn.hitftcl.wearablepc.Bluetooth.ClassicBluetoothActivity;
import cn.hitftcl.wearablepc.Bluetooth.SensorDataService;
import cn.hitftcl.wearablepc.DataFusion.FusionActivity;
import cn.hitftcl.wearablepc.Group.UserIPListActivity;
import cn.hitftcl.wearablepc.Message.SecretListActivity;
import cn.hitftcl.wearablepc.NetWork.FusionService;
import cn.hitftcl.wearablepc.NetWork.ReceiveService;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.PERMISSION;
import cn.hitftcl.wearablepc.Utils.RequestPermission;

public class IndexActivity extends AppCompatActivity {
    private final static String TAG = "debug001";
    private Intent sensorDataService = null, netService = null, sendSensorDataService=null, fusionService=null;

    MyGridLayout grid;
    int[] srcs = { R.drawable.actions_booktag, R.drawable.actions_about,
            R.drawable.actions_message, R.drawable.actions_account, R.drawable.camera_usb, R.drawable.camera_wifi};
    String titles[] = { "地图", "数据融合", "条密", "小组", "USB摄像头", "wifi摄像头"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("哈工大可穿戴计算机");

        grid = (MyGridLayout) findViewById(R.id.list);
        grid.setGridAdapter(new MyGridLayout.GridAdatper() {
            @Override
            public View getView(int index) {
                View view = getLayoutInflater().inflate(R.layout.actions_item, null);
                ImageView iv = (ImageView) view.findViewById(R.id.iv);
                TextView tv = (TextView) view.findViewById(R.id.tv);
                iv.setImageResource(srcs[index]);
                tv.setText(titles[index]);
                return view;
            }

            @Override
            public int getCount() {
                return titles.length;
            }
        });
        grid.setOnItemClickListener(new MyGridLayout.OnItemClickListener() {

            @Override
            public void onItemClick(View v, int index) {
                switch (index){
                    case 0:
                        Intent intent0 = new Intent(IndexActivity.this, MapActivity.class);
                        startActivity(intent0);
                        break;
                    case 1:
                        Intent intent1 = new Intent(IndexActivity.this, FusionActivity.class);
                        startActivity(intent1);
                        break;
                    case 2:
                        Intent intent4 = new Intent(IndexActivity.this, SecretListActivity.class);
                        startActivity(intent4);
                        break;
                    case 3:
                        Intent intent5 = new Intent(IndexActivity.this, UserIPListActivity.class);
                        startActivity(intent5);
                        break;
                    case 4:
                        Intent intent6 = new Intent();
                        intent6.setClassName("cn.edu.hit.ftcl.wearablepc.UVCCamera", "cn.edu.hit.ftcl.wearablepc.UVCCamera.MainActivity");
                        startActivity(intent6);
                        break;
                    case 5:
                        Intent intent7 = new Intent();
                        intent7.setClassName("cn.edu.hit.ftcl.wearablepc.wificamera", "cn.edu.hit.ftcl.wearablepc.wificamera.thecamhi.main.MainActivity");
                        startActivity(intent7);
                        break;
                }
            }
        });

        //TODO 开启接收网络数据的服务
        netService = new Intent(this, ReceiveService.class);
        startService(netService);

        //TODO 开启传感器数据接收服务
        sensorDataService = new Intent(this, SensorDataService.class);
        startService(sensorDataService);

        //TODO 开启传感器数据接收服务
        sendSensorDataService = new Intent(this, SensorDataService.class);
        startService(sendSensorDataService);

        //TODO 开启数据融合服务
        fusionService = new Intent(this, FusionService.class);
        startService(fusionService);



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_index, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.offline_down:
                Intent offline_intent = new Intent(IndexActivity.this, OfflineMapActivity.class);
                startActivity(offline_intent);
                return true;
//            case R.id.about_us:
//                return true;
            case R.id.bleset:
                Intent btset_intent = new Intent(IndexActivity.this, BluetoothActivity.class);
                startActivity(btset_intent);
                return true;
            case R.id.classicbtset:
                Intent classic_intent = new Intent(IndexActivity.this, ClassicBluetoothActivity.class);
                startActivity(classic_intent);
                return true;
//            case R.id.voicetest:
//                Intent voice_intent = new Intent(IndexActivity.this, VoiceControlActivity.class);
//                startActivity(voice_intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(sensorDataService);
        stopService(netService);
        stopService(sendSensorDataService);
        stopService(fusionService);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_0:
                Intent intent0 = new Intent(IndexActivity.this, MapActivity.class);
                startActivity(intent0);
                break;
            case KeyEvent.KEYCODE_1:
//                        Intent intent1 = new Intent(IndexActivity.this, SensorActivity.class);
//                        startActivity(intent1);
                break;
            case KeyEvent.KEYCODE_2:
//                        Intent intent2 = new Intent(IndexActivity.this, ImageActivity.class);
//                        startActivity(intent2);
                break;
            case KeyEvent.KEYCODE_3:
//                        Intent intent3 = new Intent(IndexActivity.this, FtpFileListActivity.class);
//                        startActivity(intent3);
                break;
            case KeyEvent.KEYCODE_4:
                Intent intent4 = new Intent(IndexActivity.this, SecretListActivity.class);
                startActivity(intent4);
                break;
            case KeyEvent.KEYCODE_5:
                Intent intent5 = new Intent(IndexActivity.this, UserIPListActivity.class);
                startActivity(intent5);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}