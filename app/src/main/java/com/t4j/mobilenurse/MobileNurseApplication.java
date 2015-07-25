package com.t4j.mobilenurse;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by fumitaka on 2015/07/25.
 */
public class MobileNurseApplication extends Application{
	private final String TAG = "APPLICATION";

	// 顔認識したイメージ
	Bitmap capturedImage = null;

	@Override
	public void onCreate() {
		//Application作成時
		Log.v(TAG, "--- onCreate() in ---");
	}

	@Override
	public void onTerminate() {
		//Application終了時
		Log.v(TAG,"--- onTerminate() in ---");
	}

	public void setCapturedImage(Bitmap bmp){
		this.capturedImage = bmp;
	}

	public Bitmap getCapturedImage(){
		return this.capturedImage;
	}

	public void clearCapturedImage(){
		this.capturedImage = null;
	}
}
