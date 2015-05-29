package org.monroe.team.smooker.promo.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.promo.common.constant.Settings;
import org.monroe.team.smooker.promo.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.promo.db.Dao;

import java.util.Date;

public class RemoveData extends TransactionUserCase<Boolean, Void, Dao>{

    public RemoveData(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Boolean todayOnly, Dao dao) {
        Date date = DateUtils.today();
        if (!todayOnly) {
            date = new Date(0);
        }

        dao.removeSmokesAfter(date);
        dao.removeSmokesCancellationAfter(date);

        if (!todayOnly){
            //clear data
            using(SettingManager.class).set(Settings.APP_FIRST_TIME_DATE, DateUtils.today().getTime());
            //quit smoke program
            using(QuitSmokeProgramManager.class).disable();
        }
        return null;
    }
}
