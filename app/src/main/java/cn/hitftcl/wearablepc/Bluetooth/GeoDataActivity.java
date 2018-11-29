package cn.hitftcl.wearablepc.Bluetooth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import cn.hitftcl.wearablepc.R;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.hitftcl.wearablepc.Model.BDTable;

public class GeoDataActivity extends AppCompatActivity {

    private ListView gpsLV;
    private Button refresh, clear;
    private TextView data_item_number;

    private GeoFileAdapter gpsFileAdapter;

    private List<BDTable> gpsTableArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_data);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("北斗实时数据");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gpsLV = (ListView) findViewById(R.id.detail_bd_listview);
        gpsFileAdapter = new GeoFileAdapter(this);
        gpsLV.setAdapter(gpsFileAdapter);

        data_item_number =(TextView) findViewById(R.id.gpsDataNumberer);

        refresh = (Button)findViewById(R.id.refresh_data_btn);
        clear = (Button)findViewById(R.id.clear_data_btn);

        refresh.setOnClickListener(new View.OnClickListener() {   //刷新数据
            @Override
            public void onClick(View v) {
                QueryAllGPSData();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {     //清空数据
            @Override
            public void onClick(View v) {
                ClearAllGPSData();
            }
        });
        QueryAllGPSData();   //进入页面直接显示详细数据
    }

    private void QueryAllGPSData(){       //全部数据
        int queryResultSum = 0;

        gpsTableArrayList.clear();
        gpsFileAdapter.clearList();
        gpsFileAdapter.notifyDataSetChanged();
        gpsTableArrayList = DataSupport.findAll(BDTable.class);
        queryResultSum = gpsTableArrayList.size();
        if (queryResultSum <= 0){
            Log.d("无结果","0条结果");
            data_item_number.setText("共查询到"+queryResultSum+"条记录");
            return ;
        }else{
            int index = 1;
            Log.d("结果数目",""+queryResultSum);

            for (BDTable unit:gpsTableArrayList){
                gpsFileAdapter.addData(index,unit.getRecordDate(), unit.getLatitude(),unit.getLongitude());
                index++;
                gpsFileAdapter.notifyDataSetChanged();
            }
        }
        data_item_number.setText("共查询到"+queryResultSum+"条记录");
    }

    /**
     * 清空所有保存的GPS数据
     */
    private void ClearAllGPSData(){
        int deleteSum = 0;
        deleteSum = DataSupport.deleteAll(BDTable.class);
        if (deleteSum <= 0){
            Log.d("删除了：","0条数据");
        }else{
            Log.d("删除了：",""+deleteSum+"条数据");
        }
        /*清空显示数据列表*/
        gpsTableArrayList.clear();
        gpsFileAdapter.clearList();
        gpsFileAdapter.notifyDataSetChanged();
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
