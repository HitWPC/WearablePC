package cn.hitftcl.wearablepc.Bluetooth;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

import cn.hitftcl.wearablepc.R;

public class BluetoothActivity extends AppCompatActivity  {
    private RadioButton btn_first, btn_second;
    private ViewPager vp;
    //fragment的集合，对应每个子页面
    private ArrayList<Fragment> fragments = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("蓝牙设备设置");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

    }

    public void initView() {
        fragments.add(new ScanFragment());
        fragments.add(new ConnectedFragment());
        fragments.add(new ScanDataFragment());
        FragmentAdapter fragmentAdapter= new FragmentAdapter(getSupportFragmentManager(), fragments);//实例化adapter，得到fragment
        vp=(ViewPager)findViewById(R.id.viewpager);
        vp.setAdapter(fragmentAdapter);//建立连接
        final BottomNavigationView bottom = (BottomNavigationView)findViewById(R.id.bottom);
        bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.one:
                        vp.setCurrentItem(0);
                        break;
                    case R.id.two:
                        vp.setCurrentItem(1);
                        break;
                    case R.id.three:
                        vp.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });

        //viewpager监听事件，当viewpager滑动时得到对应的fragment碎片
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                bottom.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
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
