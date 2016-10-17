package com.fivesigmagames.sdghunter.repository.aws;

import com.amazonaws.geo.model.GeoPoint;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.S3Link;

/**
 * Created by ppanero on 17/10/2016.
 */

@DynamoDBTable(tableName = "PICTURES")
public class AWSShareItem {

    // CONSTANTS
    private static final String LOCATION_ATTR_NAME = "location";
    private static final String FILENAME_ATTR_NAME = "picture_name";
    private static final String PHOTO_ATTR_NAME = "photoS3";

    // VARS
    private String filename;
    private S3Link photo;
    private GeoPoint location;

    @DynamoDBHashKey(attributeName = "picture_name")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @DynamoDBAttribute(attributeName = "photoS3")
    public S3Link getPhoto() {
        return photo;
    }

    public void setPhoto(S3Link photo) {
        this.photo = photo;
    }

    @DynamoDBAttribute(attributeName = LOCATION_ATTR_NAME)
    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public static String getLocationAttrName() {
        return LOCATION_ATTR_NAME;
    }

    public static String getFilenameAttrName() {
        return FILENAME_ATTR_NAME;
    }

    public static String getPhotoAttrName() {
        return PHOTO_ATTR_NAME;
    }
}
