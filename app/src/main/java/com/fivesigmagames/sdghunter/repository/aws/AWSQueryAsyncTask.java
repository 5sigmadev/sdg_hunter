package com.fivesigmagames.sdghunter.repository.aws;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.fivesigmagames.sdghunter.R;
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
    public AWSInitMapperAsyncTask.InitMapperAsyncResponse delegate = null;
    private String downloadDir;

    public AWSQueryAsyncTask(AWSInitMapperAsyncTask.InitMapperAsyncResponse delegate, String downloadDir){
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

            Map<String, Condition> conditionsMap = getStringConditionMap(location);

            DynamoDBQueryExpression<AWSShareItem> queryExpression = new DynamoDBQueryExpression<AWSShareItem>()
                    .withRangeKeyConditions(conditionsMap);

            List<AWSShareItem> fittingShareItems = mapper.query(AWSShareItem.class, queryExpression);

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
            }
            shareItemList.add(new ShareItem(filename, downloadFile.getAbsolutePath(),
                    awsItem.getLatitude(), awsItem.getLongitude()));
        }
        return shareItemList;
    }


    @NonNull
    private Map<String, Condition> getStringConditionMap(Location location) {
        Condition latMin = new Condition()
                .withComparisonOperator(ComparisonOperator.GT.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(location.getLatitude() - 0.03)));
        Condition latMax = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(location.getLatitude() + 0.03)));
        Condition lngMin = new Condition()
                .withComparisonOperator(ComparisonOperator.GT.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(location.getLongitude() - 0.03)));
        Condition lngMax = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(location.getLongitude() + 0.03)));

        Map<String, Condition> conditionsMap = new HashMap<>();
        conditionsMap.put(AWSShareItem.getLatAttrName(), latMin);
        conditionsMap.put(AWSShareItem.getLatAttrName(), latMax);
        conditionsMap.put(AWSShareItem.getLngAttrName(), lngMin);
        conditionsMap.put(AWSShareItem.getLngAttrName(), lngMax);
        return conditionsMap;
    }

    // INTERFACES

    public interface QueryAsyncResponse {
        void queryProcessFinish(ArrayList<ShareItem> output);
    }
}