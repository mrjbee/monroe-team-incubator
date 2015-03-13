package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.EventMessenger;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.db.Dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CalculateTodaySmokeSchedule extends TransactionUserCase<Void, List<CalculateTodaySmokeSchedule.SmokeSuggestion>, Dao> {


    public CalculateTodaySmokeSchedule(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }


    //Define start from date
    //Define how much left
    //using limit of 30 min

    @Override
    protected List<SmokeSuggestion> transactionalExecute(Void request, Dao dao) {
        List<SmokeSuggestion> answer = getSmokeSuggestionsImpl(dao);
        using(EventMessenger.class).send(Events.SMOKE_SCHEDULE_CHANGED,new ArrayList<SmokeSuggestion>(answer));
        return answer;
    }

    private List<SmokeSuggestion> getSmokeSuggestionsImpl(Dao dao) {
        Date now = DateUtils.now();
        Date today = DateUtils.dateOnly(now);
        QuitSmokeProgram smokeProgram = using(QuitSmokeProgramManager.class).get();
        List<DAOSupport.Result> smokesTodayList = dao.getSmokesForPeriod(today,DateUtils.mathDays(today,1));
        if (smokeProgram == null || smokesTodayList.isEmpty()){
            //Not enough data
            return Collections.EMPTY_LIST;
        }

        int todaySmokeLimit = smokeProgram.getTodaySmokeCount();
        if (todaySmokeLimit <= smokesTodayList.size()){
            //over or same as limit
            return Collections.EMPTY_LIST;
        }


        Date startFromDate = smokesTodayList.get(smokesTodayList.size()-1).get(1,Date.class);
        List<DAOSupport.Result> cancellationsTodayList = dao.getSmokesCancelForPeriod(today,DateUtils.mathDays(today,1));
        int smokeSkippedToday = 0;
        if (!cancellationsTodayList.isEmpty()){
            startFromDate = new Date(Math.max(startFromDate.getTime(),
                    cancellationsTodayList.get(cancellationsTodayList.size() - 1).get(1, Long.class)));
            for (DAOSupport.Result result : cancellationsTodayList) {
                if (SmokeCancelReason.SKIP == SmokeCancelReason.byId(result.get(2,Integer.class))){
                    smokeSkippedToday++;
                }
            }
        }

        int smokesToScheduleCount = todaySmokeLimit - smokeSkippedToday - smokesTodayList.size();

        if (smokesToScheduleCount < 0){
            return Collections.EMPTY_LIST;
        }

        return recalculateSmokingSchedule(startFromDate,DateUtils.mathDays(today,1),smokesToScheduleCount);
    }

    public List<SmokeSuggestion> recalculateSmokingSchedule(Date scheduleStart, Date scheduleStop, int leftSmokes) {
        long period = scheduleStop.getTime() - scheduleStart.getTime();
        if (period < 30 || leftSmokes <= 0) return Collections.EMPTY_LIST;
        period = period/60000;
        int deltaMinutes = (int) period / leftSmokes;
        if (deltaMinutes < 30){
            deltaMinutes = 30;
            leftSmokes = (int) (period/deltaMinutes);
        }
        List<SmokeSuggestion> answer = new ArrayList<SmokeSuggestion>();
        for (int i = 1; i < leftSmokes + 1; i++){
            answer.add(new SmokeSuggestion(DateUtils.mathMinutes(scheduleStart, deltaMinutes * i)));
        }
        return answer;
    }

    public static class SmokeSuggestion implements Serializable{
        public final Date date;
        public SmokeSuggestion(Date date) {
            this.date = date;
        }
    }

}
