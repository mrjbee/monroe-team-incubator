package org.monroe.team.smooker.app.common;

import android.content.SharedPreferences;

import org.monroe.team.smooker.app.dp.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;

import java.util.Date;

public class Preferences {

    private final SharedPreferences preferences;

    public Preferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    //==========Shared Preferences

    public boolean isFirstStart() {
        return preferences.getBoolean("FIRST_START", true);
    }

    public void markAsFirstStartDone(){
        preferences.edit().putBoolean("FIRST_START",false).apply();
    }


    public Currency getCurrency() {
        return Currency.byId(preferences.getInt("CURRENCY_ID", Currency.RUB.id));
    }

    public void setCurrency(Currency currency) {
        preferences.edit().putInt("CURRENCY_ID", currency.id).apply();
    }

    public int getSmokePerDay(int defaultValue) {
        return preferences.getInt("SMOKE_PER_DAY", defaultValue);
    }

    public void setSmokePerDay(int smokePerDay) {
        preferences.edit().putInt("SMOKE_PER_DAY", smokePerDay).apply();
    }

    public int getDesireSmokePerDay() {
        return preferences.getInt("TARGET_SMOKE_PER_DAY", 0);
    }

    public void setDesireSmokePerDay(int desireSmokePerDay) {
        preferences.edit().putInt("TARGET_SMOKE_PER_DAY", desireSmokePerDay).apply();
    }

    public SmokeQuitProgramDifficult getQuitProgram() {
        return SmokeQuitProgramDifficult.levelByIndex(preferences.getInt("QUIT_PROGRAM",
                SmokeQuitProgramDifficult.DISABLED.toIndex()));
    }

    public void setQuiteProgram(SmokeQuitProgramDifficult quiteProgramLevel) {
        preferences.edit().putInt("QUIT_PROGRAM", quiteProgramLevel.toIndex()).apply();
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
            dao.savePrice(costPerSmoke, DateUtils.dateOnly(DateUtils.getNow()));
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
