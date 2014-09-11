package org.monroe.team.smooker.app.dp;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAO {

    private final SQLiteDatabase db;


    public DAO(SQLiteDatabase db) {
        this.db = db;
    }

    public Result getLastPrice() {
        Cursor cursor = db.query(
                DB.SmokePriceEntry.TABLE_NAME,
                column(DB.SmokePriceEntry._ID, DB.SmokePriceEntry._SINCE_DATE, DB.SmokePriceEntry._PRICE),
                null,
                null,
                null,
                null,
                DB.SmokePriceEntry._SINCE_DATE+" DESC",
                "1"
        );
        if (!cursor.moveToFirst()) return null;
        return Result.answer().with(cursor.getLong(0),cursor.getLong(1),cursor.getFloat(2));
    }

    private String[] column(String ... columnNames) {
        return columnNames;
    }

    public long savePrice(float costPerSmoke, Date date) {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        // Insert the new row, returning the primary key value of the new row
        long id = db.insertWithOnConflict(
                DB.SmokePriceEntry.TABLE_NAME,
                null,
                DB.SmokePriceEntry.asRow(costPerSmoke,date),
                SQLiteDatabase.CONFLICT_REPLACE);
        if (id == -1) throw new RuntimeException("Couldn`t insert.");
        return id;
    }


    public static class Result {

        private List<Object> fetchedFiledList = new ArrayList<Object>(4);

        static Result answer(){
            return new Result();
        }

        Result with(Object ... withValues) {
            for (Object withValue : withValues) {
                fetchedFiledList.add(withValue);
            }

            return this;
        }

        @Override
        public String toString() {
            return "Result {" + fetchedFiledList + '}';
        }


        public <Type> Type get(int index, Class<Type> asClass) {
            return (Type) fetchedFiledList.get(index);
        }
    }


}
