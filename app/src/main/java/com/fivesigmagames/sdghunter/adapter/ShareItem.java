package com.fivesigmagames.sdghunter.adapter;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ppanero on 02/10/16.
 */

public class ShareItem implements Parcelable {
    private String title;
    private String fullPath;

    public ShareItem(String title, String fullPath) {
        super();
        this.title = title;
        this.fullPath = fullPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    //PARCELABLE

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(fullPath);
    }

    protected ShareItem(Parcel in) {
        title = in.readString();
        fullPath = in.readString();
    }

    public static final Creator<ShareItem> CREATOR = new Creator<ShareItem>() {
        @Override
        public ShareItem createFromParcel(Parcel in) {
            return new ShareItem(in);
        }

        @Override
        public ShareItem[] newArray(int size) {
            return new ShareItem[size];
        }
    };
}
