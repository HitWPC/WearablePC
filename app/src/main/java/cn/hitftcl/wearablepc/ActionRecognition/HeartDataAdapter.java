package cn.hitftcl.wearablepc.ActionRecognition;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hitftcl.wearablepc.R;

/**
 * Created by Administrator on 2018/10/10.
 */

public class HeartDataAdapter extends BaseAdapter {

    private List<Integer> dataIdList;
    private List<Date> dataDateList;
    private List<Integer> heartValue;


    private LayoutInflater Inflater;

    public HeartDataAdapter(Activity parent){
        super();
        dataIdList = new ArrayList<Integer>();
        dataDateList = new ArrayList<Date>();
        heartValue = new ArrayList<Integer>();

        Inflater = parent.getLayoutInflater();
    }

    public void addData(int id, Date date, int value){
        dataIdList.add(Integer.valueOf(id));
        dataDateList.add(date);
        heartValue.add(value);
    }

    public void clearList(){
        heartValue.clear();
        dataIdList.clear();
        dataDateList.clear();
    }

    @Override
    public int getCount() {
        return dataIdList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HeartDataAdapter.FieldReferences fields;
        if (convertView == null){
            convertView = Inflater.inflate(R.layout.layout_detail_heart_item, null);
            fields = new HeartDataAdapter.FieldReferences();
            fields.heartQueryId = (TextView)convertView.findViewById(R.id.heart_query_id);
            fields.heartQueryDate = (TextView)convertView.findViewById(R.id.heart_query_date);
            fields.heartQueryValue = (TextView)convertView.findViewById(R.id.heart_query_value);
            convertView.setTag(fields);
        }else{
            fields = (HeartDataAdapter.FieldReferences)convertView.getTag();
        }

        int id = dataIdList.get(position);
        int value = heartValue.get(position);
        Date date = dataDateList.get(position);
        SimpleDateFormat formatter   =   new SimpleDateFormat("HH:mm:ss.sss");
        String dateString = formatter.format(date);

        fields.heartQueryId.setText(String.valueOf(id));
        fields.heartQueryDate.setText(dateString);
        fields.heartQueryValue.setText(String.valueOf(value));


        return  convertView;
    }

    private class FieldReferences{
        TextView heartQueryId;
        TextView heartQueryValue;
        TextView heartQueryDate;
    }
}
