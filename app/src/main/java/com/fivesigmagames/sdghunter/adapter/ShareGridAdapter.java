package com.fivesigmagames.sdghunter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fivesigmagames.sdghunter.R;
import com.fivesigmagames.sdghunter.view.ShareFragment;

import java.util.ArrayList;

/**
 * Created by ppanero on 02/10/16.
 */

public class ShareGridAdapter extends RecyclerView.Adapter<ShareGridAdapter.ViewHolder> {

    private ArrayList<ShareItem> mData = new ArrayList<ShareItem>();


    private final ShareFragment.OnShareFragmentInteractionListener mListener;

    public ShareGridAdapter(ArrayList data, ShareFragment.OnShareFragmentInteractionListener listener) {
        mData = data;
        mListener = listener;
    }

    @Override
    public ShareGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_photo_layout, parent, false);
        return new ShareGridAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.imageTitle.setText(mData.get(position).getTitle());
        holder.image.setImageBitmap(mData.get(position).getImage());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.sharePicture(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public ShareItem getItemAt(int position){
        if(position <= mData.size()){
            return mData.get(position);
        }
        return null;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        TextView imageTitle;
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imageTitle = (TextView) view.findViewById(R.id.text);
            image = (ImageView) view.findViewById(R.id.image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + imageTitle.getText() + "'";
        }
    }
}


