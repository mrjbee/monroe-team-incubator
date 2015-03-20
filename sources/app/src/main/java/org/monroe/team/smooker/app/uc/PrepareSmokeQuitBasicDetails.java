package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.app.uc.common.FetchFailedException;

import java.io.Serializable;
import java.util.Date;

public class PrepareSmokeQuitBasicDetails extends UserCaseSupport<Void, PrepareSmokeQuitBasicDetails.BasicDetails>{


    public PrepareSmokeQuitBasicDetails(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected BasicDetails executeImpl(Void request) {
        GetSmokeQuitSchedule.QuitSchedule quitSchedule = null;
        try {
            quitSchedule = using(DataManger.class).fetch(GetSmokeQuitSchedule.QuitSchedule.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("PSQBD1",e);
        }
        if (quitSchedule.isDisabled()){
            return new BasicDetails(null, null);
        }

        return new BasicDetails(quitSchedule.scheduleDates.get(0).date,
                Lists.getLast(quitSchedule.scheduleDates).date);
    }

    public static class BasicDetails implements Serializable {

        public final Date startDate;
        public final Date endDate;

        public BasicDetails(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
