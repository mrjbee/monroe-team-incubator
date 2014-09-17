package org.monroe.team.smooker.app.db;

import android.content.ContentValues;
import android.provider.BaseColumns;

import java.util.Date;

public final class DB {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1 ;
    public static final String DATABASE_NAME = "Smooker.db";

    private DB(){}

    static final String[] SQL_CREATE_ENTRIES = {
            "CREATE TABLE " + SmokeEntry.TABLE_NAME + " (" +
                    SmokeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SmokeEntry._DATE + " INTEGER NOT NULL" +
            " )"
            };

    static final String[] SQL_DELETE_ENTRIES = {
            "DROP TABLE IF EXISTS " + SmokeEntry.TABLE_NAME
            };

    public static abstract class SmokeEntry implements BaseColumns {
        public static final String TABLE_NAME = "smoke";
        public static final String _DATE = "date";

        public static ContentValues asRow(Date date) {
            ContentValues values = new ContentValues(1);
            values.put(_DATE, date.getTime());
            return values;
        }
    }
}
