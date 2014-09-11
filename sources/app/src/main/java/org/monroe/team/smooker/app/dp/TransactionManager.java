package org.monroe.team.smooker.app.dp;

import android.database.sqlite.SQLiteDatabase;

public class TransactionManager {

    private final DBHelper helper;
    private SQLiteDatabase dbInstance;

    public TransactionManager(DBHelper helper) {
        this.helper = helper;
    }

    public <ResultType> ResultType execute(TransactionAction<ResultType> action){
        prepareResources();
        return executeAction(action, dbInstance);
    }

    public synchronized void prepareResources() {
        if (dbInstance == null){
            dbInstance = helper.getWritableDatabase();
        }
    }

    public synchronized void releaseResources() {
        if (dbInstance != null){
            dbInstance.close();
            dbInstance = null;
        }
    }


    private <ResultValue> ResultValue executeAction(TransactionAction<ResultValue> action, SQLiteDatabase database) {
        DAO dao = new DAO(database);
        database.beginTransaction();
        ResultValue resultValue;
        try {
            resultValue = action.execute(dao);
            database.setTransactionSuccessful();
            return resultValue;
        }
        catch(RuntimeException e){
            throw e;
        }finally {
            database.endTransaction();
        }
    }

    public static interface TransactionAction <ResultValue> {
        public ResultValue execute(DAO dao);
    }
}
