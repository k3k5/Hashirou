package de.uni_regensburg.mi.hashirou.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

/**
 * Created by k3k5 on 28.07.17.
 */

public class DatabaseAdapter {

    private DatabaseHelper helper;
    private SQLiteDatabase db;

    public DatabaseAdapter(Context context){
        helper = new DatabaseHelper(context);
    }

    //all needed sql statements
    private static final String SQL_CREATE_ENTRIES =
                    "CREATE TABLE " + DatabaseContract.FeedEntry.TABLE_NAME + " (" +
                            DatabaseContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                            DatabaseContract.FeedEntry.COLUMN_NAME_TIMESTAMP + " TEXT," +
                            DatabaseContract.FeedEntry.COLUMN_NAME_LOCATION_LAT + " DOUBLE," +
                            DatabaseContract.FeedEntry.COLUMN_NAME_LOCATION_LNG + " DOUBLE," +
                            DatabaseContract.FeedEntry.COLUMN_NAME_CURRENT_SPEED + " DOUBLE," +
                            DatabaseContract.FeedEntry.COLUMN_NAME_CURRENT_HEIGHT + " DOUBLE)";

    private static final String SQL_DELETE_ENTRIES =
                    "DROP TABLE IF EXISTS " + DatabaseContract.FeedEntry.TABLE_NAME;

    private static final String SQL_RETURN_ALL_ENTRIES =
                    "Select * FROM " + DatabaseContract.FeedEntry.TABLE_NAME;

    public void open(){
        db = helper.getWritableDatabase();
    }

    public void close(){
        db.close();
        helper.close();
    }

    //called whenever a location is measured
    public long insertLocation(String currentTimeStamp, Location location){
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.FeedEntry.COLUMN_NAME_TIMESTAMP, currentTimeStamp);
        values.put(DatabaseContract.FeedEntry.COLUMN_NAME_LOCATION_LAT, location.getLatitude());
        values.put(DatabaseContract.FeedEntry.COLUMN_NAME_LOCATION_LNG, location.getLongitude());
        values.put(DatabaseContract.FeedEntry.COLUMN_NAME_CURRENT_SPEED, location.getSpeed());
        values.put(DatabaseContract.FeedEntry.COLUMN_NAME_CURRENT_HEIGHT, location.getAltitude());

        return db.insert(DatabaseContract.FeedEntry.TABLE_NAME, null, values);
    }

    public Cursor getAllLocations(){
        return db.rawQuery(SQL_RETURN_ALL_ENTRIES,null);
    }


    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME =  "Hashirou.db";

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
            onCreate(sqLiteDatabase);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
