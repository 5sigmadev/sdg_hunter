package com.fivesigmagames.sdghunter.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.fivesigmagames.sdghunter.R;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        String picFullPath = getIntent().getStringExtra("pic_path");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(picFullPath,bmOptions);
        ImageView imageView = (ImageView) findViewById(R.id.share_activity_pic);
        imageView.setImageBitmap(bitmap);

    }
}
