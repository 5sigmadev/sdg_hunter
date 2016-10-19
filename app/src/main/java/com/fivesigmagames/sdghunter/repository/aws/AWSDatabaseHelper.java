package com.fivesigmagames.sdghunter.repository.aws;


import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.fivesigmagames.sdghunter.R;

/**
 * Created by ppanero on 17/10/2016.
 */

public class AWSDatabaseHelper {

    // CONSTANTS

    private static final String TAG = "SDG [AWS DB Helper]";

    // VARS
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient ddbClient;

    public AmazonDynamoDBClient getDdbClient() {
        return ddbClient;
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public void buildCredentialsProvider(Context context) {
        if (credentialsProvider == null){
            // Initialize the Amazon Cognito credentials provider
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    context.getResources().getString(R.string.account_id), //Account user ID
                    context.getResources().getString(R.string.identity_pool_id), // your identity pool id
                    context.getResources().getString(R.string.unauth_role), // an unauthenticated role ARN
                    context.getResources().getString(R.string.auth_role),// an authenticated role ARN
                    Regions.US_WEST_2 // Region
            );
        }
        else {
            Log.d(TAG, "CredentialsProvider already built");
        }
    }

    public boolean buildDynamoDBClient(){
        if(ddbClient == null) {
            if (credentialsProvider != null) {
                ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                ddbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
                Log.d(TAG, "DynamoDBClient built");
                return true;
            }
            Log.d(TAG, "ERROR could not built DynamoDBClient");
            return false;
        }
        else{
            Log.d(TAG, "DynamoDBClient already built");
            return true;
        }
    }
}
