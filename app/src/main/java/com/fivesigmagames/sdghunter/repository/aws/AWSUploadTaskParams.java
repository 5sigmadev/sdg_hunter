package com.fivesigmagames.sdghunter.repository.aws;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.fivesigmagames.sdghunter.model.ShareItem;

/**
 * Created by ppanero on 18/10/2016.
 */

public class AWSUploadTaskParams {

    private ShareItem item;
    private DynamoDBMapper mapper;

    public AWSUploadTaskParams(ShareItem shareItem, DynamoDBMapper mapper) {
        this.item = shareItem;
        this.mapper = mapper;
    }

    public ShareItem getItem() {
        return item;
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }
}
