package com.t4j.mobilenurse;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.io.File;
import java.util.Date;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedFile;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SelectImageActivity extends Activity {

    private static final String    TAG                 = "SelectImage::Activity";
    private static final int IMAGE_CHOOSER_RESULTCODE = 1;

    private MenuItem               mItemMain;
    private MenuItem               mItemDirectImage;
    private MenuItem               mItemSelectImage;

    private Uri mPictureUri;

    public SelectImageActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    private void launchChooser() {
        // ギャラリーから選択
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);

        // カメラで撮影
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mPictureUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent i2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i2.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);

        // ギャラリー選択のIntentでcreateChooser()
        Intent chooserIntent = Intent.createChooser(i, "Pick Image");
        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { i2 });

        startActivityForResult(chooserIntent, IMAGE_CHOOSER_RESULTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "called onActivityResult");
        if (requestCode == IMAGE_CHOOSER_RESULTCODE) {

            if (resultCode != RESULT_OK) {
                if (mPictureUri != null) {
                    getContentResolver().delete(mPictureUri, null, null);
                    mPictureUri = null;
                }
                return;
            }

            // 画像を取得
            Uri result = (data == null) ? mPictureUri : data.getData();

            // 暫定 ファイルパスを取得 TODO カメラ->NG ギャラリー->OK
            String img_path = data.getDataString();
            if (img_path.indexOf("content:") == 0)
            {
                Uri uri = Uri.parse(img_path);
                Cursor cur = getContentResolver().query(uri, null, null, null, null);
                cur.moveToPosition(0);
                img_path = cur.getString(1);
                cur.close();
            }

            // JSONのパーサー
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(Date.class, new DateTypeAdapter())
                    .create();

            // RestAdapterの生成
            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint("http://mobilenurse.t4j.com:8080")
                    .setConverter(new GsonConverter(gson))
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setLog(new AndroidLog("=NETWORK="))
                    .build();

            // 非同期処理の実行
            adapter.create(RetroFitApi.class).updateMultipart(
                    new TypedFile("image/jpeg", new File(img_path)), "testupload")
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
                                ((TextView) findViewById(R.id.text)).setText(
                                        "rank:" + diagnose.diagnoses.get(0).rank +
                                                ", condition:" + diagnose.diagnoses.get(0).condition +
                                                ", score:" + diagnose.diagnoses.get(0).score);
                            }
                        }
                    });
            mPictureUri = null;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_selectimage);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchChooser();
            }
        });


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

    private void setMinFaceSize(float faceSize) {
    }
}
