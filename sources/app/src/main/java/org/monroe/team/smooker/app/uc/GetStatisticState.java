package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.dp.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

public class GetStatisticState extends TransactionUserCase<Void, GetStatisticState.StatisticState> {

    public GetStatisticState(Registry registry) {
        super(registry);
    }

    @Override
    protected StatisticState transactionalExecute(Void request, DAO dao) {
        StatisticState statisticState = new StatisticState();
        statisticState.smokeTimesTodayCounter = dao.getSmokeForPeriod(DateUtils.dateOnly(DateUtils.now()),
                DateUtils.dateOnly(DateUtils.addDays(DateUtils.now(), 1))).size();
        return statisticState;
    }

    public static class StatisticState {

        Integer smokeTimesTodayCounter;
        Float spendMoney;

        public Integer getSmokeTimesTodayCounter() {
            return smokeTimesTodayCounter;
        }

        public Float getSpendMoney() {
            return spendMoney;
        }
    }
}
