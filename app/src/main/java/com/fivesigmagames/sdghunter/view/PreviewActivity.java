package com.fivesigmagames.sdghunter.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.fivesigmagames.sdghunter.R;

public class PreviewActivity extends AppCompatActivity {

    // CONSTANTS
    private static final int RESULT_PHOTO_RETAKE = 0;
    private static final int RESULT_PHOTO_SHARE = 1;
    private static final int RESULT_PHOTO_SAVE = 2;

    // VARS
    private String mCurrentFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        mCurrentFilePath = (String) getIntent().getExtras().get("pic_path");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentFilePath,bmOptions);
        ImageView imagePreview = (ImageView) findViewById(R.id.preview_image);
        imagePreview.setImageBitmap(bitmap);

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
