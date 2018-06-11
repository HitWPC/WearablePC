package cn.hitftcl.wearablepc.IndexGrid;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.hitftcl.wearablepc.BDMap.MapActivity;
import cn.hitftcl.wearablepc.BDMap.offlinemap.OfflineMapActivity;
import cn.hitftcl.wearablepc.Bluetooth.BTSettingActivity;
import cn.hitftcl.wearablepc.Bluetooth.BluetoothLeService;
import cn.hitftcl.wearablepc.Group.UserIPListActivity;
import cn.hitftcl.wearablepc.Message.SecretListActivity;
import cn.hitftcl.wearablepc.R;

public class IndexActivity extends AppCompatActivity {
    private final static String TAG = "debug001";

    MyGridLayout grid;
    int[] srcs = { R.drawable.actions_booktag, R.drawable.actions_about, R.drawable.actions_comment,
            R.drawable.actions_account, R.drawable.actions_message, R.drawable.actions_account,};
    String titles[] = { "地图", "感知", "通信", "无线", "条密", "小组"};

    private BluetoothLeService mBluetoothLeService;


    /**
     * 管理蓝牙服务的生命周期
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.e(TAG, "mBluetoothLeService is okay");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

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
//                        Intent intent1 = new Intent(IndexActivity.this, SensorActivity.class);
//                        startActivity(intent1);
                        break;
                    case 2:
//                        Intent intent2 = new Intent(IndexActivity.this, ImageActivity.class);
//                        startActivity(intent2);
                        break;
                    case 3:
//                        Intent intent3 = new Intent(IndexActivity.this, FtpFileListActivity.class);
//                        startActivity(intent3);
                        break;
                    case 4:
                        Intent intent4 = new Intent(IndexActivity.this, SecretListActivity.class);
                        startActivity(intent4);
                        break;
                    case 5:
                        Intent intent5 = new Intent(IndexActivity.this, UserIPListActivity.class);
                        startActivity(intent5);
                        break;
                }
            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_map, menu);
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
            case R.id.btset:
                Intent btset_intent = new Intent(IndexActivity.this, BTSettingActivity.class);
                btset_intent.putExtra("bleService",mBluetoothLeService);
                startActivity(btset_intent);
                return true;
//            case R.id.voicetest:
//                Intent voice_intent = new Intent(IndexActivity.this, VoiceControlActivity.class);
//                startActivity(voice_intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
