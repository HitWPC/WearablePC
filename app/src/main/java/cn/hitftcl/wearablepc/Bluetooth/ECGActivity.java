package cn.hitftcl.wearablepc.Bluetooth;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.UUIDs;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.hitftcl.wearablepc.Model.EnvironmentTable;
import cn.hitftcl.wearablepc.MyApplication;
import cn.hitftcl.wearablepc.R;
import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class ECGActivity extends AppCompatActivity {
    public static final String TAG = "debug001";
    private Button getECG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("心电图监控");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getECG = findViewById(R.id.getECG);
        getECG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<BleDevice> bluetoothDevices = BleManager.getInstance().getAllConnectedDevice();
                for (BleDevice bluetoothDevice : bluetoothDevices) {
                    if(bluetoothDevice.getName().equals("ZG1616")){
                        byte[] buf = {0x01,0x01};
                        BleManager.getInstance().write(bluetoothDevice, UUIDs.UUID_Heart_Service, UUIDs.UUID_ECG_Char_Write, buf,
                                new BleWriteCallback(){

                                    @Override
                                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                        Log.d(TAG, "write success");
                                        Toast.makeText(MyApplication.getContext(), "开始测量心电", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onWriteFailure(BleException exception) {
                                        Log.d(TAG, "write fail");
                                        Toast.makeText(MyApplication.getContext(), "启动心电测量失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        break;
                    }
                }
            }
        });

//        showChangeLineChart();
    }




//    private void setChart(int type,  //0 temp， 1 humi, 2 press, 3 so2, 4 no
//                          LineChartView chartView,
//                          List chartlines,
//                          List mAxisXValues,
//                          List pointValues,
//                          Line chartline,
//                          LineChartData lineChartData,
//                          LineChartValueFormatter lineChartValueFormatter,
//                          Axis axisX,
//                          Axis axisY,
//                          float up,
//                          float down){
////清空数据，将不影响下一次点击按钮 传过来一个新的list的显示
//        chartlines.clear();
//        mAxisXValues.clear();
//        pointValues.clear();
//        /*
//        * 这个循环是循环为x轴（x底端轴线）增加值应该只能是float类型的，当然int可以
//        */
//        for(EnvironmentTable data : envDataList){
//            Date time=data.getDate();
//            AxisValue axisValue=new AxisValue(time.getMinutes()*60+time.getSeconds());
//            axisValue.setLabel(time.getMinutes()+"'"+time.getSeconds()+"\"");
//            mAxisXValues.add(axisValue);
//        }
////        x轴完事了，该布点了
//        for(EnvironmentTable data:envDataList){
////            第一点的坐标假如是（4*24+12，25.5）  那么pointValue的参数就应该是这个
//            Date time=data.getDate();
//            PointValue pointValue = null;
//            switch (type){
//                case 0:
//                    pointValue=new PointValue(time.getMinutes()*60+time.getSeconds(),Float.parseFloat(data.getTemperature()+""));
//                    pointValue.setLabel(data.getTemperature()+"");
//                    axisY.setName("温度 (℃)");
//                    break;
//                case 1:
//                    pointValue=new PointValue(time.getMinutes()*60+time.getSeconds(),Float.parseFloat(data.getHumidity()+""));
//                    pointValue.setLabel(data.getHumidity()+"");
//                    axisY.setName("湿度 (%rh)");
//                    break;
//                case 2:
//                    pointValue=new PointValue(time.getMinutes()*60+time.getSeconds(),Float.parseFloat(data.getPressure()+""));
//                    pointValue.setLabel(data.getPressure()+"");
//                    axisY.setName("气压 (kpa)");
//                    break;
//                case 3:
//                    pointValue=new PointValue(time.getMinutes()*60+time.getSeconds(),Float.parseFloat(data.getSO2()+""));
//                    pointValue.setLabel(data.getSO2()+"");
//                    axisY.setName("SO2 (ppm)");
//                    break;
//                case 4:
//                    pointValue=new PointValue(time.getMinutes()*60+time.getSeconds(),Float.parseFloat(data.getNO()+""));
//                    pointValue.setLabel(data.getNO()+"");
//                    axisY.setName("NO (ppm)");
//                    break;
//                default:
//                    break;
//            }
////            温度显示小数
//            chartline.setFormatter(lineChartValueFormatter);
////            把这个点添加到集合里去,等会显示用
//            pointValues.add(pointValue);
//        }
////        把点的集合放在线上，显示多条线 就用不同的line分别添加不同的values
//        chartline.setValues(pointValues);
////        线的颜色
//        chartline.setColor(Color.BLUE);
////        点的颜色
//        chartline.setPointColor(Color.BLUE);
////        形状，ValueShape提供了几个
//        chartline.setShape(ValueShape.CIRCLE);
////        线的透明度
////        chartline.setAreaTransparency(0);
////        点的大小
//        chartline.setPointRadius(2);
////        点上的标注信息，刚才 pointValues里面每个点的标注
//        chartline.setCubic(true);
////        阴影面积
//        chartline.setFilled(false);
////        是否用线显示。如果为false 则没有曲线只有点显示
//        chartline.setHasLines(true);
////        是否用点显示。如果为false 则没有点
//        chartline.setHasPoints(true);
////        点的标注是否显示
//        chartline.setHasLabels(true);
////        线的粗细
//        chartline.setStrokeWidth(2);
////        显示几条线，就可以分别设置每一条线，然后add进来
//        chartlines.add(chartline);
//
////        线的集合放在chart数据中，
//        lineChartData.setLines(chartlines);
//        lineChartData.setValueLabelsTextColor(Color.BLACK);// 设置数据文字颜色
//
///*        x轴上面的值，就是刚才mAxisXValues的值
//          5个参数分别是：
//          1-例如4*24+12 显示为4日12时
//          2-是否有x轴网格线
//          3-x轴信息标注颜色是黑色
//          4-x轴网格线白色
//          5-x轴信息标注字体12
//          6-x轴的大名
//          Y轴也是这么设置，当你想点击不同的按钮，显示不同的信息时可用type进行处理
//*/
//        axisX.setValues(mAxisXValues).setHasLines(true).setTextColor(Color.BLACK).setLineColor(Color.WHITE).setTextSize(12).setName("时间");
//        axisY.setHasLines(true).setTextColor(Color.BLACK).setLineColor(Color.WHITE);
//
//
//
//
////        X轴上的标注数量,点少的时候可以这么用，点多的时候，就不建议这么用了
////        axisX.setMaxLabelChars(8);
////        x 轴在底部
//        lineChartData.setAxisXBottom(axisX);
////        x 轴在顶部
////        lineChartData.setAxisXTop(axisX);
////        y 轴在左，也可以右边
//        lineChartData.setAxisYLeft(axisY);
//
////        这两句话设置折线上点的背景颜色，默认是有一个小方块，而且背景色和点的颜色一样
////        如果想要原来的效果可以不用这两句话，我的显示的是透明的
//        lineChartData.setValueLabelBackgroundColor(Color.TRANSPARENT);
//        lineChartData.setValueLabelBackgroundEnabled(false);
//
//
////        把数据放在chart里，设置完这句话其实就可以显示了
//        chartView.setLineChartData(lineChartData);
//
//
////        设置行为属性，支持缩放、滑动以及平移，设置他就可以自己设置动作了
//        chartView.setInteractive(true);
////        可放大
//        chartView.setZoomEnabled(true);
////        我这边设置横向滚动
//        chartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
//        chartView.setMaxZoom(2);
////        设置可视化视图样式，这里能做的东西非常多，
//        final Viewport v = new Viewport(chartView.getMaximumViewport());
////        我设置两种。点击不同按钮时，y轴固定最大值最小值不一样
////        这里可以固定x轴，让y轴变化，也可以x轴y轴都固定，也就是固定显示在你设定的区间里的点point（x，y）
//        //=========================心率上下限
//        v.top=up;
//        v.bottom=down;
////        这句话非常关键，上面两种设置，来确定最大可视化样式
////        我们可以理解为，所有点放在linechart时，整个视图全看到时候的样子，也就是点很多很多，距离很紧密
//        chartView.setMaximumViewport(v);
////        接着我们要设置，我们打开这个页面时显示的样子
////        如果你想所有，这两句话就不用了
////        但是我只显示5个点以内 刚才插入的点应该是...（4*24+8，-15.5）（4*24+9，-15.5）（4*24+10，-3.5）（4*24+11，20.0）（4*24+12，8.5）
////        x轴最右边就应该是x=4*24+12 的点 最左边就应该是x=4*24+8的点
////        当然这个非常灵活，也可以固定显示y轴 最小多少，最大多少
//        if(envDataList.size()>=7)
//            v.left=envDataList.get(envDataList.size()-7).getDate().getMinutes()*60+envDataList.get(envDataList.size()-5).getDate().getSeconds();
//        else
//            v.left=envDataList.get(0).getDate().getMinutes()*60+envDataList.get(0).getDate().getSeconds();
//        v.right =envDataList.get(envDataList.size()-1).getDate().getMinutes()*60+envDataList.get(envDataList.size()-1).getDate().getSeconds();
////        确定上两句话的设置
//        chartView.setCurrentViewport(v);
//    }
//
//    /**
//     * 这个是模拟实时获取数据的一个计时器，大家可以根据实际情况修改
//     */
//    private void showChangeLineChart() {
//
//        envDataList = DataSupport.select("*").order("date desc").limit(10).find(EnvironmentTable.class);
//        Log.d(TAG, "envDataList-size:"+envDataList.size());
//        if(envDataList!=null && envDataList.size()>0){
//            Message message = new Message();
//            message.what = 0;
//            handler.sendMessage(message);
//        }
//
//    }
//
//    Handler handler = new Handler(){
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 0:
//                    setKLine(envDataList);
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    };
//

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
