package org.monroe.team.smooker.promo.uc;

import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.smooker.promo.common.constant.Settings;
import org.monroe.team.smooker.promo.uc.common.FetchFailedException;

import java.io.Serializable;
import java.util.Date;

public class PrepareSmokeClockDetails extends UserCaseSupport<Void, PrepareSmokeClockDetails.SmokeClockDetails> {

    public PrepareSmokeClockDetails(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    public SmokeClockDetails executeImpl(Void request) {

        GetSmokeStatistic.SmokeStatistic todaySmokeDetails;
        try {
            todaySmokeDetails = using(DataManger.class).fetch(GetSmokeStatistic.SmokeStatistic.class);
        } catch (DataProvider.FetchException e) {
            throw new FetchFailedException("PSCD1",e);
        }
        Date date = todaySmokeDetails.getLastSmokeDate();
        if (date == null){
            date = new Date(using(SettingManager.class).get(Settings.APP_FIRST_TIME_DATE));
        }
        return new SmokeClockDetails(date.getTime());
    }

    public static class SmokeClockDetails implements Serializable {

        public final long msSinceLastSmoke;

        public SmokeClockDetails(long msSinceLastSmoke) {
            this.msSinceLastSmoke = msSinceLastSmoke;
        }
    }
}
