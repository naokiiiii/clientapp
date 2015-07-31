package com.t4j.mobilenurse;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedFile;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InspectionActivity extends Activity  {

    private static final String    TAG                 = "Inspection::Activity";
    private static final int REQUEST_CAPTURE_IMAGE = 100;

    private MenuItem               mItemMain;
    private MenuItem               mItemDirectImage;
//    private MenuItem               mItemSelectImage;
//    private MenuItem               mItemSelectMode;

    private ImageView capturedImageView;

    //private MediaPlayer mp = null;

    public InspectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        super.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.setContentView(R.layout.activity_inspection);

        MobileNurseApplication mobileNurseApplication = (MobileNurseApplication)this.getApplication();

        Bitmap bmpRsz = mobileNurseApplication.getCapturedImage();
        capturedImageView = (ImageView)findViewById(R.id.capturedImageView);


        //this.mp = MediaPlayer.create(this, R.raw.good);
	    // save
	    // sdcardフォルダを指定
	    File root = Environment.getExternalStorageDirectory();
	    FileOutputStream fos = null;
        String fileName = "sample.jpg";
	    File file = null;
	    try {
            // 画像を256x256にリサイズ
            bmpRsz = Bitmap.createScaledBitmap(bmpRsz, 256, 256, false);
            capturedImageView.setImageBitmap(bmpRsz);

		    file = new File(root, fileName);
		    fos = new FileOutputStream(file);
		    bmpRsz.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

		    // TOOD ここでrest (通信用のthreadと通信中の画面更新のthreadを分けておきたいのでその辺を調査）
            this.rest(root, fileName);
	    }catch (Exception e) {
		    Log.e("Error", "" + e.toString());
	    }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemMain = menu.add("Main");
        mItemDirectImage = menu.add("Direct Image");
//        mItemSelectImage = menu.add("Select Image");
//        mItemSelectMode = menu.add("Select Mode");
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
        }
//        else if (item == mItemSelectImage) {
//            Intent intent = new Intent(this, SelectImageActivity.class);
//            startActivity(intent);
//        } else if (item == mItemSelectMode) {
//            Intent intent = new Intent(this, SelectModeActivity.class);
//            startActivity(intent);
//        }
        return true;
    }

    /**
     * serverに顔認した結果を転送。
     * @param root
     * @param fileName
     */
	private void rest(File root, String fileName) {
		// JSONのパーサー
		Gson gson = new GsonBuilder()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.registerTypeAdapter(Date.class, new DateTypeAdapter())
				.create();
		// RestAdapterの生成
		RestAdapter adapter = new RestAdapter.Builder()
//                .setEndpoint("http://10.222.0.13:80")
                .setEndpoint("http://mobilenurse.t4j.com:80")
				.setConverter(new GsonConverter(gson))
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setLog(new AndroidLog("=NETWORK="))
				.build();

		// 非同期処理の実行
		adapter.create(RetroFitApi.class).updateMultipart(
				new TypedFile("image/jpeg", new File(root, fileName)), "testupload")
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<DiagnoseResponse>() {
					@Override
					public void onCompleted() {
						Log.d("SelectActivity", "onCompleted()");
					}

					@Override
					public void onError(Throwable e) {
						Log.e("SelectActivity", "Error : " + e.toString());
					}

                    /**
                     * 正常にサーバの応答を取得できたら
                     * 結果をtextViewに表示。
                     *
                     * @param diagnose
                     */
					@Override
					public void onNext(DiagnoseResponse diagnose) {
						Log.d("SelectActivity", "onNext()");
						if (diagnose != null) {
                            // TODO 診断結果に応じて色々やるのはここで！！

                            // TextViewに診断結果を表示する。
//							((TextView) findViewById(R.id.inspection_result)).setText(
//                                    diagnose.message + " " +
//									"rank:" + diagnose.diagnoses.get(0).rank +
//											", condition:" + diagnose.diagnoses.get(0).condition +
//											", score:" + diagnose.diagnoses.get(0).score);
                            ((TextView) findViewById(R.id.inspection_result)).setText(diagnose.message);
						}
                        //mp.start();
					}
				});
	}
}
