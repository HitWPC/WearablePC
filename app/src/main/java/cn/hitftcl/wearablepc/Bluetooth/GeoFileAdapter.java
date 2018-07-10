package cn.hitftcl.wearablepc.Bluetooth;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.hitftcl.wearablepc.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by anyihao on 2017/11/25.
 */
public class GeoFileAdapter extends BaseAdapter {

    private List<Integer> dataIdList;
    private List<Date> dataDateList;
    private List<Double> latitudeList;
    private List<Double> longitudeList;


    private LayoutInflater Inflater;

    public GeoFileAdapter(Activity parent){
        super();
        dataIdList = new ArrayList<Integer>();
        dataDateList = new ArrayList<Date>();
        latitudeList = new ArrayList<Double>();
        longitudeList = new ArrayList<Double>();

        Inflater = parent.getLayoutInflater();
    }

    public void addData(int id, Date date, double latitude, double longitude){
        dataIdList.add(Integer.valueOf(id));
        dataDateList.add(date);
        latitudeList.add(latitude);
        longitudeList.add(longitude);

    }

    public void clearList(){

        latitudeList.clear();
        longitudeList.clear();
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
        FieldReferences fields;
        if (convertView == null){
            convertView = Inflater.inflate(R.layout.layout_detail_bd_item, null);
            fields = new FieldReferences();
            fields.gpsQueryId = (TextView)convertView.findViewById(R.id.bd_query_id);
            fields.gpsQueryDate = (TextView)convertView.findViewById(R.id.bd_query_date);
            fields.gpsQueryLatitude = (TextView)convertView.findViewById(R.id.bd_query_latitude);
            fields.gpsQueryLongitude = (TextView)convertView.findViewById(R.id.bd_query_longitude);
            convertView.setTag(fields);
        }else{
            fields = (FieldReferences)convertView.getTag();
        }

        int id = dataIdList.get(position);
        double lati = latitudeList.get(position);
        double longi = longitudeList.get(position);

        Date date = dataDateList.get(position);
        SimpleDateFormat formatter   =   new SimpleDateFormat("HH:mm:ss.sss");
        String dateString = formatter.format(date);

        fields.gpsQueryId.setText(String.valueOf(id));
        fields.gpsQueryDate.setText(dateString);
        fields.gpsQueryLatitude.setText(String.valueOf(lati));
        fields.gpsQueryLongitude.setText(String.valueOf(longi));

        return  convertView;
    }

    private class FieldReferences{
        TextView gpsQueryId;
        TextView gpsQueryDate;
        TextView gpsQueryLatitude;

        TextView gpsQueryLongitude;

    }
}
