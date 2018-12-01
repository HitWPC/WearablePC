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

public class ActionDataAdapter extends BaseAdapter {

    private List<Integer> dataIdList;
    private List<String> actionList;
    private List<Date> startDateList;
    private List<Date> stopDateList;


    private LayoutInflater Inflater;

    public ActionDataAdapter(Activity parent){
        super();
        dataIdList = new ArrayList<>();
        actionList = new ArrayList<>();
        startDateList = new ArrayList<>();
        stopDateList = new ArrayList<>();

        Inflater = parent.getLayoutInflater();
    }

    public void addData(int id, String action, Date startDate, Date stopDate){
        dataIdList.add(Integer.valueOf(id));
        actionList.add(action);
        startDateList.add(startDate);
        stopDateList.add(stopDate);
    }

    public void clearList(){
        actionList.clear();
        dataIdList.clear();
        startDateList.clear();
        stopDateList.clear();
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
        ActionDataAdapter.FieldReferences fields;
        if (convertView == null){
            convertView = Inflater.inflate(R.layout.layout_detail_action_item, null);
            fields = new ActionDataAdapter.FieldReferences();
            fields.QueryId = (TextView)convertView.findViewById(R.id.query_id);
            fields.StartDate = (TextView)convertView.findViewById(R.id.start_date);
            fields.StopDate = (TextView)convertView.findViewById(R.id.stop_date);
            fields.QueryAction = convertView.findViewById(R.id.query_action);
            convertView.setTag(fields);
        }else{
            fields = (ActionDataAdapter.FieldReferences)convertView.getTag();
        }

        int id = dataIdList.get(position);
        String action = actionList.get(position);
        Date startDate = startDateList.get(position);
        Date stopDate = stopDateList.get(position);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.sss");
        String startDateString = formatter.format(startDate);
        String stopDateString = formatter.format(stopDate);

        fields.QueryId.setText(String.valueOf(id));
        fields.QueryAction.setText(action);
        fields.StartDate.setText(startDateString);
        fields.StopDate.setText(stopDateString);


        return  convertView;
    }

    private class FieldReferences{
        TextView QueryId;
        TextView QueryAction;
        TextView StartDate;
        TextView StopDate;
    }
}
