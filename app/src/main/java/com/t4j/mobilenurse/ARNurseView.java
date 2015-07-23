package com.t4j.mobilenurse;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

import com.t4j.mobilenurse.R;

public class ARNurseView extends View {
    private static final String TAG = "ARViewNurse::View";
    public ARNurseView(Context context)
    {
        super(context);
        prepareImages();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if(bmpNurse != null){
            canvas.drawBitmap(bmpNurse, coordNurse.x, coordNurse.y, null);
        }
    }

    private Bitmap bmpNurse = null;
    private void prepareImages()
    {
        Resources r = getResources();
        bmpNurse = BitmapFactory.decodeResource(r, R.drawable.nurse);
        setCoordBomb(new Point(300, 200));
    }
    private Point coordNurse = null;
    public void setCoordBomb(Point p)
    {
        coordNurse = p;
        invalidate();
    }
}