package cn.hitftcl.wearablepc.Bluetooth;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cn.hitftcl.wearablepc.BDMap.offlinemap.OfflineMapActivity;
import cn.hitftcl.wearablepc.IndexGrid.IndexActivity;
import cn.hitftcl.wearablepc.R;

public class ScanDataFragment extends Fragment {


    private static final String[] DATAITEM = new String[] {"环境","北斗"};//定义一个String数组用来显示ListView的内容
    private ListView sensorNameListView;
    private View view;
    private FragmentManager fragmentManager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_data, container, false);
        sensorNameListView = (ListView)view.findViewById(R.id.sensor_data_listview);
        sensorNameListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, DATAITEM));
        sensorNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        Intent intent0 = new Intent(getActivity(), EnviromentDataActivity.class);   //环境
                        startActivity(intent0);
                        break;
                    case 1:
                        Intent intent5 = new Intent(getActivity(), GeoDataActivity.class);   //GPS
                        startActivity(intent5);
                        break;
                    default:
                        break;
                }
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_datatable, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}