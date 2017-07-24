package com.example.peter.redfaceplusplus.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by xalo on 16/9/1.
 */

//画圆
public class CircleImageView extends android.support.v7.widget.AppCompatImageView {
    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //获得图片的宽度
        int width = getWidth();
        //获得图片的高度
        int height = getHeight();
        //短的二分之一作为半径
        int radius = height > width ? width / 2 : height / 2;

        //重新定义的一个画布，这一步很关键
        Paint mPaint = new Paint();
        //抗锯齿
        mPaint.setAntiAlias(true);
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_4444);
        Canvas bitmapCanvas = new Canvas(bitmap);
        super.onDraw(bitmapCanvas);

        //圆形的框
        Bitmap cB = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_4444);
        Canvas cCanv = new Canvas(cB);
        //在控件中间画一个
        cCanv.drawCircle(width / 2, height / 2, radius,
                mPaint);

        canvas.drawBitmap(bitmap, 0.0f, 0.0f, mPaint);
        //dst是后画的图形
        mPaint.setXfermode(new PorterDuffXfermode(
                PorterDuff.Mode.DST_IN));
        //一定要用之前的画布，不然会出现边角是黑色
        bitmapCanvas.drawBitmap(cB, 0.0f, 0.0f, mPaint);


    }
}
