package com.t4j.mobilenurse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class DirectImageActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {

    private static final String    TAG                 = "DirectImage::Activity";
	private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private MenuItem               mItemMain;
    private MenuItem               mItemDirectImage;
    private MenuItem               mItemSelectImage;

	private CameraBridgeViewBase mOpenCvCameraView;
	private CascadeClassifier mJavaDetector;
	private ARNurseView mARNuerseView;

	private Mat                    mRgba;
	private Mat                    mGray;
	private float                  mRelativeFaceSize   = 0.5f;
	private int                    mAbsoluteFaceSize   = 0;
	private File                   mCascadeFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.setContentView(R.layout.activity_directimage);

	    mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);

		mARNuerseView = new ARNurseView(this);
		addContentView(mARNuerseView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemMain = menu.add("Main");
        mItemDirectImage = menu.add("Direct Image");
        mItemSelectImage = menu.add("Select Image");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemMain) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (item == mItemDirectImage) {
            Intent intent = new Intent(this, DirectImageActivity.class);
            startActivity(intent);
        } else if (item == mItemSelectImage) {
            Intent intent = new Intent(this, SelectImageActivity.class);
            startActivity(intent);
        }
        return true;
    }

	public Mat onCameraFrame(Mat inputFrame) {

		inputFrame.copyTo(mRgba);
		Imgproc.cvtColor(inputFrame, mGray, Imgproc.COLOR_RGBA2GRAY);

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
			//mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
		}

		MatOfRect faces = new MatOfRect();

		if (mJavaDetector != null){
			mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
		}

		Rect[] facesArray = faces.toArray();

		// 顔を検知したら、サーバへ画像を送信するためのアクティビティ(inspectionActivity)に遷移する。
		if( facesArray.length > 0 ) {
			// フレームデータをbitmapに変換
			Bitmap dsc = Bitmap.createBitmap(inputFrame.width(), inputFrame.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(inputFrame, dsc);

			// 画像を縮小 1/2サイズ (巨大なデータをintentで受け渡すと落ちることがあるので、とりあえず1/2に縮小）
			Matrix matrix = new Matrix();
			matrix.postScale(0.5f, 0.5f);
			Bitmap bmpRsz = Bitmap.createBitmap(dsc, 0, 0, dsc.getWidth(), dsc.getHeight(), matrix, true);

			// intentに縮小した画像を設定してinspectionActivityへ遷移
			Intent intent = new Intent(this, InspectionActivity.class);
			intent.putExtra("data", bmpRsz);
			startActivity(intent);

			Log.i(TAG, "GET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		}
		return mRgba;
	}

	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");

					try {
						// load cascade file from application resources
						InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
						//InputStream is = getResources().openRawResource(R.raw.haarcascade_mcs_mouth);

						File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
						mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
						//mCascadeFile = new File(cascadeDir, "haarcascade_mcs_mouth.xml");

						FileOutputStream os = new FileOutputStream(mCascadeFile);

						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						is.close();
						os.close();

						mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
						if (mJavaDetector.empty()) {
							Log.e(TAG, "Failed to load cascade classifier");
							mJavaDetector = null;
						} else
							Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

						cascadeDir.delete();

					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
					}

					mOpenCvCameraView.enableView();
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
		}
	};

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}
}