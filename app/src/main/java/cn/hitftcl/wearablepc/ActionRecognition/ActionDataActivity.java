package cn.hitftcl.wearablepc.ActionRecognition;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hitftcl.wearablepc.ActionRecognition.model.FeaVector;
import cn.hitftcl.wearablepc.Model.HeartTable;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.Constant;

public class ActionDataActivity extends AppCompatActivity {

    private ListView heartLV;
    private Button refresh, clear;
    private TextView data_item_number;

    private ActionDataAdapter actionDataAdapter;

    private List<FeaVector> feaVectors = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_data);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("动作识别结果");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        heartLV = (ListView) findViewById(R.id.detail_heart_listview);
        actionDataAdapter = new ActionDataAdapter(this);
        heartLV.setAdapter(actionDataAdapter);

        data_item_number =(TextView) findViewById(R.id.heartDataNumberer);

        refresh = (Button)findViewById(R.id.refresh_data_btn);
        clear = (Button)findViewById(R.id.clear_data_btn);

        refresh.setOnClickListener(new View.OnClickListener() {   //刷新数据
            @Override
            public void onClick(View v) {
                QueryAllHeartData();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {     //清空数据
            @Override
            public void onClick(View v) {
                ClearAllHeartData();
            }
        });
        QueryAllHeartData();   //进入页面直接显示详细数据
    }

    private void QueryAllHeartData(){       //全部数据
        int queryResultSum = 0;

        feaVectors.clear();
        actionDataAdapter.clearList();
        actionDataAdapter.notifyDataSetChanged();
        feaVectors = DataSupport.findAll(FeaVector.class);
        queryResultSum = feaVectors.size();
        if (queryResultSum <= 0){
            Log.d("无结果","0条结果");
            data_item_number.setText("共查询到"+queryResultSum+"条记录");
            return ;
        }else{
            int index = 1;
            Log.d("结果数目",""+queryResultSum);

            for (FeaVector unit:feaVectors){
                actionDataAdapter.addData(index, Constant.actionTcategory.get(unit.getCategory()), new Date(unit.getStartTime()), new Date(unit.getEndTime()));
                index++;
                actionDataAdapter.notifyDataSetChanged();
            }
        }
        data_item_number.setText("共查询到"+queryResultSum+"条记录");
    }

    /**
     * 清空所有保存的GPS数据
     */
    private void ClearAllHeartData(){
        int deleteSum = 0;
        deleteSum = DataSupport.deleteAll(HeartTable.class);
        if (deleteSum <= 0){
            Log.d("删除了：","0条数据");
        }else{
            Log.d("删除了：",""+deleteSum+"条数据");
        }
        /*清空显示数据列表*/
        feaVectors.clear();
        actionDataAdapter.clearList();
        actionDataAdapter.notifyDataSetChanged();
        data_item_number.setText("共查询到0条记录");
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
