package com.fivesigmagames.sdghunter.repository.aws;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.fivesigmagames.sdghunter.model.ShareItem;

import java.io.File;

/**
 * Created by ppanero on 18/10/2016.
 */

public class AWSUploadAsyncTask extends AsyncTask<AWSUploadTaskParams, Void, Void> {

    // CONSTANTS
    private static final String TAG = "SDG [AWS Upload]";


    @Override
    protected Void doInBackground(AWSUploadTaskParams... awsParams) {
        int count = awsParams.length;
        if(count == 1){
            Log.d(TAG, "Uploading ShareItem...");
            AWSUploadTaskParams params = awsParams[0];
            DynamoDBMapper mapper = params.getMapper();
            ShareItem item = params.getItem();

            // AWS
            AWSShareItem awsItem = new AWSShareItem();
            awsItem.setLatitude(item.getLatitude());
            awsItem.setLongitude(item.getLongitude());
            awsItem.setFilename(item.getTitle());
            // S3 pic
            awsItem.setPhoto(mapper.createS3Link(AWSShareItemRepository.getMyBucketName(), item.getTitle()));
            awsItem.getPhoto().uploadFrom(new File(item.getFullPath()));
            // Save
            mapper.save(awsItem);
            Log.d(TAG, "AWS Item saved in DynamoDB");

        }
        else {
            Log.d(TAG, "Error (Duplicated), more than one item to publish at the same time");
        }
        return null;
    }
}

