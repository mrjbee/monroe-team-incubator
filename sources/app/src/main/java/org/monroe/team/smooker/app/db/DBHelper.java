package org.monroe.team.smooker.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, DB.DATABASE_NAME, null, DB.DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        for (String sqlCreateEntry : DB.SQL_CREATE_ENTRIES_INITIAL) {
            db.execSQL(sqlCreateEntry);
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
