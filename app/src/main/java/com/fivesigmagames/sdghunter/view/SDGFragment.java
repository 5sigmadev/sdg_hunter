package com.fivesigmagames.sdghunter.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fivesigmagames.sdghunter.R;
import com.fivesigmagames.sdghunter.adapter.SDGGridAdapter;
import com.fivesigmagames.sdghunter.adapter.ShareGridAdapter;
import com.fivesigmagames.sdghunter.model.SDGItem;
import com.fivesigmagames.sdghunter.model.ShareItem;

import java.util.ArrayList;

public class SDGFragment extends Fragment {

    //CONSTANTS
    private static final String TAG = "SDG [SDG Fragment]";
    private static final String ARGS_PHOTOS = "photos";
    private static final int GRID_SPAN_COUNT_PORTRAIT = 3;

    //VARS
    private SDGGridAdapter gridAdapter;
    private ArrayList<SDGItem> mSDGItemList = new ArrayList<>();

    //INTERFACES
    private OnSDGFragmentInteractionListener mListener;


    public SDGFragment() {
        // Required empty public constructor
    }

    public static SDGFragment newInstance(ArrayList<SDGItem> sdgItemList) {
        SDGFragment fragment = new SDGFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_PHOTOS, sdgItemList);
        fragment.setArguments(args);
        Log.d(TAG, "Fragment instantiated");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSDGItemList = getArguments().getParcelableArrayList(ARGS_PHOTOS);
            Log.d(TAG, "Fragment created with data");
        }
        else{
            Log.d(TAG, "Fragment created without data");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sdg, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.photo_grid);
        // Set the adapter
        if (recyclerView != null) {
            Context context = recyclerView.getContext();
            recyclerView.setLayoutManager(new GridLayoutManager(context, GRID_SPAN_COUNT_PORTRAIT));
            gridAdapter = new SDGGridAdapter(mSDGItemList, mListener, context);
            recyclerView.setAdapter(gridAdapter);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {


        super.onAttach(context);
        if (context instanceof OnSDGFragmentInteractionListener) {
            mListener = (OnSDGFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSDGFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateDescription(int position) {
        //TODO
    }

    public interface OnSDGFragmentInteractionListener {
        void sdgPicDescription(int position);
    }
}
