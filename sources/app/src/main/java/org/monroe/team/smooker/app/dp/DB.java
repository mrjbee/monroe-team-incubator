package org.monroe.team.smooker.app.dp;

import android.content.ContentValues;
import android.provider.BaseColumns;

import java.util.Date;

public final class DB {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "Smooker.db";

    private DB(){}

    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SmokePriceEntry.TABLE_NAME + " (" +
                    SmokePriceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SmokePriceEntry._PRICE + " REAL NOT NULL," +
                    SmokePriceEntry._SINCE_DATE + " INTEGER NOT NULL UNIQUE" +
            " )";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SmokePriceEntry.TABLE_NAME;


    public static abstract class SmokePriceEntry implements BaseColumns {
        public static final String TABLE_NAME = "smoke_price";
        public static final String _PRICE = "price";
        public static final String _SINCE_DATE = "since_date";

        public static ContentValues asRow(float costPerSmoke, Date date) {
            ContentValues values = new ContentValues(2);
            values.put(_PRICE,costPerSmoke);
            values.put(_SINCE_DATE, date.getTime());
            return values;
        }
    }


}
