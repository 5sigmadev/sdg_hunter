package com.fivesigmagames.sdghunter.repository.aws;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.S3Link;

/**
 * Created by ppanero on 17/10/2016.
 */

@DynamoDBTable(tableName = "PICTURES")
public class AWSShareItem {

    // CONSTANTS
    private static final String LAT_ATTR_NAME = "lat";
    private static final String LNG_ATTR_NAME = "lng";
    private static final String FILENAME_ATTR_NAME = "picture_name";
    private static final String PHOTO_ATTR_NAME = "photoS3";

    // VARS
    private String filename;
    private S3Link photo;
    private double latitude;
    private double longitude;


    @DynamoDBHashKey(attributeName = FILENAME_ATTR_NAME)
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @DynamoDBAttribute(attributeName = PHOTO_ATTR_NAME)
    public S3Link getPhoto() {
        return photo;
    }

    public void setPhoto(S3Link photo) {
        this.photo = photo;
    }

    @DynamoDBAttribute(attributeName = LAT_ATTR_NAME)
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute(attributeName = LNG_ATTR_NAME)
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // ATTRS

    public static String getLatAttrName() {
        return LAT_ATTR_NAME;
    }

    public static String getLngAttrName() {
        return LNG_ATTR_NAME;
    }

    public static String getFilenameAttrName() {
        return FILENAME_ATTR_NAME;
    }

    public static String getPhotoAttrName() {
        return PHOTO_ATTR_NAME;
    }
}
