package org.monroe.team.smooker.promo.uc;

import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.promo.db.Dao;
import org.monroe.team.smooker.promo.uc.common.FetchFailedException;

import java.util.Date;

public class PrepareSmokeQuitDateDetails extends TransactionUserCase<Date, PrepareSmokeQuitDateDetails.DateDetails, Dao>{

    public PrepareSmokeQuitDateDetails(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected DateDetails transactionalExecute(Date request, Dao dao) {

        DateDetails answer = new DateDetails();
        Date today = DateUtils.today();
        Date requestDate = DateUtils.dateOnly(request);

        answer.isFuture = !request.before(today);
        answer.smokeCounts = dao.getSmokesForPeriod(requestDate, DateUtils.mathDays(requestDate, 1)).size();

        GetSmokeQuitSchedule.QuitSchedule schedule = null;
        try {
            schedule = using(DataManger.class).fetch(GetSmokeQuitSchedule.QuitSchedule.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("PSQDD1",e);
        }

        GetSmokeQuitSchedule.QuitScheduleDate scheduleDate = schedule.getForDate(requestDate);
        if (scheduleDate != null){
            answer.limit = scheduleDate.limit;
            answer.isPassed = scheduleDate.successful;
            answer.isLimitChanged = scheduleDate.isNewLimitDate;
        }
        return answer;
    }

    public static class DateDetails{

        private boolean isFuture = false;
        private boolean isPassed = false;
        private int smokeCounts = 0;
        private int limit = -1;
        private boolean isLimitChanged = false;

        public boolean isFuture() {
            return isFuture;
        }

        public boolean isPassed() {
            return isPassed;
        }

        public int getSmokeCounts() {
            return smokeCounts;
        }

        public int getLimit() {
            return limit;
        }

        public boolean isLimitChanged() {
            return isLimitChanged;
        }
    }
}
