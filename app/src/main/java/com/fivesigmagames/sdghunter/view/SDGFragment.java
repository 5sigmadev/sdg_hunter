package com.fivesigmagames.sdghunter.view;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

    public void showSdgDescription(int position) {
        SDGItem sdgItem = this.mSDGItemList.get(position);
        String formattedText = "";
        String[] descParts = sdgItem.getDescription().split("\t\t");
        if(descParts.length == 1) {
            for (String descPart : descParts) {
                String[] parts = descPart.split("\n");
                for(String part : parts){
                    formattedText = formattedText.concat("\u2022\t").concat(part).concat("\n\n");
                }
            }
        }
        else{
            if(descParts.length > 1) {
                for (String descPart : descParts) {
                    if(!descPart.equals("")) {
                        String[] parts = descPart.split("\t");
                        formattedText = formattedText.concat(parts[0]).concat(":\n\n");
                        if(parts.length > 1) {
                            String[] textParts = parts[1].split("\n");
                            for (String part : textParts) {
                                formattedText = formattedText.concat("\u2022\t").concat(part).concat("\n");
                            }
                        }
                        formattedText = formattedText.concat("\n");
                    }
                }
            }
        }

        this.buildDescriptionDialog(sdgItem.getTitle(), formattedText);
    }

    private void buildDescriptionDialog(String title, String description) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(description)
                .setCancelable(false)
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public interface OnSDGFragmentInteractionListener {
        void sdgPicDescription(int position);
    }
}
