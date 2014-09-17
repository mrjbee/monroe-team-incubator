package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.EventMessenger;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.Settings;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
                    List<DAO.Result> todaySmokeDaoList = dao.getSmokesForPeriod(DateUtils.dateOnly(DateUtils.now()),
                            DateUtils.dateOnly(DateUtils.addDays(DateUtils.now(), 1)));
                    statisticState.todaySmokeDates = new ArrayList<Date>(todaySmokeDaoList.size());
                    for (int i = 0; i < todaySmokeDaoList.size() ; i++) {
                        statisticState.todaySmokeDates.add(todaySmokeDaoList.get(i).get(1, Date.class));
                    }
                    statisticState.todaySmokeDates = Collections.unmodifiableList(statisticState.todaySmokeDates);
                    break;
                case SPEND_MONEY:
                    Float money = calculateSpendMoney(dao);
                    DecimalFormat df = new DecimalFormat();
                    df.setCurrency(using(Settings.class).getAs(Settings.CURRENCY_ID, Settings.CONVERT_CURRENCY).nativeInstance);
                    df.setMaximumFractionDigits(2);
                    df.setMinimumFractionDigits(2);
                    statisticState.spendMoney = df.format(money) +" "+using(Settings.class).getAs(Settings.CURRENCY_ID, Settings.CONVERT_CURRENCY).symbol;
                    break;
                case AVERAGE_PER_DAY:
                    List<DAO.Result> results = dao.groupSmokesPerDay();
                    Integer average = null;
                    if (results.size() > 3){
                        //at least two full days
                        int answer = 0;
                        for (int i=1; i<results.size()-1; i++){
                           answer+= results.get(i).get(1,Long.class);
                        }
                        average = Math.round(answer/(results.size()-2));
                    }
                    statisticState.averageSmoke = average;
                    break;
            }
        }
        return statisticState;
    }

    private Float calculateSpendMoney(DAO dao) {
        return using(Settings.class).get(Settings.SMOKE_PRICE) * dao.getSmokesAllPeriod().size();
    }

    public static enum StatisticName{
        SMOKE_TODAY, SPEND_MONEY, AVERAGE_PER_DAY, ALL;
        private static final StatisticName[] ALL_NAMES = {SMOKE_TODAY, SPEND_MONEY, AVERAGE_PER_DAY};
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

        List<Date> todaySmokeDates;
        String spendMoney;
        Integer averageSmoke;

        public List<Date> getTodaySmokeDates() {
            return todaySmokeDates;
        }

        public String getSpendMoney() {
            return spendMoney;
        }

        public Integer getAverageSmoke() {
            return averageSmoke;
        }
    }
}
