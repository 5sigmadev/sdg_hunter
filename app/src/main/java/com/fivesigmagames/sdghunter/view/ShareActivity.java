package com.fivesigmagames.sdghunter.view;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fivesigmagames.sdghunter.R;

import java.io.File;
import java.util.List;

public class ShareActivity extends AppCompatActivity {

    // CONSTANTS
    private static final String INSTAGRAM_NOT_INSTALLED_ERROR_MESSAGE = "Instagram is not installed. Please install it in order to share.";
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred when trying to share" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        final String picFullPath = getIntent().getStringExtra("pic_path");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(picFullPath,bmOptions);
        ImageView imageView = (ImageView) findViewById(R.id.share_activity_pic);
        imageView.setImageBitmap(bitmap);

        Button instaBtn = (Button) findViewById(R.id.btn_share_insta);
        instaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = ((TextView)findViewById(R.id.share_text)).getText().toString();
                shareInstagram(picFullPath, text);
            }
        });

        Button fbBtn = (Button) findViewById(R.id.btn_share_fb);
        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = ((TextView)findViewById(R.id.share_text)).getText().toString();
                shareFacebook(picFullPath, text);
            }
        });

        Button twtBtn = (Button) findViewById(R.id.btn_share_twt);
        twtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = ((TextView)findViewById(R.id.share_text)).getText().toString();
                shareTwitter(picFullPath, text);
            }
        });
    }

    public void shareFacebook(String fullPicPath, String captionText) {

    }

    public void shareInstagram(String fullPicPath, String captionText) {
        String[] instagramPackages = new String[]{"com.instagram.android"};
        if(isPackageInstalled(instagramPackages)){
            String type = "image/*";
            Intent share = findIntentPackage(instagramPackages, type);

            if(share != null) {
                share.setAction(Intent.ACTION_SEND);
                File media = new File(fullPicPath);
                Uri uri = Uri.fromFile(media);
                share.putExtra(Intent.EXTRA_STREAM, uri);
                /*
                    Note that Instagram has forbidden the use of caption from third party apps.
                 */
                //share.putExtra(Intent.EXTRA_TEXT,captionText);
                startActivity(share);
            }
            else {
                Toast.makeText(this, UNEXPECTED_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(this, INSTAGRAM_NOT_INSTALLED_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }
    }

    public void shareTwitter(String fullPicPath, String captionText) {
        String[] twitterPackages = new String[]{
                // package // name - nb installs (thousands)
                "com.twitter.android", // official - 10 000
                "com.twidroid", // twidroid - 5 000
                "com.handmark.tweetcaster", // Tweecaster - 5 000
                "com.thedeck.android" }; // TweetDeck - 5 000 };
        if(isPackageInstalled(twitterPackages)){

            String type = "image/*";
            Intent share = findIntentPackage(twitterPackages, type);

            if(share != null) {
                share.setAction(Intent.ACTION_SEND);
                File media = new File(fullPicPath);
                Uri uri = Uri.fromFile(media);
                share.putExtra(Intent.EXTRA_STREAM, uri);
                //TODO: Campaign Hashtag? suppress user text and put hashtag?
                share.putExtra(Intent.EXTRA_TEXT,captionText);
                startActivity(share);
            }
            else {
                Toast.makeText(this, UNEXPECTED_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        }
        else{
            Intent tweet = new Intent(Intent.ACTION_VIEW);
            tweet.setData(Uri.parse("http://twitter.com/?status=" + Uri.encode(captionText)));
            startActivity(tweet);
        }
    }

    private boolean isPackageInstalled(String[] packageList) {
        boolean installed = false;

        int i = 0;
        while (!installed && i < packageList.length) {
            try {
                ApplicationInfo info = getPackageManager().getApplicationInfo(packageList[i], 0);
                installed = true;
            } catch (PackageManager.NameNotFoundException e) {
                installed = false;
            }
            ++i;
        }
        return installed;
    }

    private Intent findIntentPackage(String[] possiblePackages, String type) {

        Intent intent = new Intent();
        intent.setType(type);
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (int i = 0; i < possiblePackages.length; i++) {
            for (ResolveInfo resolveInfo : list) {
                String p = resolveInfo.activityInfo.packageName;
                if (p != null && p.startsWith(possiblePackages[i])) {
                    intent.setPackage(p);
                    return intent;
                }
            }
        }
        return null;
    }
}
