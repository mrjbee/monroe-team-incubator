package org.monroe.team.smooker.promo.uc;

import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.promo.uc.common.FetchFailedException;

import java.io.Serializable;
import java.util.Date;

public class PrepareSmokeQuitDetails extends UserCaseSupport<Void, PrepareSmokeQuitDetails.Details>{


    public PrepareSmokeQuitDetails(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Details executeImpl(Void request) {


        GetSmokeQuitSchedule.QuitSchedule quitSchedule = null;
        try {
            quitSchedule = using(DataManger.class).fetch(GetSmokeQuitSchedule.QuitSchedule.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("PSQBD1",e);
        }
        GetSmokeQuitDetails.Details smokeQuitDetails = null;
        try {
            smokeQuitDetails = using(DataManger.class).fetch(GetSmokeQuitDetails.Details.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("PSQBD2",e);
        }

        if (quitSchedule.isDisabled()){
            return new Details(null, null,0,0,-1);
        }

        int limit = smokeQuitDetails.limit;
        int progress = calculateProgress(quitSchedule);
        Date endDate = Lists.getLast(quitSchedule.scheduleDates).date;
        int dayLeftCount = (int) DateUtils.asDays(endDate.getTime() - DateUtils.today().getTime(), false);
        if (dayLeftCount < 0){
            dayLeftCount = 0;
        }

        return new Details(quitSchedule.scheduleDates.get(0).date,
                endDate,
                dayLeftCount,
                progress,
                limit);
    }

    private int calculateProgress(GetSmokeQuitSchedule.QuitSchedule quitSchedule) {
        int limitDays = 0;
        int limitDaysPassed = 0;
        for(GetSmokeQuitSchedule.QuitScheduleDate scheduleDate:quitSchedule.scheduleDates){
            if (scheduleDate.isNewLimitDate){
                limitDays++;
                if (scheduleDate.successful){
                    limitDaysPassed++;
                }
            }

        }
        return Math.round((float)limitDaysPassed/(float)limitDays * 100f);
    }

    public static class Details implements Serializable {

        public final Date startDate;
        public final Date endDate;
        public final int dayLeftCount;
        public final int progress;
        public final int todayLimit;


        public Details(Date startDate, Date endDate, int dayLeftCount, int progress, int todayLimit) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.dayLeftCount = dayLeftCount;
            this.progress = progress;
            this.todayLimit = todayLimit;
        }
    }
}
