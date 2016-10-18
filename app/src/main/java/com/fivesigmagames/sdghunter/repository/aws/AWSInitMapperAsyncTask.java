package com.fivesigmagames.sdghunter.repository.aws;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

/**
 * Created by ppanero on 18/10/2016.
 */

public class AWSInitMapperAsyncTask extends AsyncTask<AWSDatabaseHelper, Void, DynamoDBMapper> {

    // CONSTANTS
    private static final String TAG = "SDG [AWS InitMapper]";

    // VARS
    public AsyncResponse delegate = null;

    public AWSInitMapperAsyncTask(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected DynamoDBMapper doInBackground(AWSDatabaseHelper... awsDatabaseHelpers) {
        int count = awsDatabaseHelpers.length;
        if(count == 1) {
            Log.d(TAG, "Initializing DynamoDBMapper");
            AWSDatabaseHelper helper = awsDatabaseHelpers[0];
            DynamoDBMapper mapper = new DynamoDBMapper(
                    helper.getDdbClient(),
                    helper.getCredentialsProvider()
            );
            return mapper;
        }
        else {
            Log.d(TAG, "Error (Duplicated), more than one AWS db helper to initialize the DynamoDB mapper");
        }
        return null;
    }

    @Override
    protected void onPostExecute(DynamoDBMapper result) {
        delegate.processFinish(result);
    }


    // INTERFACES

    // you may separate this or combined to caller class.
    public interface AsyncResponse {
        void processFinish(DynamoDBMapper output);
    }

}
