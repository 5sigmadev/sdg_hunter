package com.fivesigmagames.sdghunter.repository.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fivesigmagames.sdghunter.model.ShareItem;
import com.fivesigmagames.sdghunter.repository.sqlite.SqliteShareItemContract.ShareItemEntry;
/**
 * Created by ppanero on 06/10/16.
 */

public class SqliteShareItemRepository {

    //CONSTANTS
    private static final String TAG = "SDG [ShareItemRepos]";
    private static final String UNIQUE_CONSTRAINT = " UNIQUE";
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    public static final String CREATE_TABLE_SHARE_ITEM =
            "CREATE TABLE " + ShareItemEntry.TABLE_NAME + " (" +
                    ShareItemEntry._ID + " INTEGER PRIMARY KEY," +
                    ShareItemEntry.COLUMN_NAME_TITLE + TEXT_TYPE + UNIQUE_CONSTRAINT + COMMA_SEP +
                    ShareItemEntry.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP +
                    ShareItemEntry.COLUMN_NAME_LONGITUDE + REAL_TYPE + " )";

    public static final String DROP_TABLES_SHARE_ITEM =
            "DROP TABLE IF EXISTS " + ShareItemEntry.TABLE_NAME;

    // VARS
    SqliteDatabaseHelper mDbHelper;

    public SqliteShareItemRepository(Context context){
        mDbHelper = new SqliteDatabaseHelper(context);
    }

    public void insert(ShareItem item){
        // Insert the new row, returning the primary key value of the new row
        if(findByName(item.getTitle()) == null) {
            // Gets the data repository in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(ShareItemEntry.COLUMN_NAME_TITLE, item.getTitle());
            values.put(ShareItemEntry.COLUMN_NAME_LATITUDE, item.getLatitude());
            values.put(ShareItemEntry.COLUMN_NAME_LONGITUDE, item.getLongitude());

            long newItemId = db.insert(ShareItemEntry.TABLE_NAME, null, values);
            db.close();
        }
    }

    public ShareItem findByName(String name){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ShareItemEntry._ID,
                ShareItemEntry.COLUMN_NAME_TITLE,
                ShareItemEntry.COLUMN_NAME_LATITUDE,
                ShareItemEntry.COLUMN_NAME_LONGITUDE
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = ShareItemEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { name };

        Cursor queryCursor = db.query(
                ShareItemEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        int resultCount = queryCursor.getCount();
        if(resultCount == 1) {
            queryCursor.moveToFirst();
            long _id = queryCursor.getLong(queryCursor.getColumnIndexOrThrow(ShareItemEntry._ID));
            String title = queryCursor.getString(
                    queryCursor.getColumnIndexOrThrow(ShareItemEntry.COLUMN_NAME_TITLE)
            );
            double latitude = queryCursor.getDouble(
                    queryCursor.getColumnIndexOrThrow(ShareItemEntry.COLUMN_NAME_LATITUDE)
            );
            double longitude = queryCursor.getDouble(
                    queryCursor.getColumnIndexOrThrow(ShareItemEntry.COLUMN_NAME_LONGITUDE)
            );
            queryCursor.close();
            db.close();
            return new ShareItem(_id, title, null, latitude, longitude);
        }
        else if(resultCount > 1){
            Log.e(TAG, "Error querying, more than one result matching title ");
        }
        else {
            Log.d(TAG, "Photo title not found in db");
        }
        queryCursor.close();
        db.close();
        return null;
    }

}
