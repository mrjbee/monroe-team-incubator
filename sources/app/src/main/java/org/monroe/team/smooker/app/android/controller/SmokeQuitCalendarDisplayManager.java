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

    public DisplayDetails getSmokeQuitDateDisplayDetails(Date date) {
        String mainText = dayOnlyDateFormat.format(date);
        boolean isMonth = false;
        if ("1".equals(mainText)){
            mainText = monthOnlyDateFormat.format(date);
            isMonth = true;
        }
        DisplayDetails answer = new DisplayDetails(mainText);
        answer.isMonthStart = isMonth;
        return answer;
    }

    public static class DisplayDetails {

        public String mainText;
        public String optionalText;
        public boolean isPassed;
        public boolean isFuture;
        public boolean isMonthStart;

        public DisplayDetails(String mainText) {
            this(mainText,"",false,false,false);
        }

        public DisplayDetails(String mainText, String optionalText, boolean isPassed, boolean isFuture, boolean isMonthStart) {
            this.mainText = mainText;
            this.optionalText = optionalText;
            this.isPassed = isPassed;
            this.isFuture = isFuture;
            this.isMonthStart = isMonthStart;
        }
    }
}
