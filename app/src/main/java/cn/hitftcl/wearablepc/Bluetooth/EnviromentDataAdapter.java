package cn.hitftcl.wearablepc.Bluetooth;

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
 * Created by anyihao on 2017/11/25.
 */
public class EnviromentDataAdapter extends BaseAdapter {


    private List<Double> temperatureList;
    private List<Double> pressureList;
    private List<Double> humidityList;
    private List<Double> SO2List;
    private List<Double> NOList;
    private List<Double> voltageList;

    private LayoutInflater Inflater;

    public EnviromentDataAdapter(Activity parent){
        super();
        temperatureList = new ArrayList<Double>();
        humidityList = new ArrayList<Double>();

        SO2List = new ArrayList<Double>();
        NOList = new ArrayList<Double>();
        voltageList = new ArrayList<Double>();
        pressureList = new ArrayList<Double>();

        Inflater = parent.getLayoutInflater();
    }

    public void addData(int id,double temperature, double pressure, double humidity, double SO2, double NO, double voltage){
        temperatureList.add(temperature);
        pressureList.add(pressure);
        humidityList.add(humidity);
        SO2List.add(SO2);
        NOList.add(NO);
        voltageList.add(voltage);

    }

    public void clearList(){

        temperatureList.clear();
        pressureList.clear();
        humidityList.clear();
        SO2List.clear();
        NOList.clear();
        voltageList.clear();


    }

    @Override
    public int getCount() {
        return temperatureList.size();
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
            convertView = Inflater.inflate(R.layout.layout_detail_env_item, null);
            fields = new FieldReferences();
            fields.envTemperature= (TextView)convertView.findViewById(R.id.env_temperature);
            fields.envHumidity= (TextView)convertView.findViewById(R.id.env_humidity);
            fields.envPressure= (TextView)convertView.findViewById(R.id.env_pressure);
            fields.envSO2= (TextView)convertView.findViewById(R.id.env_SO2);
            fields.envNO= (TextView)convertView.findViewById(R.id.env_NO);
            fields.envVoltage= (TextView)convertView.findViewById(R.id.env_voltage);

            convertView.setTag(fields);
        }else{
            fields = (FieldReferences)convertView.getTag();
        }


        double temperature = temperatureList.get(position);
        double humidity = humidityList.get(position);
        double pressure = pressureList.get(position);
        double SO2 = SO2List.get(position);
        double NO = NOList.get(position);
        double voltage = voltageList.get(position);


        fields.envTemperature.setText(String.valueOf(temperature));
        fields.envHumidity.setText(String.valueOf(humidity));
        fields.envPressure.setText(String.valueOf(pressure));
        fields.envSO2.setText(String.valueOf(SO2));
        fields.envNO.setText(String.valueOf(NO));
        fields.envVoltage.setText(String.valueOf(voltage));

        return  convertView;
    }

    private class FieldReferences{
        TextView envTemperature;
        TextView envHumidity;
        TextView envPressure;

        TextView envSO2;
        TextView envNO;
        TextView envVoltage;

    }
}
