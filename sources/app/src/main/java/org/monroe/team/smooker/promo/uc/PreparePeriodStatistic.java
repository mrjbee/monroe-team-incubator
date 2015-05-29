package org.monroe.team.smooker.promo.uc;


import android.util.Pair;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.promo.db.Dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PreparePeriodStatistic extends TransactionUserCase<Void, PreparePeriodStatistic.PeriodStatistic, Dao>{

    public PreparePeriodStatistic(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected PeriodStatistic transactionalExecute(Void request, Dao dao) {

        List<Pair<Date, Integer>> monthSmokeList = new ArrayList<Pair<Date, Integer>>();
        List<DAOSupport.Result> loggedDates = dao.groupSmokesPerDay();
        Date today = DateUtils.dateOnly(DateUtils.now());
        Date itDate = null;
        int itSmokeCount = 0;
        boolean wasValuable = false;
        for (int i = 11; i > -1; i--){
            itDate = DateUtils.mathDays(today,-i);
            itSmokeCount = getSmokeCountForDate(itDate, loggedDates);
            if (wasValuable || itSmokeCount > 0){
                wasValuable = true;
                monthSmokeList.add(new Pair<Date, Integer>(itDate,itSmokeCount));
            }
        }
        for (int i=1; monthSmokeList.size() < 11;i++){
            itDate = DateUtils.mathDays(today,i);
            monthSmokeList.add(new Pair<Date, Integer>(itDate,0));
        }
        return new PeriodStatistic(monthSmokeList);
    }

    private int getSmokeCountForDate(Date itDate, List<DAOSupport.Result> loggedDates) {
        for (DAOSupport.Result loggedDate : loggedDates) {
            if (loggedDate.get(0,Date.class).compareTo(itDate) == 0){
                return (int)loggedDate.get(1,Long.class).longValue();
            }
        }
        return 0;
    }

    public static class PeriodStatistic implements Serializable {

        public final List<Pair<Date, Integer>> smokesPerDayList;

        public PeriodStatistic(List<Pair<Date, Integer>> smokesPerDayList) {
            this.smokesPerDayList = smokesPerDayList;
        }
    }

}
