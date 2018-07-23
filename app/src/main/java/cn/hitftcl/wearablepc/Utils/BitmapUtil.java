package cn.hitftcl.wearablepc.Utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Administrator on 2018/7/23.
 */

public class BitmapUtil {

    /**
     *
     * @param bitMap
     * @param newWidth 想要的宽度
     * @param newHeight 想要的高度
     * @return
     */
    public static Bitmap getAdaptBitMap(Bitmap bitMap, int newWidth, int newHeight){
        int width = bitMap.getWidth();
        int height = bitMap.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, true);
    }
}
