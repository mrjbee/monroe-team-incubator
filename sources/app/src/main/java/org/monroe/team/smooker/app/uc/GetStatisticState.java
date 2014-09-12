package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.dp.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetStatisticState extends TransactionUserCase<GetStatisticState.StatisticRequest, GetStatisticState.StatisticState> {

    public GetStatisticState(Registry registry) {
        super(registry);
    }

    @Override
    protected StatisticState transactionalExecute(StatisticRequest request, DAO dao) {
        StatisticState statisticState = new StatisticState();
        for (StatisticName statisticName : request.nameSet) {
            switch (statisticName){
                case SMOKE_TODAY:
                    statisticState.smokeTimesTodayCounter = dao.getSmokesForPeriod(DateUtils.dateOnly(DateUtils.now()),
                            DateUtils.dateOnly(DateUtils.addDays(DateUtils.now(), 1))).size();
                case SPEND_MONEY:
                    Float money = calculateSpendMoney(dao);
                    DecimalFormat df = new DecimalFormat();
                    df.setCurrency(using(Preferences.class).getCurrency().nativeInstance);
                    df.setMaximumFractionDigits(2);
                    df.setMinimumFractionDigits(2);
                    statisticState.spendMoney = df.format(money) +" "+using(Preferences.class).getCurrency().symbol;
            }
        }
        return statisticState;
    }

    private Float calculateSpendMoney(DAO dao) {
        List<DAO.Result> priceList = dao.getPrices();
        if (priceList.isEmpty()) return 0f;

        if (priceList.size() == 1){
            return priceList.get(0).get(0,Float.class) * dao.getSmokesAllPeriod().size();
        } else {
            float answer = 0;
            DAO.Result price;
            DAO.Result nextPrice = null;

            for (int i = 1; i < priceList.size(); i++) {
                price = priceList.get(i-1);
                nextPrice = priceList.get(i);
                long count = dao.getSmokesForPeriod(
                        i == 1 ? null : price.get(1, Date.class),
                        nextPrice.get(1, Date.class)
                ).size();
                answer += price.get(0, Float.class) * count;
            }

            price = nextPrice;
            long count = dao.getSmokesForPeriod(price.get(1, Date.class), null).size();
            answer += price.get(0, Float.class) * count;
            return answer;
        }
    }



    public static enum StatisticName{
        SMOKE_TODAY, SPEND_MONEY, ALL;
        private static final StatisticName[] ALL_NAMES = {SMOKE_TODAY, SPEND_MONEY};
    }

    public static class StatisticRequest {
        final Set<StatisticName> nameSet = new HashSet<StatisticName>();

        public static StatisticRequest create(StatisticName ... names){
            StatisticRequest request = new StatisticRequest();
            return request.with(names);
        }

        public StatisticRequest with(StatisticName... names) {
            if (names != null){
                for (StatisticName name : names) {
                    if (name == StatisticName.ALL){
                        nameSet.addAll(Arrays.asList(StatisticName.ALL_NAMES));
                        return this;
                    } else {
                        this.nameSet.add(name);
                    }
                }
            }
            return this;
        }
    }

    public static class StatisticState {

        Integer smokeTimesTodayCounter;
        String spendMoney;

        public Integer getSmokeTimesTodayCounter() {
            return smokeTimesTodayCounter;
        }

        public String getSpendMoney() {
            return spendMoney;
        }
    }
}
