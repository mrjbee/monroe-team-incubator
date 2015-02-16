package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.services.EventMessenger;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

public class AddSmoke extends TransactionUserCase<Void,GetStatisticState.StatisticState> {

    public AddSmoke(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected GetStatisticState.StatisticState transactionalExecute(Void request, DAO dao) {
        dao.addSmoke();
        GetStatisticState.StatisticState statisticState = usingModel().execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(
                        GetStatisticState.StatisticName.SMOKE_TODAY));
        using(EventMessenger.class).send(Events.SMOKE_COUNT_CHANGED, statisticState.getTodaySmokeDates().size());
        return statisticState;
    }
}
