package com.fivesigmagames.sdghunter.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ppanero on 02/10/16.
 */

public class ShareItem implements Parcelable {

    private long _id;
    private String title;
    private String fullPath;
    private double latitude;
    private double longitude;

    public ShareItem(String title, String fullPath, double lat, double lng) {
        super();
        this.title = title;
        this.fullPath = fullPath;
        this.latitude = lat;
        this.longitude = lng;
    }

    public ShareItem(long id, String title, String fullPath, double lat, double lng) {
        super();
        this._id = id;
        this.title = title;
        this.fullPath = fullPath;
        this.latitude = lat;
        this.longitude = lng;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
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
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }

    protected ShareItem(Parcel in) {
        title = in.readString();
        fullPath = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
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
