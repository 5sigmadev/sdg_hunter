package com.fivesigmagames.sdghunter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.fivesigmagames.sdghunter.R;
import com.fivesigmagames.sdghunter.adapter.ShareGridAdapter;
import com.fivesigmagames.sdghunter.adapter.ShareItem;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShareFragment.OnShareFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShareFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareFragment extends Fragment {

    //CONSTANTS
    private static final String ARGS_PHOTOS = "photos";
    private static final int GRID_SPAN_COUNT_PORTRAIT = 3;

    //VARS
    private GridView gridView;
    private ShareGridAdapter gridAdapter;
    private ArrayList<ShareItem> mShareItemList = new ArrayList<>();

    //INTERFACES
    private OnShareFragmentInteractionListener mListener;


    public ShareFragment() {
        // Required empty public constructor
    }


    public static ShareFragment newInstance(ArrayList<ShareItem> shareItemList) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_PHOTOS, shareItemList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mShareItemList = getArguments().getParcelableArrayList(ARGS_PHOTOS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.photo_grid);
        // Set the adapter
        if (recyclerView != null) {
            Context context = recyclerView.getContext();
            if (mShareItemList.size() <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, GRID_SPAN_COUNT_PORTRAIT));
            }
            gridAdapter = new ShareGridAdapter(mShareItemList, mListener);
            recyclerView.setAdapter(gridAdapter);
        }

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShareFragmentInteractionListener) {
            mListener = (OnShareFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public ShareItem getShareItem(int position) {
         return this.gridAdapter.getItemAt(position);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnShareFragmentInteractionListener {
        void sharePicture(int position);
    }
}
