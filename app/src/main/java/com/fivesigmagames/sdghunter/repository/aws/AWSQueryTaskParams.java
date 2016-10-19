package com.fivesigmagames.sdghunter.repository.aws;

import android.location.Location;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

/**
 * Created by ppanero on 18/10/2016.
 */

public class AWSQueryTaskParams {

    private Location location;
    private DynamoDBMapper mapper;

    public AWSQueryTaskParams(Location location, DynamoDBMapper mapper) {
        this.location = location;
        this.mapper = mapper;
    }

    public Location getLocation() {
        return location;
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }
}
