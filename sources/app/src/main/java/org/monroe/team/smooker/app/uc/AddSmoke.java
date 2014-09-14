package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.EventMessenger;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

public class AddSmoke extends TransactionUserCase<Void,GetStatisticState.StatisticState> {

    public AddSmoke(Registry registry) {
        super(registry);
    }

    @Override
    protected GetStatisticState.StatisticState transactionalExecute(Void request, DAO dao) {
        dao.addOneSmoke();
        GetStatisticState.StatisticState statisticState = usingModel().execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(
                        GetStatisticState.StatisticName.SMOKE_TODAY));
        using(EventMessenger.class).send(Events.ADD_SMOKE, statisticState.getTodaySmokeDates().size());
        return statisticState;
    }
}
