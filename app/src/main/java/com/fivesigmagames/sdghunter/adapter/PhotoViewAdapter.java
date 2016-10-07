package com.fivesigmagames.sdghunter.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.fivesigmagames.sdghunter.R;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.io.File;

/**
 * Created by ppanero on 04/10/16.
 */

public class PhotoViewAdapter implements MapboxMap.InfoWindowAdapter {

    LayoutInflater inflater=null;

    public PhotoViewAdapter(LayoutInflater inflater) {
        this.inflater=inflater;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        View rootView = inflater.inflate(R.layout.view_marker, null);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(marker.getTitle(),bmOptions);
        ((ImageView)rootView.findViewById(R.id.marker_image)).setImageBitmap(bitmap);

        return rootView;
    }
}
