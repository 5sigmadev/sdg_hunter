package com.fivesigmagames.sdghunter.repository.aws;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fivesigmagames.sdghunter.model.ShareItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 * Created by ppanero on 18/10/2016.
 */

public class AWSQueryAsyncTask extends AsyncTask<AWSQueryTaskParams, Void, ArrayList<ShareItem>> {

    // CONSTANTS
    private static final String TAG = "SDG [AWS Upload]";

    // VARS
    public QueryAsyncResponse delegate = null;
    private String downloadDir;

    public AWSQueryAsyncTask(QueryAsyncResponse delegate, String downloadDir){
        this.delegate = delegate;
        this.downloadDir = downloadDir;
    }

    @Override
    protected ArrayList<ShareItem> doInBackground(AWSQueryTaskParams... awsParams) {
        int count = awsParams.length;
        if(count == 1){
            Log.d(TAG, "Uploading ShareItem...");
            AWSQueryTaskParams params = awsParams[0];
            DynamoDBMapper mapper = params.getMapper();
            Location location = params.getLocation();

            Map<String, AttributeValue> conditionsMap = getStringConditionMap(location);

            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withFilterExpression(AWSShareItem.getLatAttrName() + " >= :val1 and " +
                                            AWSShareItem.getLatAttrName() + " <= :val2 and " +
                                            AWSShareItem.getLngAttrName() + " >= :val3 and " +
                                            AWSShareItem.getLngAttrName() + " <= :val4"
                    )
                    .withExpressionAttributeValues(conditionsMap);

            List<AWSShareItem> fittingShareItems = mapper.scan(AWSShareItem.class, scanExpression);

            return transformAWSShareItemtoShareItem(fittingShareItems);


        }
        else {
            Log.d(TAG, "Error (Duplicated), more than one item to publish at the same time");
        }
        return null;
    }

    private ArrayList<ShareItem> transformAWSShareItemtoShareItem(List<AWSShareItem> fittingShareItems) {
        ArrayList<ShareItem> shareItemList = new ArrayList<>();

        File dir = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), downloadDir);
        for(AWSShareItem awsItem : fittingShareItems){
            String filename = awsItem.getFilename();
            File downloadFile = new File(dir.getAbsolutePath(), filename);
            if(!downloadFile.exists()){
                awsItem.getPhoto().downloadTo(downloadFile);
                Log.d(TAG, "Downloading picture " + downloadFile.getAbsolutePath());
            }
            else{
                Log.d(TAG, "Picture already exists: " + downloadFile.getAbsolutePath());
            }
            shareItemList.add(new ShareItem(filename, downloadFile.getAbsolutePath(),
                    awsItem.getLatitude(), awsItem.getLongitude()));
        }
        return shareItemList;
    }


    @NonNull
    private Map<String, AttributeValue> getStringConditionMap(Location location) {

        Map<String, AttributeValue> conditionsMap = new HashMap<>();
        conditionsMap.put(":val1", new AttributeValue().withN(String.valueOf(location.getLatitude() - 0.03)));
        conditionsMap.put(":val2", new AttributeValue().withN(String.valueOf(location.getLatitude() + 0.03)));
        conditionsMap.put(":val3", new AttributeValue().withN(String.valueOf(location.getLongitude() - 0.03)));
        conditionsMap.put(":val4", new AttributeValue().withN(String.valueOf(location.getLongitude() + 0.03)));

        return conditionsMap;
    }

    @Override
    protected void onPostExecute(ArrayList<ShareItem> result) {
        delegate.queryProcessFinish(result);
    }


    // INTERFACES

    public interface QueryAsyncResponse {
        void queryProcessFinish(ArrayList<ShareItem> output);
    }
}