package org.monroe.team.smooker.app.dp;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.uc.common.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
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
                strs(DB.SmokePriceEntry._ID, DB.SmokePriceEntry._SINCE_DATE, DB.SmokePriceEntry._PRICE),
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

    private String[] strs(String... columnNames) {
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

    public Result getFirstLoggedSmoke() {
        return null;
    }

    public long addOneSmoke() {
        long id = db.insertOrThrow(
                DB.SmokeEntry.TABLE_NAME,
                null,
                DB.SmokeEntry.asRow(DateUtils.now()));
        return id;
    }

    public List<Result> getSmokeForPeriod(Date startDate, Date endDate) {
        Cursor cursor = db.query(DB.SmokeEntry.TABLE_NAME,
                strs(DB.SmokeEntry._ID, DB.SmokeEntry._DATE),
                "? <= " + DB.SmokeEntry._DATE + " AND ? > " + DB.SmokeEntry._DATE,
                strs(Long.toString(startDate.getTime()), Long.toString(endDate.getTime())),
                null,
                null,
                null);
        return collect(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return Result.answer().with(arg.getLong(0),arg.getLong(1));
            }
        });
    }

    private List<Result> collect(Cursor cursor, Closure<Cursor,Result> closure) {
        List<Result> answer = new ArrayList<Result>(cursor.getCount());
        Result itResult;
        while (cursor.moveToNext()) {
            itResult = closure.execute(cursor);
            if (itResult != null) answer.add(itResult);
        }
        return answer;
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
