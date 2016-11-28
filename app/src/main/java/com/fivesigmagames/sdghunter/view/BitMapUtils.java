package com.fivesigmagames.sdghunter.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ppanero on 08/10/16.
 */

public class BitMapUtils {

    // CONSTANTS
    private static final String TAG = "SDG [Bitmap Utils]";

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static int getRotationAngle(String photoPath){
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(photoPath);
        } catch (IOException e) {
            Log.e(TAG, "Unexpected error occurred when getting rotation angle.");
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                Log.d(TAG, "90 Degrees rotation needed");
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                Log.d(TAG, "180 Degrees rotation needed");
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                Log.d(TAG, "270 Degrees rotation needed");
                return 270;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                Log.d(TAG, "0 Degrees rotation needed");
                return 0;
        }
    }
}
