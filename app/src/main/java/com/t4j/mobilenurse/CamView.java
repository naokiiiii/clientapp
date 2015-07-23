package com.t4j.mobilenurse;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class CamView extends SurfaceView implements SurfaceHolder.Callback
{
    CamView(Context context)
    {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Log.e(TAG, "CamView created");
    }
    public void surfaceCreated(SurfaceHolder holder)
    {
        try{
            mCamera = Camera.open();
            surfaceDestroyed = false;
            Log.e(TAG, "Camera surface created");
        }catch (Exception e){
        }
        try{
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            checkPreviewCallback();
        }catch(Throwable ex){
            surfaceDestroyed = true;
            Log.e(TAG, "Camera surface destroyed");
            if(mCamera != null){
                mCamera.release();
            }
            mCamera = null;
        }
    }
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        surfaceDestroyed = true;
        Log.e(TAG, "Camera surface destroyed");
        if(mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
    }
    public boolean isSurfaceDestroyed()
    {
        return surfaceDestroyed;
    }
    private void setRotation(Camera.Parameters params, int rotationValue)
    {
        try{
            Method rotateSet = Camera.class.getMethod("setDisplayOrientation", new Class[] {Integer.TYPE} );
            Object arguments[] = new Object[] { new Integer(rotationValue) };
            rotateSet.invoke(mCamera, arguments);
        }catch(NoSuchMethodException nsme){
        }catch(IllegalArgumentException e){
        }catch(IllegalAccessException e){
        }catch(InvocationTargetException e){
        }
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if(mCamera == null){
            return;
        }
        mCamera.stopPreview();
        Camera.Parameters params = mCamera.getParameters();
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT){
            params.set("orientation", "landscape");
            screenRotation = 0;
            setRotation(params, screenRotation);
        }else{
            params.set("orientation", "portrait");
            screenRotation = 90;
            setRotation(params, screenRotation);
        }
        mCamera.setParameters(params);
        try{
            mCamera.startPreview();
        }catch(RuntimeException re){
        }
    }
    public void setPreviewCallback(PreviewCallback cb)
    {
        Log.e(TAG, "Camera: setPreviewCallback");
        this.previewCallback = cb;
        checkPreviewCallback();
    }
    private void checkPreviewCallback()
    {
        if((mCamera == null) || (previewCallback == null) || surfaceDestroyed){
            return;
        }
        mCamera.setPreviewCallback(previewCallback);
    }
    private static final String TAG = "CamView";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean surfaceDestroyed = true;
    private int screenRotation = 0;
    private PreviewCallback previewCallback = null;
}