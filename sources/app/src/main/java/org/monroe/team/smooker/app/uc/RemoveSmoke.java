package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.common.EventMessenger;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

import java.util.List;

public class RemoveSmoke extends TransactionUserCase<Void,Boolean> {

    public RemoveSmoke(Registry registry) {
        super(registry);
    }

    @Override
    protected Boolean transactionalExecute(Void request, DAO dao) {
        //TODO: Disallow to remove smoke if "-30" min is yesterday
        List<DAO.Result> results = dao.getSmokesForPeriod(DateUtils.mathMinutes(DateUtils.now(), -30), DateUtils.now());
        if (results.isEmpty()){
            return false;
        }

        dao.removeSmokeById(results.get(results.size()-1).get(0,Long.class));
        GetStatisticState.StatisticState statisticState = usingModel().execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(
                        GetStatisticState.StatisticName.SMOKE_TODAY));
        using(EventMessenger.class).send(Events.SMOKE_COUNT_CHANGED, statisticState.getTodaySmokeDates().size());
        return true;
    }
}
