package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.dp.DAO;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

public class AddSmoke extends TransactionUserCase<Void,GetStatisticState.StatisticState> {

    public AddSmoke(Registry registry) {
        super(registry);
    }

    @Override
    protected GetStatisticState.StatisticState transactionalExecute(Void request, DAO dao) {
        dao.addOneSmoke();
        return usingModel().execute(GetStatisticState.class,null);
    }
}
