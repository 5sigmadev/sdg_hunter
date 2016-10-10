package com.fivesigmagames.sdghunter.view.ar;

import com.fivesigmagames.sdghunter.R;
import com.unity3d.player.*;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;

public class UnityPlayerActivity extends Activity {

    // CONSTANTS
    private static final int RESULT_PHOTO_TAKEN = 4;
    private static final String PICTURE = "PICTURE";
    private static final String TAG = "SDG [Unity Camera]";

    // VARS
	protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    private Uri mUri;
    private boolean mUsed = false;


	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
        mUri = getIntent().getExtras().getParcelable(MediaStore.EXTRA_OUTPUT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

		mUnityPlayer = new UnityPlayer(this);
        mUnityPlayer.getView().setBackgroundColor(getResources().getColor(R.color.white));
		setContentView(mUnityPlayer);
		mUnityPlayer.requestFocus();
        mUnityPlayer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                finish(RESULT_PHOTO_TAKEN);
                return true;
            }
        });
	}

	// Quit Unity
	@Override protected void onDestroy ()
	{
		mUnityPlayer.quit();
		super.onDestroy();
	}

	// Pause Unity
	@Override protected void onPause()
	{
		super.onPause();
		mUnityPlayer.pause();
	}

	// Resume Unity
	@Override protected void onResume()
	{
		super.onResume();
		mUnityPlayer.resume();
	}

	// This ensures the layout will be correct.
	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}

	// Notify Unity of the focus change.
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}

	// For some reason the multiple keyevent type is not supported by the ndk.
	// Force event injection by overriding dispatchKeyEvent().
	@Override public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.injectEvent(event);
		return super.dispatchKeyEvent(event);
	}

	// Pass any events not handled by (unfocused) views straight to UnityPlayer
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
	@Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
	@Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
	/*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }


    public void finish(int resultCode) {
        savePictureFromBitmap();
        Activity mParent = getParent();
        Intent returnIntent = new Intent();
        if (mParent == null) {
            setResult(resultCode, returnIntent);
            finish();
        } else {
            mParent.setResult(resultCode, returnIntent);
            mParent.finishFromChild(this);
        }
    }

    // Unity Player Camera
    public void savePictureFromBitmap() {
        mUnityPlayer.setDrawingCacheEnabled(true);
        mUnityPlayer.buildDrawingCache(true);
        Bitmap picture = Bitmap.createBitmap(mUnityPlayer.getDrawingCache());
        File imageFile = new File(getExternalStorageDirectory(), mUri.getPath());

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            int quality = 40;
            picture.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Error saving file. File: " + imageFile.getAbsolutePath() + " not found.");
        } catch (IOException e) {
            Log.d(TAG, "Error saving file. Unexpected IO error");
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, "Error saving file. Unexpected IO error");
                }
            }
        }
        mUnityPlayer.setDrawingCacheEnabled(false);
        Log.d(TAG, "File  " + imageFile.getAbsolutePath() + " saved");
    }
}
