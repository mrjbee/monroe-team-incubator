package org.monroe.team.smooker.app.uc.underreview;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.EventMessenger;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.common.SmookerModel;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.db.Dao;

public class AddSmoke extends TransactionUserCase<Void,GetStatisticState.StatisticState, Dao> {

    public AddSmoke(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected GetStatisticState.StatisticState transactionalExecute(Void request, Dao dao) {
        dao.addSmoke();
        GetStatisticState.StatisticState statisticState = using(SmookerModel.class).execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(
                        GetStatisticState.StatisticName.SMOKE_TODAY));
        using(EventMessenger.class).send(Events.SMOKE_COUNT_CHANGED, statisticState.getTodaySmokeDates().size());
        return statisticState;
    }
}