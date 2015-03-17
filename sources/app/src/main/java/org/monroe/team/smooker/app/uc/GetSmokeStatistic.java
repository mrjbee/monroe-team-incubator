package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.db.Dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GetSmokeStatistic extends TransactionUserCase<Void, GetSmokeStatistic.SmokeStatistic,Dao> {


    public GetSmokeStatistic(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected SmokeStatistic transactionalExecute(Void request, Dao dao) {
        int totalSmokes =  dao.getSmokesAllPeriod().size();

        List<DAOSupport.Result> todaySmokeDaoList = dao.getSmokesForPeriod(DateUtils.dateOnly(DateUtils.now()),
                DateUtils.dateOnly(DateUtils.mathDays(DateUtils.now(), 1)));
        List<Date> todaySmokeDates = new ArrayList<Date>(todaySmokeDaoList.size());
        for (int i = 0; i < todaySmokeDaoList.size() ; i++) {
            todaySmokeDates.add(todaySmokeDaoList.get(i).get(1, Date.class));
        }
        todaySmokeDates = Collections.unmodifiableList(todaySmokeDates);

        List<DAOSupport.Result> results = dao.groupSmokesPerDay();
        int average = -1;
        if (results.size() > 2){
           results.remove(0);
           results.remove(Lists.getLastIndex(results));
           int answer = 0;
           for (int i=0; i<results.size(); i++){
                answer+= results.get(i).get(1,Long.class);
           }
           average = Math.round(answer/(results.size()));
        }

        DAOSupport.Result result = dao.getLastLoggedSmoke();
        Date lastSmokeDate = null;
        if (result != null){
            lastSmokeDate = result.get(1,Date.class);
        }
        return new SmokeStatistic(average,totalSmokes, lastSmokeDate, todaySmokeDates);
    }

    public static class SmokeStatistic implements Serializable {

        private final int averageSmokeCount;
        private final int totalSmokeCount;
        private final Date lastSmokeDate;
        private final List<Date> todaySmokeDateList;

        public SmokeStatistic(int averageSmokeCount, int totalSmokeCount, Date lastSmokeDate, List<Date> todaySmokeDateList) {
            this.averageSmokeCount = averageSmokeCount;
            this.totalSmokeCount = totalSmokeCount;
            this.lastSmokeDate = lastSmokeDate;
            this.todaySmokeDateList = todaySmokeDateList;
        }

        public boolean isAverageSmokeDefined(){
            return averageSmokeCount != -1;
        }

        public int getAverageSmokeCount() {
            return averageSmokeCount;
        }

        public int getTotalSmokeCount() {
            return totalSmokeCount;
        }

        public List<Date> getTodaySmokeDateList() {
            return todaySmokeDateList;
        }

        public int getTodaySmokeCount() {
            return todaySmokeDateList.size();
        }

        public Date getLastSmokeDate() {
            return lastSmokeDate;
        }
    }
}
