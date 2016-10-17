package com.fivesigmagames.sdghunter.repository.aws;


import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by ppanero on 17/10/2016.
 */

public class AWSDatabaseHelper {

    // CONSTANTS
    private static final String IDENTITY_POOL_ID = "us-west-2:650953df-bbe0-414b-9b13-4c4f43b71598";
    private static final String TAG = "SDG [AWS DB Helper]";

    // VARS
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient ddbClient;

    public AmazonDynamoDBClient getDdbClient() {
        return ddbClient;
    }

    public void buildCredentialsProvider(Context context) {

        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                IDENTITY_POOL_ID, // Identity Pool ID
                Regions.US_WEST_2 // Region
        );
    }

    public boolean buildDynamoDBClient(){
        if(credentialsProvider != null) {
            ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            Log.d(TAG, "DynamoDBClient built");
            return true;
        }
        Log.d(TAG, "ERROR could not built DynamoDBClient");
        return false;
    }
}
