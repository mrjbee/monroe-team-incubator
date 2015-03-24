package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.uc.common.FetchFailedException;

import java.io.Serializable;

public class PrepareTodaySmokeDetails extends UserCaseSupport<Void, PrepareTodaySmokeDetails.TodaySmokeDetails> {

    public PrepareTodaySmokeDetails(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    public TodaySmokeDetails executeImpl(Void request) {
        GetSmokeStatistic.SmokeStatistic smokeStatistic = null;
        try {
           smokeStatistic = using(DataManger.class).fetch(GetSmokeStatistic.SmokeStatistic.class);
        } catch (DataProvider.FetchException e) {
           throw new FetchFailedException("GTSD1",e);
        }
        GetSmokeQuitDetails.Details smokeQuitDetails = null;
        try {
            smokeQuitDetails = using(DataManger.class).fetch(GetSmokeQuitDetails.Details.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("GTSD2",e);
        }

        if (smokeQuitDetails.level == QuitSmokeDifficultLevel.DISABLED){
            return new TodaySmokeDetails(smokeStatistic.getTodaySmokeCount(),
                    smokeStatistic.getTotalSmokeCount(),
                    smokeStatistic.getAverageSmokeCount(), TodaySmokeDetails.SpecialType.NO_LIMIT, smokeStatistic.getTodaySmokeCount());
        } else {
            int delta = smokeQuitDetails.limit - smokeStatistic.getTodaySmokeCount();
            TodaySmokeDetails.SpecialType type = TodaySmokeDetails.SpecialType.BEFORE_LIMIT;
            if (delta < 0 ){
                type = TodaySmokeDetails.SpecialType.AFTER_LIMIT;
            } if (delta == 0){
                type = TodaySmokeDetails.SpecialType.NO_LEFT;
            }
            return new TodaySmokeDetails(Math.abs(delta), smokeStatistic.getTotalSmokeCount(),
                    smokeStatistic.getAverageSmokeCount(), type, smokeStatistic.getTodaySmokeCount());
        }
    }

    public static class TodaySmokeDetails implements Serializable {

        public final int specialCount;
        public final int total;
        public final int avarage;
        public final SpecialType type;
        public final int todaySmokes;

        public TodaySmokeDetails(int specialCount, int total, int avarage, SpecialType type, int todaySmokes) {
            this.specialCount = specialCount;
            this.total = total;
            this.avarage = avarage;
            this.type = type;
            this.todaySmokes = todaySmokes;
        }

        public static enum SpecialType{
            NO_LIMIT, BEFORE_LIMIT, AFTER_LIMIT, NO_LEFT
        }
    }
}
