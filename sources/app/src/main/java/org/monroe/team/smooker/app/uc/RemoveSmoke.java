package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.EventMessenger;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.common.Model;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.db.Dao;

import java.util.List;

public class RemoveSmoke extends TransactionUserCase<Void,Boolean,Dao> {

    public RemoveSmoke(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Boolean transactionalExecute(Void request, Dao dao) {
        //TODO: Disallow to remove smoke if "-30" min is yesterday
        List<DAOSupport.Result> results = dao.getSmokesForPeriod(DateUtils.mathMinutes(DateUtils.now(), -30), DateUtils.now());
        if (results.isEmpty()){
            return false;
        }

        dao.removeSmokeById(results.get(results.size()-1).get(0,Long.class));
        GetStatisticState.StatisticState statisticState = using(Model.class).execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(
                        GetStatisticState.StatisticName.SMOKE_TODAY));
        using(EventMessenger.class).send(Events.SMOKE_COUNT_CHANGED, statisticState.getTodaySmokeDates().size());
        return true;
    }
}
