package com.fivesigmagames.sdghunter.view.ar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


/**
 * Created by ppanero on 11/10/16.
 */

public class SDGUnityPlayerActivity extends UnityPlayerActivity {

    // INSTANCE
    public static SDGUnityPlayerActivity instance;
    // CONSTANTS
    private static final int RESULT_PHOTO_TAKEN = 4;
    private static final String PICTURE = "PICTURE";
    private static final String TAG = "SDG [Extended UP]";

    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate started");
        super.onCreate(savedInstanceState);
        SDGUnityPlayerActivity.instance = this;
        Log.d(TAG, "onCreate finished");
    }

    // VARS
    private static String mNewFilePath;

    public void setNewFilePath(String filePath){
        mNewFilePath = filePath;
        Log.d(TAG, "Setting new file path");
    }

    public void finishActivity() {
        Log.d(TAG, "Exiting Unity Activity");
        Activity mParent = getParent();
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PICTURE, mNewFilePath);
        if (mParent == null) {
            setResult(RESULT_PHOTO_TAKEN, returnIntent);
            finish();
        } else {
            mParent.setResult(RESULT_PHOTO_TAKEN, returnIntent);
            mParent.finishFromChild(this);
        }
    }
}
