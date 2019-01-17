package cn.hitftcl.wearablepc.DataFusion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.wearablepc.ActionRecognition.model.FeaVector;
import cn.hitftcl.wearablepc.Model.EnvironmentTable;
import cn.hitftcl.wearablepc.Model.HeartTable;
import cn.hitftcl.wearablepc.Model.UserIPInfo;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.Constant;

public class TestFusionActivity extends AppCompatActivity {

    private Button startSave, stopSave;

    private NumberPicker heart, temp1,temp2,  humi1, humi2,pressure1,pressure2,SO21,SO22,NO1,NO2,actionResult;

    private static int _heart=70;
    private static int _temp1, _temp2;   private static double _temp=20;
    private static int _humi1, _humi2;   private static double _humi=30;
    private static int _pressure1, _pressure2;   private static double _pressure=101;
    private static int _so21, _so22;   private static double _so2=0;
    private static int _no1, _no2;   private static double _no=0;
    private static String _actionResult="standing";
    private static String[] actionType = {"standing","walking","running"};
    private Timer timer= null;
    private TimerTask timerTask = null;

    static boolean flagStartSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fusion);

        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_testFusion);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("数据融合测试配置");

        final UserIPInfo self = DataSupport.where("type = ?", String.valueOf(UserIPInfo.TYPE_SELF)).findFirst(UserIPInfo.class);
        final String IP = self.getIp();
        //TODO 定时保存任务
        if(timer==null){
            timer = new Timer(true);
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("begin:", "准备保存");
                HeartTable heartTable = new HeartTable(_heart, new Date(), IP);
                heartTable.save();

                EnvironmentTable environmentTable = new EnvironmentTable(_temp, _pressure, _humi, _so2, _no, 3.3, new Date(), IP);
                environmentTable.save();
                FeaVector feaVector = new FeaVector(null,System.currentTimeMillis()-2000,System.currentTimeMillis(), Constant.actionTcategory2.get(_actionResult),0);
                feaVector.save();
            }
        };

        //TODO 保存按钮
        startSave = findViewById(R.id.startSave);
        stopSave = findViewById(R.id.stopSave);
        startSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!flagStartSave){
                    if(timer==null){
                        timer = new Timer(true);
                    }
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            HeartTable heartTable = new HeartTable(_heart, new Date(), IP);
                            boolean s1 = heartTable.save();
                            Log.d("TAg", s1+"%%%%%%");
                            EnvironmentTable environmentTable = new EnvironmentTable(_temp, _pressure, _humi, _so2, _no, 3.3, new Date(), IP);
                            boolean suc = environmentTable.save();
                            Log.d("TAg", suc+"%%%%%%%%%%%%%%%%%%%");
                            FeaVector feaVector = new FeaVector(null,System.currentTimeMillis()-2000,System.currentTimeMillis(), Constant.actionTcategory2.get(_actionResult),0);
                            feaVector.save();
                        }
                    };
                    timer.schedule(timerTask, 0, 1000);
                    flagStartSave = true;
                }
            }
        });

        stopSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timer!=null){
                    timer.cancel();
                    timer = null;
                }
                flagStartSave = false;

            }
        });

        //TODO 心率控件
        heart = findViewById(R.id.heart);
        heart.setMaxValue(150);
        heart.setMinValue(0);
        heart.setValue(70);
        heart.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _heart = i1;
//                Toast.makeText(MyApplication.getContext(),"心率"+_heart,Toast.LENGTH_SHORT).show();
            }
        });

        //TODO 获取温度控件
        temp1 = findViewById(R.id.temperature1);
        temp2 = findViewById(R.id.temperature2);
        temp1.setMaxValue(50);
        temp1.setMinValue(0);
        temp1.setValue(20);
        temp2.setMaxValue(9);
        temp2.setMinValue(0);
        temp2.setValue(0);
        temp1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _temp1 = i1;
                _temp = _temp1+0.1*_temp2;
//                Toast.makeText(MyApplication.getContext(),"温度"+_temp,Toast.LENGTH_SHORT).show();
            }
        });
        temp2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _temp2 = i1;
                _temp = _temp1+0.1*_temp2;
//                Toast.makeText(MyApplication.getContext(),"温度"+_temp,Toast.LENGTH_SHORT).show();
            }
        });

        //TODO 获取湿度控件
        humi1 = findViewById(R.id.humi1);
        humi2 = findViewById(R.id.humi2);
        humi1.setMaxValue(99);
        humi1.setMinValue(0);
        humi1.setValue(30);
        humi2.setMaxValue(9);
        humi2.setMinValue(0);
        humi2.setValue(0);
        humi1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _humi1 = i1;
                _humi = _humi1+0.1*_humi2;
//                Toast.makeText(MyApplication.getContext(),"湿度"+_humi,Toast.LENGTH_SHORT).show();
            }
        });
        humi2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _humi2 = i1;
                _humi = _humi1+0.1*_humi2;
//                Toast.makeText(MyApplication.getContext(),"湿度"+_humi,Toast.LENGTH_SHORT).show();
            }
        });

        //TODO 获取气压控件
        pressure1 = findViewById(R.id.pressure);
        pressure2 = findViewById(R.id.pressure2);
        pressure1.setMaxValue(120);
        pressure1.setMinValue(80);
        pressure1.setValue(101);
        pressure2.setMinValue(0);
        pressure2.setMaxValue(9);
        pressure2.setValue(3);
        pressure1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _pressure1 = i1;
                _pressure = _pressure1+0.1*_pressure2;
//                Toast.makeText(MyApplication.getContext(),"气压"+_pressure,Toast.LENGTH_SHORT).show();
            }
        });
        pressure2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _pressure2 = i1;
                _pressure = _pressure1+0.1*_pressure2;
//                Toast.makeText(MyApplication.getContext(),"气压"+_pressure,Toast.LENGTH_SHORT).show();
            }
        });

        //TODO 获取SO2控件
        SO21= findViewById(R.id.SO2);
        SO22 = findViewById(R.id.SO22);
        SO21.setMaxValue(99);
        SO21.setMinValue(0);
        SO21.setValue(0);
        SO22.setMaxValue(9);
        SO22.setMinValue(0);
        SO22.setValue(0);
        SO21.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _so21 = i1;
                _so2 = _so21+0.1*_so22;
//                Toast.makeText(MyApplication.getContext(),"SO2 "+_so2,Toast.LENGTH_SHORT).show();
            }
        });
        SO22.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _so22 = i1;
                _so2 = _so21+0.1*_so22;
//                Toast.makeText(MyApplication.getContext(),"SO2 "+_so2,Toast.LENGTH_SHORT).show();
            }
        });

        //TODO 获取NO控件
        NO1= findViewById(R.id.NO1);
        NO2 = findViewById(R.id.NO2);
        NO1.setMaxValue(199);
        NO1.setMinValue(0);
        NO1.setValue(0);
        NO2.setMaxValue(9);
        NO2.setMinValue(0);
        NO2.setValue(0);
        NO1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _no1 = i1;
                _no = _no1+0.1*_no2;
//                Toast.makeText(MyApplication.getContext(),"no "+_no,Toast.LENGTH_SHORT).show();
            }
        });
        NO2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _no2 = i1;
                _no = _no1+0.1*_no2;
//                Toast.makeText(MyApplication.getContext(),"no "+_no,Toast.LENGTH_SHORT).show();
            }
        });

        //TODO 获取动作控件
        actionResult = findViewById(R.id.actionResult);
        actionResult.setDisplayedValues(actionType);
        //设置最大最小值
        actionResult.setMinValue(1);
        actionResult.setMaxValue(actionType.length);
        //设置默认的位置
        actionResult.setValue(1);
        actionResult.setWrapSelectorWheel(false);
        actionResult.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                _actionResult = actionType[i1-1];
//                Toast.makeText(MyApplication.getContext(),"actionResult "+_actionResult,Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onDestroy() {
        if (timer!=null)
            timer.cancel();
        super.onDestroy();
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
