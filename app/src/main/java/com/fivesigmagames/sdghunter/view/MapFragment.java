package com.fivesigmagames.sdghunter.view;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fivesigmagames.sdghunter.R;
import com.fivesigmagames.sdghunter.adapter.PhotoViewAdapter;
import com.fivesigmagames.sdghunter.model.ShareItem;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.io.File;
import java.util.ArrayList;

import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    // CONSTANTS
    private static final String TAG = "SDG [Map Fragment]";
    private static final String ARGS_PHOTOS = "photos";
    private static final String ARGS_CENTER = "center";

    // VARS
    private MapView mapView;
    private MapboxMap mMapboxMap;
    private ArrayList<ShareItem> mShareItemList = new ArrayList<>();
    private Location mCenter;

    // INTERFACES
    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }


    public static MapFragment newInstance(ArrayList<ShareItem> shareItemList, Location center) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_PHOTOS, shareItemList);
        args.putParcelable(ARGS_CENTER, center);
        fragment.setArguments(args);
        Log.d(TAG, "Fragment instantiated");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapboxMap = null;
        mapView = null;

        MapboxAccountManager.start(getContext(), getResources().getString(R.string.mapbox_api_key));

        if (getArguments() != null) {
            mShareItemList = getArguments().getParcelableArrayList(ARGS_PHOTOS);
            mCenter = getArguments().getParcelable(ARGS_CENTER);
            Log.d(TAG, "Fragment created with data");
        }
        else{
            Log.d(TAG, "Fragment created without data");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // This contains the MapView in XML and needs to be called after the account manager
        mapView = (MapView) rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;
                mMapboxMap.setInfoWindowAdapter(new PhotoViewAdapter(inflater));
                if(mCenter != null) {
                    mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mCenter.getLatitude(), mCenter.getLongitude()), 15)
                    );
                }
                if(mShareItemList != null) {
                    for (ShareItem item : mShareItemList) {
                        String title = getFullPathForTitle(item);
                        mMapboxMap.addMarker(new MarkerViewOptions()
                                .position(new LatLng(item.getLatitude(), item.getLongitude()))
                                .title(title));
                    }
                }
            }
        });
        return rootView;
    }

    @NonNull
    private String getFullPathForTitle(ShareItem item) {
        String title = item.getFullPath();
        if(title == null || title == ""){
            String sdgPictures = getResources().getString(R.string.sdg_pictures_path)
                    .concat(File.separator).concat(
                    getResources().getString(R.string.sdg_taken_pictures_path)
                    );
            title = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath().concat(File.separator).concat(sdgPictures)
                    .concat(File.separator).concat(item.getTitle());


        }
        return title;
    }

    @Override
    public void onDestroyView(){
        mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public void updateMap(ArrayList<ShareItem> shareItemList, Location center, boolean clear){

        if (center != null) {
            mCenter = center;
            if(mMapboxMap != null) {
                mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mCenter.getLatitude(), mCenter.getLongitude()),
                        (float) mMapboxMap.getCameraPosition().zoom)
                );
            }
        }
        if (shareItemList != null) {
            mShareItemList = shareItemList;
            if(mMapboxMap != null){
                if(clear) {
                    mMapboxMap.clear();
                }
                for (ShareItem item : mShareItemList) {
                    String title = getFullPathForTitle(item);
                    mMapboxMap.addMarker(new MarkerViewOptions()
                            .position(new LatLng(item.getLatitude(), item.getLongitude()))
                            .title(title));
                }
            }
        }
    }

    public void updateMap(ShareItem item){
        if (mShareItemList != null) {
            mShareItemList.add(item);
            if(mMapboxMap != null){
                String title = getFullPathForTitle(item);
                mMapboxMap.addMarker(new MarkerViewOptions()
                        .position(new LatLng(item.getLatitude(), item.getLongitude()))
                        .title(title));
            }
        }
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
