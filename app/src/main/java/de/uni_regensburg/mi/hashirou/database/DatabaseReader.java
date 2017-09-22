package de.uni_regensburg.mi.hashirou.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by k3k5 on 28.07.17.
 */

public class DatabaseReader extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Hashirou.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DatabaseContract.FeedEntry.TABLE_NAME + " (" +
                    DatabaseContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.FeedEntry.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    DatabaseContract.FeedEntry.COLUMN_NAME_LOCATION_LAT + " DOUBLE," +
                    DatabaseContract.FeedEntry.COLUMN_NAME_LOCATION_LNG + " DOUBLE," +
                    DatabaseContract.FeedEntry.COLUMN_NAME_CURRENT_SPEED + " DOUBLE," +
                    DatabaseContract.FeedEntry.COLUMN_NAME_CURRENT_HEIGHT + " DOUBLE)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseContract.FeedEntry.TABLE_NAME;

    public DatabaseReader(Context context) {
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
