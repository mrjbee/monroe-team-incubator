package org.monroe.team.smooker.app.db;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.monroe.team.android.box.Closure;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.uc.common.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAO {

    private final SQLiteDatabase db;

    public DAO(SQLiteDatabase db) {
        this.db = db;
    }

    private String[] strs(Object... vals) {
        String[] strings = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            strings[i]=String.valueOf(vals[i]);
        }
        return strings;
    }

    public long addSmoke() {
        long id = db.insertOrThrow(
                DB.SmokeEntry.TABLE_NAME,
                null,
                DB.SmokeEntry.asRow(DateUtils.now()));
        return id;
    }

    /**
     * @param startDate
     * @param endDate
     * @return Result [ID:long,DATE:long]
     */
    public List<Result> getSmokesForPeriod(Date startDate, Date endDate) {
        String whereStatement = null;
        String[] whereArgs = null;

        if (startDate != null || endDate != null){
            if (startDate != null && endDate != null){
                whereStatement = "? <= " + DB.SmokeEntry._DATE + " AND ? > " + DB.SmokeEntry._DATE;
                whereArgs = strs(Long.toString(startDate.getTime()), Long.toString(endDate.getTime()));
            } else if (endDate != null){
                whereStatement = "? > " + DB.SmokeEntry._DATE;
                whereArgs = strs(Long.toString(endDate.getTime()));
            } else {
                whereStatement = "? <= " + DB.SmokeEntry._DATE;
                whereArgs = strs(Long.toString(startDate.getTime()));
            }
        }

        Cursor cursor = db.query(DB.SmokeEntry.TABLE_NAME,
                strs(DB.SmokeEntry._ID, DB.SmokeEntry._DATE),
                whereStatement,
                whereArgs,
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


    /**
     * @param startDate
     * @param endDate
     * @return Result [ID:long,DATE:long,REASON:int]
     */
    public List<Result> getSmokesCancelForPeriod(Date startDate, Date endDate) {
        String whereStatement = null;
        String[] whereArgs = null;

        if (startDate != null || endDate != null){
            if (startDate != null && endDate != null){
                whereStatement = "? <= " + DB.SmokeEntry._DATE + " AND ? > " + DB.SmokeEntry._DATE;
                whereArgs = strs(Long.toString(startDate.getTime()), Long.toString(endDate.getTime()));
            } else if (endDate != null){
                whereStatement = "? > " + DB.SmokeEntry._DATE;
                whereArgs = strs(Long.toString(endDate.getTime()));
            } else {
                whereStatement = "? <= " + DB.SmokeEntry._DATE;
                whereArgs = strs(Long.toString(startDate.getTime()));
            }
        }

        Cursor cursor = db.query(DB.SmokeCancelEntry.TABLE_NAME,
                strs(DB.SmokeCancelEntry._ID,
                     DB.SmokeCancelEntry._DATE,
                     DB.SmokeCancelEntry._REASON),
                whereStatement,
                whereArgs,
                null,
                null,
                null);
        return collect(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return Result.answer().with(
                        arg.getLong(0),
                        arg.getLong(1),
                        arg.getInt(2));
            }
        });
    }

    /**
     * @return
     * 0:Date,
     * 1:Count(LONG)
     */

    public List<Result> groupSmokesPerDay() {

        //SELECT strftime('%Y-%m-%d', date / 1000, 'unixepoch'), count(*) FROM smoke GROUP BY strftime('%Y-%m-%d', date / 1000, 'unixepoch');

        Cursor cursor = db.query(DB.SmokeEntry.TABLE_NAME,
                strs("strftime('%Y-%m-%d', "+DB.SmokeEntry._DATE+" / 1000, 'unixepoch','localtime'), count(*)"),
                null,
                null,
                "strftime('%Y-%m-%d', "+DB.SmokeEntry._DATE+" / 1000, 'unixepoch','localtime')",
                null,
                null);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return collect(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                try {
                    return Result.answer().with(dateFormat.parse(arg.getString(0)).getTime(),arg.getLong(1));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
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
        cursor.close();
        return answer;
    }

    public List<Result> getSmokesAllPeriod() {
        return getSmokesForPeriod(null, null);
    }

    public void removeSmokeById(Long id) {
        int i = db.delete(DB.SmokeEntry.TABLE_NAME,
                "? == "+DB.SmokeEntry._ID,
                strs(id));
        if (i != 1){
            throw new IllegalStateException("Supposed to remove single instance but was "+i);
        }
    }

    public Result getLastLoggedSmoke() {
        Cursor cursor = db.query(DB.SmokeEntry.TABLE_NAME,
                strs(DB.SmokeEntry._ID, DB.SmokeEntry._DATE),
                null,
                null,
                null,
                null,
                DB.SmokeEntry._DATE + " DESC",
                "1");
        if (!cursor.moveToFirst()){
            return null;
        }

        return new Result().with(cursor.getLong(0),cursor.getLong(1));

    }

    public void removeSmokesAfter(Date now) {
        db.delete(DB.SmokeEntry.TABLE_NAME,
                "? < " + DB.SmokeEntry._DATE,
                strs(Long.toString(now.getTime()))
        );
    }


    public void removeSmokesCancellationAfter(Date now) {
        db.delete(DB.SmokeCancelEntry.TABLE_NAME,
                "? < " + DB.SmokeEntry._DATE,
                strs(Long.toString(now.getTime()))
        );
    }

    public long addSmokeCancellation(SmokeCancelReason reason) {
        long id = db.insertOrThrow(
                DB.SmokeCancelEntry.TABLE_NAME,
                null,
                DB.SmokeCancelEntry.asRow(DateUtils.now(),reason));
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
            if (asClass.equals(Date.class)){
                return (Type) new Date(get(index,Long.class));
            }
            return (Type) fetchedFiledList.get(index);
        }
    }


}
