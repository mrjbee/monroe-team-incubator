package org.monroe.team.smooker.app.common;

import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences preferences;
    private float costPerSmoke = 1.75f;

    public PreferenceManager(SharedPreferences preferences) {
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

    //========Data Base

    public void setCostPerSmoke(float costPerSmoke) {
        this.costPerSmoke = costPerSmoke;
    }

    public float getCostPerSmoke() {
        return costPerSmoke;
    }

    public boolean hasFinancialHistory() {
        return false;
    }
}
