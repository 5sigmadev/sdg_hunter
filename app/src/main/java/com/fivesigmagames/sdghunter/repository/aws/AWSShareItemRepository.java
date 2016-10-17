package com.fivesigmagames.sdghunter.repository.aws;

import android.content.Context;
import android.util.Log;

import com.amazonaws.geo.GeoDataManager;
import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.GeoPoint;
import com.amazonaws.geo.model.PutPointRequest;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.S3Link;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.fivesigmagames.sdghunter.model.ShareItem;

import java.io.File;

/**
 * Created by ppanero on 17/10/2016.
 */

public class AWSShareItemRepository {

    // CONSTANTS
    private static final String MY_BUCKET_NAME = "sdg-hunter";
    private static final String TAG = "SDG [AWS Repository]";
    private static final String TABLE_NAME = "PICTURES";
    // VARS
    private AWSDatabaseHelper awsDatabaseHelper;
    private GeoDataManager geoDataManager;


    public AWSShareItemRepository(Context context){
        awsDatabaseHelper = new AWSDatabaseHelper();
        awsDatabaseHelper.buildCredentialsProvider(context);
        if(awsDatabaseHelper.buildDynamoDBClient()) {
            GeoDataManagerConfiguration config = new GeoDataManagerConfiguration(
                    awsDatabaseHelper.getDdbClient(),
                    TABLE_NAME
            ).withGeoJsonAttributeName(AWSShareItem.getLocationAttrName());
            geoDataManager = new GeoDataManager(config);
        }
    }

    public void insert(ShareItem item){

        AttributeValue rangeKeyValue = new AttributeValue().withS(item.getTitle());
        final PutPointRequest putPointRequest = new PutPointRequest(
                new GeoPoint(item.getLatitude(), item.getLongitude()), rangeKeyValue
        );
        /*
        awsItem.setPhoto(dynamoDBMapper.createS3Link(MY_BUCKET_NAME, item.getFullPath()));
        Log.d(TAG, "AWS Item created");
        awsItem.getPhoto().uploadFrom(new File(item.getFullPath()));
        Log.d(TAG, "AWS photo uploaded to S3");
        */
        final Runnable runnable = new Runnable() {
            public void run() {
                geoDataManager.putPoint(putPointRequest);
                Log.d(TAG, "AWS Item saved in DynamoDB");
            }

        };
        Thread mThread = new Thread(runnable);
        mThread.start();
        Log.d(TAG, "AWS save thread started...");
    }
}
