package org.monroe.team.smooker.promo.common.quitsmoke;

import java.util.Date;

public class DemoQuitSmokeProgram extends QuitSmokeProgram {

    protected DemoQuitSmokeProgram(QuitSmokeDataDriver dataDriver) {
        super(dataDriver);
    }

    @Override
    protected void doInitialize(QuitSmokeData smokeData, int startSmokeCount, int endSmokeCount) {
        //do nothing here yet
    }

    @Override
    public int getTodaySmokeCount() {
        return 12;
    }

    @Override
    protected boolean doLogSmokesForDate(QuitSmokeData smokeData, Date date, int smokeCount) {
        return false;
    }


}
