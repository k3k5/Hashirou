package de.uni_regensburg.mi.hashirou.database;

import android.provider.BaseColumns;

/**
 * Created by k3k5 on 28.07.17.
 */

public class DatabaseContract {

    private DatabaseContract() {}

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "run";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_LOCATION_LAT = "location_lat";
        public static final String COLUMN_NAME_LOCATION_LNG = "location_lng";
        public static final String COLUMN_NAME_CURRENT_SPEED = "speed";
        public static final String COLUMN_NAME_CURRENT_HEIGHT = "height";
    }
}
