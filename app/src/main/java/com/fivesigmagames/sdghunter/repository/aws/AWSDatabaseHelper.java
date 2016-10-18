package com.fivesigmagames.sdghunter.repository.aws;


import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by ppanero on 17/10/2016.
 */

public class AWSDatabaseHelper {

    // CONSTANTS
    private static final String IDENTITY_POOL_ID = "us-west-2:650953df-bbe0-414b-9b13-4c4f43b71598";
    private static final String ACCOUNT_ID = "445409667147";
    private static final String AUTH_ROLE = "arn:aws:iam::445409667147:role/Cognito_SDGHunterAuth_Role";
    private static final String UNAUTH_ROLE = "arn:aws:iam::445409667147:role/Cognito_SDGHunterUnauth_Role";
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
                    ACCOUNT_ID, //Account user ID
                    IDENTITY_POOL_ID, // your identity pool id
                    UNAUTH_ROLE, // an unauthenticated role ARN
                    AUTH_ROLE,// an authenticated role ARN
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
