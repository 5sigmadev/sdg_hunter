package com.fivesigmagames.sdghunter.view.ar;

import com.unity3d.player.*;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;

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
		setContentView(mUnityPlayer);
		mUnityPlayer.requestFocus();
	}

	// Quit Unity
	@Override protected void onDestroy ()
	{
		finish(RESULT_PHOTO_TAKEN);
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

    public String getCurrentPhotoPath(){
        Log.d(TAG, "getCurrentPhotoPath called from Unity");
        return getExternalStorageDirectory().getAbsolutePath().concat(mUri.getPath());
    }

	public void finish(int resultCode) {
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
}
