package com.fivesigmagames.sdghunter.repository.aws;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.fivesigmagames.sdghunter.R;
import com.fivesigmagames.sdghunter.model.ShareItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppanero on 17/10/2016.
 */

public class AWSShareItemRepository implements AWSInitMapperAsyncTask.InitMapperAsyncResponse,
        AWSQueryAsyncTask.QueryAsyncResponse {

    // CONSTANTS
    private static final String MY_BUCKET_NAME = "sdg-hunter";
    private static final String TAG = "SDG [AWS Repository]";

    // VARS
    private AWSDatabaseHelper awsDatabaseHelper;
    private DynamoDBMapper mapper;
    private AWSInitMapperAsyncTask initMapperAsyncTask;
    private Context context;
    private AWSQueryAsyncTask.QueryAsyncResponse mListener;

    public static String getMyBucketName() {
        return MY_BUCKET_NAME;
    }

    public AWSShareItemRepository(Context context, AWSQueryAsyncTask.QueryAsyncResponse listener){
        this.context = context;
        this.mListener = listener;
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
                AWSUploadTaskParams params = new AWSUploadTaskParams(item, mapper);
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

    //Query with a 0.03 difference which envelopes the points that fit in the
    // square that could show the screen
    public void findSDGImages(Location location){
        if(location != null) {
            if(checkMapper()) {
                String folder = context.getResources().getString(R.string.sdg_pictures_path)
                        .concat(File.separator)
                        .concat(
                            context.getResources().getString(R.string.sdg_download_pictures_path)
                        );
                AWSQueryTaskParams params = new AWSQueryTaskParams(location, mapper);
                AWSQueryAsyncTask awsUploadAsyncTask = new AWSQueryAsyncTask(
                        this,
                        folder
                );
                awsUploadAsyncTask.execute(params);
                Log.d(TAG, "AWS query thread started...");
            }
            else{
                Log.d(TAG, "Error mapper is null, not initialized properly");
            }
        }
        else{
            Log.d(TAG, "location is null, not querying for nearby points");
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

    @Override
    public void queryProcessFinish(ArrayList<ShareItem> output) {
        mListener.queryProcessFinish(output);
    }
}
