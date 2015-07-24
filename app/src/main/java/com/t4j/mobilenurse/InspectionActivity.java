package com.t4j.mobilenurse;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

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
        capturedImageView.setImageBitmap(bmp);

        // TOOD ここでrest (通信用のthreadと通信中の画面更新のthreadを分けておきたいのでその辺を調査）
    }


    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        if( REQUEST_CAPTURE_IMAGE == requestCode && resultCode == Activity.RESULT_OK ){
            Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
            capturedImageView.setImageBitmap(capturedImage);
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
}
