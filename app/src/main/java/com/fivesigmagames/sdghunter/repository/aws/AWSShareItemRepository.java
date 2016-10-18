package com.fivesigmagames.sdghunter.repository.aws;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.geo.GeoDataManager;
import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.GeoPoint;
import com.amazonaws.geo.model.PutPointRequest;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.S3Link;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.fivesigmagames.sdghunter.model.ShareItem;

import java.io.File;

/**
 * Created by ppanero on 17/10/2016.
 */

public class AWSShareItemRepository implements AWSInitMapperAsyncTask.AsyncResponse{

    // CONSTANTS
    private static final String MY_BUCKET_NAME = "sdg-hunter";
    private static final String TAG = "SDG [AWS Repository]";

    // VARS
    private AWSDatabaseHelper awsDatabaseHelper;
    private DynamoDBMapper mapper;
    private AWSInitMapperAsyncTask initMapperAsyncTask;

    public static String getMyBucketName() {
        return MY_BUCKET_NAME;
    }

    public AWSShareItemRepository(Context context){
        awsDatabaseHelper = new AWSDatabaseHelper();
        awsDatabaseHelper.buildCredentialsProvider(context);
        if(awsDatabaseHelper.buildDynamoDBClient()) {
            initMapperAsyncTask = new AWSInitMapperAsyncTask(this);
            initMapperAsyncTask.execute(awsDatabaseHelper);
        }
    }

    public void insert(ShareItem item){
        if(item != null) {
            if(checkMapper()) {
                AWSTaskParams params = new AWSTaskParams(item, mapper);
                AWSUploadAsyncTask awsUploadAsyncTask = new AWSUploadAsyncTask();
                awsUploadAsyncTask.execute(params);
                Log.d(TAG, "AWS save thread started...");
            }
            else{
                Log.d(TAG, "Error mapper is null, not initialized properly");
            }
        }
        else{
            Log.d(TAG, "ShareItem is null, not uploading");
        }
    }

    private boolean checkMapper() {
        if(initMapperAsyncTask.getStatus() == AsyncTask.Status.FINISHED){
            if(mapper != null) return true;
            else{
                initMapperAsyncTask = new AWSInitMapperAsyncTask(this);
                initMapperAsyncTask.execute(awsDatabaseHelper);
            }
        }
        else if(initMapperAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            Log.d(TAG, "Still initializing mapper, wait a few seconds...");
        }
        else if(initMapperAsyncTask.getStatus() == AsyncTask.Status.PENDING){
            initMapperAsyncTask.cancel(true);
            initMapperAsyncTask = new AWSInitMapperAsyncTask(this);
            initMapperAsyncTask.execute(awsDatabaseHelper);
        }
        return false;
    }


    @Override
    public void processFinish(DynamoDBMapper output) {
        this.mapper = output;
    }
}
