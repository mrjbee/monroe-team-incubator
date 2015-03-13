package org.monroe.team.smooker.app.uc.underreview;

import android.util.Pair;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.db.Dao;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetStatisticState extends TransactionUserCase<GetStatisticState.StatisticRequest, GetStatisticState.StatisticState, Dao> {

    public GetStatisticState(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected StatisticState transactionalExecute(StatisticRequest request, Dao dao) {
        StatisticState statisticState = new StatisticState();
        statisticState.requested = request.nameSet;

        for (StatisticName statisticName : request.nameSet) {
            switch (statisticName){
                case SMOKE_YESTERDAY:

                    List<DAOSupport.Result> smokeDaoList = dao.getSmokesForPeriod(
                                    DateUtils.dateOnly(DateUtils.mathDays(DateUtils.now(), -1)),
                                    DateUtils.dateOnly(DateUtils.now())
                                    );
                    statisticState.yesterdaySmokeDates = new ArrayList<Date>(smokeDaoList.size());
                    for (int i = 0; i < smokeDaoList.size() ; i++) {
                        statisticState.yesterdaySmokeDates.add(smokeDaoList.get(i).get(1, Date.class));
                    }
                    statisticState.yesterdaySmokeDates = Collections.unmodifiableList(statisticState.yesterdaySmokeDates);
                break;
                case SMOKE_TODAY:
                    List<DAOSupport.Result> todaySmokeDaoList = dao.getSmokesForPeriod(DateUtils.dateOnly(DateUtils.now()),
                            DateUtils.dateOnly(DateUtils.mathDays(DateUtils.now(), 1)));
                    statisticState.todaySmokeDates = new ArrayList<Date>(todaySmokeDaoList.size());
                    for (int i = 0; i < todaySmokeDaoList.size() ; i++) {
                        statisticState.todaySmokeDates.add(todaySmokeDaoList.get(i).get(1, Date.class));
                    }
                    statisticState.todaySmokeDates = Collections.unmodifiableList(statisticState.todaySmokeDates);

                    List<DAOSupport.Result> results = dao.groupSmokesPerDay();
                    Integer average = 0;
                    if (results.size() > 1){
                        int answer = 0;
                        for (int i=0; i<results.size()-1; i++){
                            answer+= results.get(i).get(1,Long.class);
                        }
                        average = Math.round(answer/(results.size()));
                    }
                    statisticState.averageSmoke = average;
                    break;
                case SPEND_MONEY:
                    Float money = calculateSpendMoney(dao);
                    DecimalFormat df = new DecimalFormat();
                    df.setCurrency(using(SettingManager.class).getAs(Settings.CURRENCY_ID, Settings.CONVERT_CURRENCY).nativeInstance);
                    df.setMaximumFractionDigits(2);
                    df.setMinimumFractionDigits(2);
                    statisticState.spendMoney = df.format(money) +" "+using(SettingManager.class).getAs(Settings.CURRENCY_ID, Settings.CONVERT_CURRENCY).symbol;
                    break;
                case QUIT_SMOKE:
                    QuitSmokeProgram quitSmokeProgram = using(QuitSmokeProgramManager.class).get();
                    if (quitSmokeProgram != null) {
                        statisticState.todaySmokeLimit = quitSmokeProgram.getTodaySmokeCount();
                        statisticState.smokeLimitChangedToday = quitSmokeProgram.isChangedToday();
                        statisticState.quitSmokeDifficult = quitSmokeProgram.getLevel();
                    } else {
                        statisticState.todaySmokeLimit = -1;
                        statisticState.quitSmokeDifficult = QuitSmokeDifficultLevel.DISABLED;
                    }
                    break;
                case LAST_LOGGED_SMOKE:
                        DAOSupport.Result result = dao.getLastLoggedSmoke();
                        if (result != null){
                            statisticState.lastSmokeDate = result.get(1,Date.class);
                        } else {
                            statisticState.lastSmokeDate = new Date(using(SettingManager.class).get(Settings.APP_FIRST_TIME_DATE));
                        }
                    break;
                case TOTAL_SMOKES:
                      statisticState.totalSmokes =  dao.getSmokesAllPeriod().size();
                    break;
                case LAST_30_DAYS_COUNT:
                    statisticState.monthSmokeList = new ArrayList<Pair<Date, Integer>>();
                    List<DAOSupport.Result> loggedDates = dao.groupSmokesPerDay();
                    Date today = DateUtils.dateOnly(DateUtils.now());
                    Date itDate = null;
                    int itSmokeCount = 0;
                    boolean wasValuable = false;
                    for (int i = 31; i > -1; i--){
                        itDate = DateUtils.mathDays(today,-i);
                        itSmokeCount = getSmokeCountForDate(itDate, loggedDates);
                        if (wasValuable || itSmokeCount > 0){
                            wasValuable = true;
                            statisticState.monthSmokeList.add(new Pair<Date, Integer>(itDate,itSmokeCount));
                        }
                    }
                    for (int i=1; statisticState.monthSmokeList.size() < 31;i++){
                        itDate = DateUtils.mathDays(today,i);
                        statisticState.monthSmokeList.add(new Pair<Date, Integer>(itDate,0));
                    }
                    break;
                }

        }
        return statisticState;
    }

    private int getSmokeCountForDate(Date itDate, List<DAOSupport.Result> loggedDates) {
        for (DAOSupport.Result loggedDate : loggedDates) {
            if (loggedDate.get(0,Date.class).compareTo(itDate) == 0){
                return (int)loggedDate.get(1,Long.class).longValue();
            }
        }
        return 0;
    }

    private Float calculateSpendMoney(Dao dao) {
        return using(SettingManager.class).get(Settings.SMOKE_PRICE) * dao.getSmokesAllPeriod().size();
    }


    public static enum StatisticName{
        SMOKE_TODAY, SMOKE_YESTERDAY, SPEND_MONEY, QUIT_SMOKE, LAST_LOGGED_SMOKE, TOTAL_SMOKES, LAST_30_DAYS_COUNT, ALL;
        private static final StatisticName[] ALL_NAMES = {SMOKE_TODAY, SPEND_MONEY, QUIT_SMOKE, TOTAL_SMOKES, LAST_LOGGED_SMOKE, LAST_30_DAYS_COUNT};
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
        List<Date> yesterdaySmokeDates;
        String spendMoney;
        Integer averageSmoke;
        Integer todaySmokeLimit;
        Set<StatisticName> requested;
        QuitSmokeDifficultLevel quitSmokeDifficult;
        Date lastSmokeDate;
        Boolean smokeLimitChangedToday;
        Integer totalSmokes;
        List<Pair<Date, Integer>> monthSmokeList;


        public List<Pair<Date, Integer>> getMonthSmokeList() {
            return monthSmokeList;
        }

        public List<Date> getYesterdaySmokeDates() {
            return yesterdaySmokeDates;
        }

        public Boolean getSmokeLimitChangedToday() {
            return smokeLimitChangedToday;
        }

        public List<Date> getTodaySmokeDates() {
            return todaySmokeDates;
        }

        public String getSpendMoney() {
            return spendMoney;
        }

        public Integer getTotalSmokes() {return totalSmokes;}

        public Integer getAverageSmoke() {
            return averageSmoke;
        }

        public Integer getTodaySmokeLimit() {
            return todaySmokeLimit;
        }

        public boolean isRequested(StatisticName name){
            return requested.contains(name);
        }

        public QuitSmokeDifficultLevel getQuitSmokeDifficult() {
            return quitSmokeDifficult;
        }

        public Date getLastSmokeDate() {
            return lastSmokeDate;
        }
    }
}
