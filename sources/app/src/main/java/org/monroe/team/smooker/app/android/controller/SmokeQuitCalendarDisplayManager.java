package org.monroe.team.smooker.app.android.controller;

import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.app.uc.GetSmokeQuitSchedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SmokeQuitCalendarDisplayManager {

    private final DataProvider<GetSmokeQuitSchedule.QuitSchedule> quitScheduleDataProvider;
    private final DateFormat dayOnlyDateFormat = new SimpleDateFormat("d");
    private final DateFormat monthOnlyDateFormat = new SimpleDateFormat("MMM");

    public SmokeQuitCalendarDisplayManager(DataProvider<GetSmokeQuitSchedule.QuitSchedule> quitScheduleDataProvider) {
        this.quitScheduleDataProvider = quitScheduleDataProvider;
    }

    public void calculateCalendarLimits(final OnLimitsCalculated resultObserver){
        quitScheduleDataProvider.fetch(true,new DataProvider.FetchObserver<GetSmokeQuitSchedule.QuitSchedule>() {

            @Override
            public void onFetch(GetSmokeQuitSchedule.QuitSchedule quitSchedule) {

                if (quitSchedule.scheduleDates.size() == 0){
                    resultObserver.onLimit(null, null);
                }

                Date startDate = quitSchedule.scheduleDates.get(0).date;
                Date endDate = Lists.getLast(quitSchedule.scheduleDates).date;

                Calendar calendar = Calendar.getInstance();
                int dayCount = calculateDaysPastInThisWeek(startDate, calendar);
                startDate = DateUtils.mathDays(startDate, -dayCount);

                dayCount = calculateDaysPastInThisWeek(endDate, calendar) + 1;
                if (dayCount != 7){
                    endDate = DateUtils.mathDays(endDate, 7 - dayCount);
                }
                resultObserver.onLimit(
                        startDate,endDate);

            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                resultObserver.onError(new DataProvider.FetchException(fetchError));
            }
        });
    }

    private int calculateDaysPastInThisWeek(Date date, Calendar calendar) {

        calendar.setTime(date);

        int daysToAdd = calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek();

        if (daysToAdd < 0){
            daysToAdd = 7 + daysToAdd;
        }
        return daysToAdd;
    }

    public static interface  OnLimitsCalculated{
        public void onLimit(Date startDate, Date endDate);
        public void onError(Exception e);
    }

    public DisplayDetails getSmokeQuitDateDisplayDetails(Date probeDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(probeDate);
        DisplayDetails answer = new DisplayDetails();
        answer.mainText = dayOnlyDateFormat.format(probeDate);
        answer.isMonthStart = false;
        if ("1".equals(answer.mainText)){
            answer.mainText = monthOnlyDateFormat.format(probeDate);
            answer.isMonthStart = true;
        }
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        answer.isWeekEnd  = dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY;


        GetSmokeQuitSchedule.QuitSchedule quitSchedule = quitScheduleDataProvider.getData();
        if (quitSchedule.isDisabled()){
            return answer;
        }

        Date today = DateUtils.today();
        answer.isFuture = !probeDate.before(today);
        GetSmokeQuitSchedule.QuitScheduleDate itQuitScheduleDate;
        for (int i = 0; i < quitSchedule.scheduleDates.size(); i++){
            itQuitScheduleDate = quitSchedule.scheduleDates.get(i);
            if (probeDate.before(itQuitScheduleDate.date)){
                if (i == 0){
                    answer.isOutsideQuitProgram = true;
                    return answer;
                }else{
                    answer.isPassed = true;
                    return  answer;
                }
            }
            if (!probeDate.after(itQuitScheduleDate.date)){
                //mean same date
                answer.isNewLimitDay = itQuitScheduleDate.isNewLimitDate;
                answer.isPassed = itQuitScheduleDate.successful;
                return answer;
            }
        }
        answer.isOutsideQuitProgram = true;
        return answer;
    }

    public static class DisplayDetails {
        public String mainText = "";
        public String optionalText = "";
        public boolean isPassed = false;
        public boolean isFuture = true;
        public boolean isMonthStart = false;
        public boolean isNewLimitDay = false;
        public boolean isOutsideQuitProgram = false;
        public boolean isWeekEnd = false;
    }
}
