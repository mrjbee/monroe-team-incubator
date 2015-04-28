package org.monroe.team.smooker.app.db;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.Schema;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Dao extends DAOSupport {

    public Dao(SQLiteDatabase db, Schema schema) {
        super(db, schema);
    }

    public long addSmoke() {
        long id = db.insertOrThrow(
                smokeTable().TABLE_NAME,
                null,
                content()
                        .value(smokeTable()._DATE,DateUtils.now().getTime())
                        .get());
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
                whereStatement = "? <= " + smokeTable()._DATE.name() + " AND ? > " + smokeTable()._DATE.name();
                whereArgs = strs(Long.toString(startDate.getTime()), Long.toString(endDate.getTime()));
            } else if (endDate != null){
                whereStatement = "? > " + smokeTable()._DATE.name();
                whereArgs = strs(Long.toString(endDate.getTime()));
            } else {
                whereStatement = "? <= " + smokeTable()._DATE.name();
                whereArgs = strs(Long.toString(startDate.getTime()));
            }
        }

        Cursor cursor = db.query(smokeTable().TABLE_NAME,
                strs(smokeTable()._ID.name(),smokeTable()._DATE.name()),
                whereStatement,
                whereArgs,
                null,
                null,
                null);
        return collect(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return result().with(arg.getLong(0),arg.getLong(1));
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
                whereStatement = "? <= " + smokeTable()._DATE.name() + " AND ? > " + smokeTable()._DATE.name();
                whereArgs = strs(Long.toString(startDate.getTime()), Long.toString(endDate.getTime()));
            } else if (endDate != null){
                whereStatement = "? > " + smokeTable()._DATE.name();
                whereArgs = strs(Long.toString(endDate.getTime()));
            } else {
                whereStatement = "? <= " + smokeTable()._DATE.name();
                whereArgs = strs(Long.toString(startDate.getTime()));
            }
        }

        Cursor cursor = db.query(smokeCancelTable().TABLE_NAME,
                strs(smokeCancelTable()._ID.name(),
                        smokeCancelTable()._DATE.name(),
                        smokeCancelTable()._REASON.name()),
                whereStatement,
                whereArgs,
                null,
                null,
                null);
        return collect(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return result().with(
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

        Cursor cursor = db.query(smokeTable().TABLE_NAME,
                strs("strftime('%Y-%m-%d', "+smokeTable()._DATE.name()+" / 1000, 'unixepoch','localtime'), count(*)"),
                null,
                null,
                "strftime('%Y-%m-%d', "+smokeTable()._DATE.name()+" / 1000, 'unixepoch','localtime')",
                null,
                null);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return collect(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                try {
                    return result().with(dateFormat.parse(arg.getString(0)).getTime(),arg.getLong(1));
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
        int i = db.delete(smokeTable().TABLE_NAME,
                "? == "+smokeTable()._ID.name(),
                strs(id));
        if (i != 1){
            throw new IllegalStateException("Supposed to remove single instance but was "+i);
        }
    }

    public void removeSmokeCancellationById(long id) {
        int i = db.delete(smokeCancelTable().TABLE_NAME,
                "? == "+smokeCancelTable()._ID.name(),
                strs(id));
        if (i != 1){
            throw new IllegalStateException("Supposed to remove single instance but was "+i);
        }
    }

    public Result getLastLoggedSmoke() {
        Cursor cursor = db.query(smokeTable().TABLE_NAME,
                strs(smokeTable()._ID.name(), smokeTable()._DATE.name()),
                null,
                null,
                null,
                null,
                smokeTable()._DATE.name() + " DESC",
                "1");
        if (!cursor.moveToFirst()){
            return null;
        }

        return new Result().with(cursor.getLong(0),cursor.getLong(1));

    }

    public Result getLastLoggedSmokeCancellation() {
        Cursor cursor = db.query(smokeCancelTable().TABLE_NAME,
                strs(
                        smokeCancelTable()._ID.name(),
                        smokeCancelTable()._DATE.name(),
                        smokeCancelTable()._REASON.name()),
                null,
                null,
                null,
                null,
                smokeCancelTable()._DATE.name() + " DESC",
                "1");

        if (!cursor.moveToFirst()){
            return null;
        }

        return new Result().with(cursor.getLong(0),cursor.getLong(1),cursor.getInt(2));
    }

    public void removeSmokesAfter(Date now) {
        db.delete(smokeTable().TABLE_NAME,
                "? < " + smokeTable()._DATE.name(),
                strs(Long.toString(now.getTime()))
        );
    }


    public void removeSmokesCancellationAfter(Date now) {
        db.delete(smokeCancelTable().TABLE_NAME,
                "? < " + smokeTable()._DATE.name(),
                strs(Long.toString(now.getTime()))
        );
    }

    public long addSmokeCancellation(SmokeCancelReason reason) {
        long id = db.insertOrThrow(
                smokeCancelTable().TABLE_NAME,
                null,
                content()
                        .value(smokeCancelTable()._DATE, DateUtils.now().getTime())
                        .value(smokeCancelTable()._REASON, reason.id)
                        .get());
        return id;
    }


    private SmookerSchema.SmokeEntry smokeTable() {
        return table(SmookerSchema.SmokeEntry.class);
    }

    private SmookerSchema.SmokeCancelEntry smokeCancelTable() {
        return table(SmookerSchema.SmokeCancelEntry.class);
    }


}
