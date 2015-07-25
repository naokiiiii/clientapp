package com.t4j.mobilenurse;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

public class ARNurseView extends View {
    private static final String TAG = "ARViewNurse::View";
    private Bitmap bmpNurse, bmpNurse2;

    public ARNurseView(Context context)
    {
        super(context);
        prepareImages();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if(bmpNurse2 != null){
            canvas.drawBitmap(bmpNurse2, coordNurse.x, coordNurse.y, null);
        }
    }

    private void prepareImages()
    {
        Resources r = getResources();
        bmpNurse = BitmapFactory.decodeResource(r, R.drawable.nurse_n);
        bmpNurse2 = Bitmap.createScaledBitmap(bmpNurse, 500, 1100, false);
        setNurseImg(new Point(1420, 50));
    }
    private Point coordNurse = null;
    public void setNurseImg(Point p)
    {
        coordNurse = p;
        invalidate();
    }
}