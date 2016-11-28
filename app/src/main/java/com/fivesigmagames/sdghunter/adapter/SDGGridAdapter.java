package com.fivesigmagames.sdghunter.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fivesigmagames.sdghunter.R;
import com.fivesigmagames.sdghunter.model.SDGItem;
import com.fivesigmagames.sdghunter.view.BitMapUtils;
import com.fivesigmagames.sdghunter.view.SDGFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by ppanero on 28/11/2016.
 */

public class SDGGridAdapter extends RecyclerView.Adapter<SDGGridAdapter.ViewHolder>{

    // CONSTANTS
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;
    private static final String TAG = "SDG [ShareGridAdapter]";
    // VARS
    private ArrayList<SDGItem> mData = new ArrayList<SDGItem>();
    private Context context;


    private final SDGFragment.OnSDGFragmentInteractionListener mListener;

    public SDGGridAdapter(ArrayList data, SDGFragment.OnSDGFragmentInteractionListener listener, Context ctx) {
        if(data != null) {
            mData = data;
        }
        else{
            mData = new ArrayList<>();
        }
        mListener = listener;
        context = ctx;
    }

    @Override
    public SDGGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_photo_layout, parent, false);
        return new SDGGridAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SDGGridAdapter.ViewHolder holder, int position) {

        if(mData.get(position) != null) {
            int id = getIDFromPosition(position);
            // Get the dimensions of the View
            int targetW = WIDTH;
            int targetH = HEIGHT;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(), id, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap =  BitmapFactory.decodeResource(context.getResources(), id, bmOptions);
            holder.image.setImageBitmap(bitmap);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (null != mListener) {
                                                        // Notify the active callbacks interface (the activity, if the
                                                        // fragment is attached to one) that an item has been selected.
                                                        mListener.sdgPicDescription(holder.getAdapterPosition());
                                                    }
                                                }
                                            }
            );
        }
        else {
            holder.mView.setVisibility(View.INVISIBLE);
        }
    }

    private int getIDFromPosition(int position) {
        switch(position){
            case 0:
                return R.drawable.sdg_icons_1;
            case 1:
                return R.drawable.sdg_icons_2;
            case 2:
                return R.drawable.sdg_icons_3;
            case 3:
                return R.drawable.sdg_icons_4;
            case 4:
                return R.drawable.sdg_icons_5;
            case 5:
                return R.drawable.sdg_icons_6;
            case 6:
                return R.drawable.sdg_icons_7;
            case 7:
                return R.drawable.sdg_icons_8;
            case 8:
                return R.drawable.sdg_icons_9;
            case 9:
                return R.drawable.sdg_icons_10;
            case 10:
                return R.drawable.sdg_icons_11;
            case 11:
                return R.drawable.sdg_icons_12;
            case 12:
                return R.drawable.sdg_icons_13;
            case 13:
                return R.drawable.sdg_icons_14;
            case 14:
                return R.drawable.sdg_icons_15;
            case 15:
                return R.drawable.sdg_icons_16;
            case 16:
                return R.drawable.sdg_icons_17;
            case 17:
                return R.drawable.sdg_icons_18;
        }
        return R.drawable.sdg_icons_18;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public SDGItem getItemAt(int position){
        if(position <= mData.size()){
            return mData.get(position);
        }
        return null;
    }

    public void addItem(SDGItem item) {
        if(item != null) {
            this.mData.add(item);
        }
        else{
            Log.d(TAG, "Not adding null item to grid view data list");
        }
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

        private static int getDrawableId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
