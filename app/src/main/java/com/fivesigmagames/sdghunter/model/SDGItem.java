package com.fivesigmagames.sdghunter.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ppanero on 28/11/2016.
 */

public class SDGItem  implements Parcelable{


    private String title;
    private String description;

    public SDGItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static Creator<SDGItem> getCREATOR() {
        return CREATOR;
    }

    //PARCELABLE

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);

    }

    protected SDGItem(Parcel in) {
        title = in.readString();
        description = in.readString();
    }

    public static final Parcelable.Creator<SDGItem> CREATOR = new Parcelable.Creator<SDGItem>() {
        @Override
        public SDGItem createFromParcel(Parcel in) {
            return new SDGItem(in);
        }

        @Override
        public SDGItem[] newArray(int size) {
            return new SDGItem[size];
        }
    };
}

