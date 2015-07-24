package com.t4j.mobilenurse;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
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

public class InspectionActivity extends Activity {

    private static final String    TAG                 = "Inspection::Activity";
    private static final int REQUEST_CAPTURE_IMAGE = 100;

    private MenuItem               mItemMain;
    private MenuItem               mItemDirectImage;
    private MenuItem               mItemSelectImage;
    private MenuItem               mItemSelectMode;

    private ImageView capturedImageView;

    public InspectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.setContentView(R.layout.activity_inspection);

        // DirectImageActivityから渡された画像をcapturedImageViewに設定。
        Intent intent = this.getIntent();
        Bitmap bmp = intent.getParcelableExtra("data");

        capturedImageView = (ImageView)findViewById(R.id.capturedImageView);

        // 1/2で受け取っているのでここで元のサイズに復元させる
        Matrix matrix = new Matrix();
        matrix.postScale(4.0f, 4.0f);
        Bitmap bmpRsz = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        capturedImageView.setImageBitmap(bmpRsz);

	    // save
	    // sdcardフォルダを指定
	    File root = Environment.getExternalStorageDirectory();
	    FileOutputStream fos = null;
	    File file = null;
	    try {
		    file = new File(root, "sample.jpg");
		    fos = new FileOutputStream(file);
		    bmpRsz.compress(Bitmap.CompressFormat.JPEG, 100, fos);

		    // TOOD ここでrest (通信用のthreadと通信中の画面更新のthreadを分けておきたいのでその辺を調査）
		    this.rest(file);
		    fos.close();
	    }catch (Exception e) {
		    Log.e("Error", "" + e.toString());
	    }
    }


    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        if( REQUEST_CAPTURE_IMAGE == requestCode && resultCode == Activity.RESULT_OK ){
            Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemMain = menu.add("Main");
        mItemDirectImage = menu.add("Direct Image");
        mItemSelectImage = menu.add("Select Image");
        mItemSelectMode = menu.add("Select Mode");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemMain) {
            Intent intent = new Intent(this, InspectionActivity.class);
            startActivity(intent);
        } else if (item == mItemDirectImage) {
            Intent intent = new Intent(this, DirectImageActivity.class);
            startActivity(intent);
        } else if (item == mItemSelectImage) {
            Intent intent = new Intent(this, SelectImageActivity.class);
            startActivity(intent);
        } else if (item == mItemSelectMode) {
            Intent intent = new Intent(this, SelectModeActivity.class);
            startActivity(intent);
        }
        return true;
    }

	private void rest(File file) {
		// JSONのパーサー
		Gson gson = new GsonBuilder()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.registerTypeAdapter(Date.class, new DateTypeAdapter())
				.create();
		// RestAdapterの生成
		RestAdapter adapter = new RestAdapter.Builder()
				.setEndpoint("http://10.222.0.13:8080")
				.setConverter(new GsonConverter(gson))
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setLog(new AndroidLog("=NETWORK="))
				.build();

		// 非同期処理の実行
		adapter.create(RetroFitApi.class).updateMultipart(
				new TypedFile("image/jpeg", file), "testupload")
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

					@Override
					public void onNext(DiagnoseResponse diagnose) {
						Log.d("SelectActivity", "onNext()");
						if (diagnose != null) {
							((TextView) findViewById(R.id.textView4)).setText(
									"runk:" + diagnose.diagnoses.get(0).runk +
											", condition:" + diagnose.diagnoses.get(0).condition +
											", score:" + diagnose.diagnoses.get(0).score);
						}
					}
				});
	}
}
