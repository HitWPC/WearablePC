package cn.hitftcl.wearablepc.Bluetooth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import cn.hitftcl.wearablepc.R;
import cn.hitftcl.wearablepc.Utils.LineGraphicView;

public class HeartLineActivity extends AppCompatActivity {
    LineGraphicView tu;
    ArrayList<Double> yList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_line);
    }

    public void drawHeartLine(){

    }
}
