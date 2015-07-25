package com.t4j.mobilenurse;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

public class ARNurseCommentView extends View {
    private static final String TAG = "ARViewNurseComment::View";
    private Bitmap cmt;

    public ARNurseCommentView(Context context)
    {
        super(context);
        prepareImages();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if(cmt != null){
            canvas.drawBitmap(cmt, coordNurse.x, coordNurse.y, null);
        }
    }

    private void prepareImages()
    {
        Resources r = getResources();
        cmt = BitmapFactory.decodeResource(r, R.drawable.comment);
        setNurseImg(new Point(30, 500));
    }
    private Point coordNurse = null;
    public void setNurseImg(Point p)
    {
        coordNurse = p;
        invalidate();
    }
}