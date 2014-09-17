package org.monroe.team.smooker.app.common;

import android.content.SharedPreferences;

import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;

@Deprecated
public class Preferences {

    private final SharedPreferences preferences;

    public Preferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Deprecated
    public <Result> Result usingDB (DAO dao,DBAction<Result> action){
        return action.execute(this,new DB(dao));
    }

    @Deprecated
    public static interface DBAction<ResultType>{
        public ResultType execute(Preferences preferences, DB dbPreferences);
    }

    public DB db(DAO dao){
        return new DB(dao);
    }

    public static class DB {

        private final DAO dao;

        public DB(DAO dao) {
            this.dao = dao;
        }

        public void setCostPerSmoke(float costPerSmoke) {
            dao.savePrice(costPerSmoke, DateUtils.dateOnly(DateUtils.now()));
        }

        public float getCostPerSmoke() {
            DAO.Result result = dao.getLastPrice();
            if (result == null) return 1.75f;
            return result.get(2, Float.class);
        }

        public boolean hasFinancialHistory() {
            //TODO: implement financial history lookup
            return false;
        }
    }

}
