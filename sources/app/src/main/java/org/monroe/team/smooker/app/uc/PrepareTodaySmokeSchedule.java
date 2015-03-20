package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.smooker.app.uc.common.FetchFailedException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class PrepareTodaySmokeSchedule extends UserCaseSupport<Void, PrepareTodaySmokeSchedule.TodaySmokeSchedule> {

    public PrepareTodaySmokeSchedule(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    public TodaySmokeSchedule executeImpl(Void request) {

        GetSmokeStatistic.SmokeStatistic todaySmokeDetails;
        GetDaySmokeSchedule.SmokeSuggestion smokeSuggestion;
        GetBasicSmokeQuitDetails.BasicSmokeQuitDetails smokeQuitDetails;

        try {
            todaySmokeDetails = using(DataManger.class).fetch(GetSmokeStatistic.SmokeStatistic.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("GTSS1",e);
        }

        try {
            smokeSuggestion = using(DataManger.class).fetch(GetDaySmokeSchedule.SmokeSuggestion.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("GTSS2",e);
        }

        try {
            smokeQuitDetails = using(DataManger.class).fetch(GetBasicSmokeQuitDetails.BasicSmokeQuitDetails.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("GTSS3",e);
        }

        return new TodaySmokeSchedule(
                todaySmokeDetails.getTodaySmokeDateList(),
                smokeSuggestion.date,
                smokeQuitDetails.limit);
    }

    public static class TodaySmokeSchedule implements Serializable {

        public final List<Date> todaySmokes;
        public final List<Date> scheduledSmokes;
        public final int limit;

        public TodaySmokeSchedule(List<Date> todaySmokes, List<Date> scheduledSmokes, int limit) {
            this.todaySmokes = todaySmokes;
            this.scheduledSmokes = scheduledSmokes;
            this.limit = limit;
        }

        public boolean isLimitSet(){
            return limit > -1;
        }
    }
}
