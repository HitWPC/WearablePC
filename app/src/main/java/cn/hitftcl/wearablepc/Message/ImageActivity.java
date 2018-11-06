package cn.hitftcl.wearablepc.Message;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import cn.hitftcl.wearablepc.R;

public class ImageActivity extends AppCompatActivity {
    public static final String TAG="debug001";
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        String path = intent.getStringExtra("PATH_INFO");

        imageView = findViewById(R.id.img);

        Log.d(TAG, path);
        setImageSrc(path);

    }

    public void setImageSrc(String path){
        Bitmap bitmap = getLoacalBitmap(path);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
