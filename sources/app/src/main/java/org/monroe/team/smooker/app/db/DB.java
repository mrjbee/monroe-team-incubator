package org.monroe.team.smooker.app.db;

import android.content.ContentValues;
import android.provider.BaseColumns;

import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;

import java.util.Date;

@Deprecated
public final class DB {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3 ;
    public static final String DATABASE_NAME = "Smooker.db";

    private DB(){}

    static final String[] SQL_CREATE_ENTRIES_INITIAL = {
            "CREATE TABLE IF NOT EXISTS " + SmokeEntry.TABLE_NAME + " (" +
                    SmokeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SmokeEntry._DATE + " INTEGER NOT NULL" +
            " )",
            "CREATE TABLE IF NOT EXISTS " + SmokeCancelEntry.TABLE_NAME + " (" +
                    SmokeCancelEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SmokeCancelEntry._DATE + " INTEGER NOT NULL," +
                    SmokeCancelEntry._REASON + " INTEGER NOT NULL" +
                    " )"
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

    public static abstract class SmokeCancelEntry implements BaseColumns {

        public static final String TABLE_NAME = "smoke_cancel";
        public static final String _DATE = "date";
        public static final String _REASON = "reason";

        public static ContentValues asRow(Date date, SmokeCancelReason reason) {
            ContentValues values = new ContentValues(1);
            values.put(_DATE, date.getTime());
            values.put(_REASON, reason.id);
            return values;
        }
    }
}
