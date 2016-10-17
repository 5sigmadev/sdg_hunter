package com.fivesigmagames.sdghunter.repository.sqlite;

import android.provider.BaseColumns;

/**
 * Created by ppanero on 06/10/16.
 */

public class SqliteShareItemContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SqliteShareItemContract() {}

    /* Inner class that defines the table contents */
    public static class ShareItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "share_item";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }

}
