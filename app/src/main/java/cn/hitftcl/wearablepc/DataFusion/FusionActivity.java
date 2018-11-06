package cn.hitftcl.wearablepc.DataFusion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import cn.hitftcl.wearablepc.NetWork.FusionService;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.ThreadPool;

public class FusionActivity extends AppCompatActivity {

    FusionState fusionState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fusion);

        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_secret);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("数据融合结果");

        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    fusionState = FusionService.getFusionResult();
                    if(fusionState!=null){
                        parseFusionRes(fusionState);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void parseFusionRes(FusionState fusionState) {
        if(fusionState.heartAvailable){
            if(fusionState.isBodyNormal()){
                ((TextView)findViewById(R.id.body)).setText("正常");
            }else{
                ((TextView)findViewById(R.id.body)).setText("异常");
            }
            switch(fusionState.getHeartState()){
                case 0:
                    ((TextView)findViewById(R.id.heart)).setText("偏低");
                    break;
                case 1:
                    ((TextView)findViewById(R.id.heart)).setText("正常偏低");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.heart)).setText("正常");
                    break;
                case 3:
                    ((TextView)findViewById(R.id.heart)).setText("正常偏高");
                    break;
                case 4:
                    ((TextView)findViewById(R.id.heart)).setText("偏高");
                    break;
            }
        }

        if(fusionState.envAvailable){
            if(fusionState.isEnvNormal()){
                ((TextView)findViewById(R.id.environment)).setText("正常");
            }else{
                ((TextView)findViewById(R.id.environment)).setText("异常");
            }

            switch(fusionState.getTemperature()){
                case 0:
                    ((TextView)findViewById(R.id.temperature)).setText("偏低");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.temperature)).setText("正常");
                    break;
                case 4:
                    ((TextView)findViewById(R.id.temperature)).setText("偏高");
                    break;
            }
            switch(fusionState.getHumidity()){
                case 0:
                    ((TextView)findViewById(R.id.humidity)).setText("偏低");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.humidity)).setText("正常");
                    break;
                case 4:
                    ((TextView)findViewById(R.id.humidity)).setText("偏高");
                    break;
            }
            switch(fusionState.getPressure()){
                case 0:
                    ((TextView)findViewById(R.id.pressure)).setText("偏低");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.pressure)).setText("正常");
                    break;
                case 4:
                    ((TextView)findViewById(R.id.pressure)).setText("偏高");
                    break;
            }
            switch(fusionState.getNo()){
                case 0:
                    ((TextView)findViewById(R.id.no)).setText("正常");
                    break;
                case 1:
                    ((TextView)findViewById(R.id.no)).setText("正常偏高");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.no)).setText("偏高，对喉部刺激较大，请注意防护");
                    break;
                case 3:
                    ((TextView)findViewById(R.id.no)).setText("过高，短时间暴露容易引起死亡，请尽快撤离");
                    break;
            }
            switch(fusionState.getSo2()){
                case 0:
                    ((TextView)findViewById(R.id.so2)).setText("正常");
                    break;
                case 1:
                    ((TextView)findViewById(R.id.so2)).setText("正常偏高");
                    break;
                case 2:
                    ((TextView)findViewById(R.id.so2)).setText("偏高，对眼睛、鼻子、咽喉有刺激，请注意防护");
                    break;
                case 3:
                    ((TextView)findViewById(R.id.so2)).setText("过高，长时间暴露可能有生命危险，请尽快撤离");
                    break;
            }
        }
        if(fusionState.bdAvailable){
            ((TextView)findViewById(R.id.position)).setText("纬度:"+fusionState.getBD_Position().latitude+" ,经度:"+fusionState.getBD_Position().longitude);
        }
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
