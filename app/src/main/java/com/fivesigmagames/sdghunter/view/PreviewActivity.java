package com.fivesigmagames.sdghunter.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.fivesigmagames.sdghunter.R;

public class PreviewActivity extends AppCompatActivity {

    // CONSTANTS
    private static final int RESULT_PHOTO_RETAKE = 0;
    private static final int RESULT_PHOTO_SHARE = 1;
    private static final int RESULT_PHOTO_SAVE = 2;
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    private static final String TAG = "SDG [Preview Activity]";

    // VARS
    private String mCurrentFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        mCurrentFilePath = (String) getIntent().getExtras().get("pic_path");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        // Get the dimensions of the View
        int targetW = WIDTH;
        int targetH = HEIGHT;

        // Get the dimensions of the bitmap
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentFilePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentFilePath,bmOptions);
        ImageView imagePreview = (ImageView) findViewById(R.id.preview_image);
        Log.d(TAG, "Loading preview of " + mCurrentFilePath);
        if(bitmap != null) {
            imagePreview.setImageBitmap(BitmapUtils.rotateImage(bitmap, BitmapUtils.getRotationAngle(mCurrentFilePath)));
        }
        else{
            Log.d(TAG, "Error loading " + mCurrentFilePath + ". Bitmap was null");
        }

        Button retakeBtn = (Button) findViewById(R.id.btn_preview_retake);
        retakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(RESULT_PHOTO_RETAKE);
            }
        });
        Button shareBtn = (Button) findViewById(R.id.btn_preview_share);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(RESULT_PHOTO_SHARE);
            }
        });
        Button saveBtn = (Button) findViewById(R.id.btn_preview_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(RESULT_PHOTO_SAVE);
            }
        });
    }

    public void finish(int resultCode) {
        Activity mParent = getParent();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("pic_path", mCurrentFilePath);
        if (mParent == null) {
            setResult(resultCode, returnIntent);
            finish();
        } else {
            mParent.setResult(resultCode, returnIntent);
            mParent.finishFromChild(this);
        }
    }
}
