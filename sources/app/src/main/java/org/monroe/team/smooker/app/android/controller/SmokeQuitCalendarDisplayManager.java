package org.monroe.team.smooker.app.android.controller;

import android.util.Pair;

import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.app.uc.GetSmokeQuitSchedule;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SmokeQuitCalendarDisplayManager {

    private final DataProvider<GetSmokeQuitSchedule.QuitSchedule> quitScheduleDataProvider;
    private final DateFormat dayOnlyDateFormat = new SimpleDateFormat("d");
    private final DateFormat monthOnlyDateFormat = new SimpleDateFormat("MMM");
    private Calendar calendar = Calendar.getInstance();

    public SmokeQuitCalendarDisplayManager(DataProvider<GetSmokeQuitSchedule.QuitSchedule> quitScheduleDataProvider) {
        this.quitScheduleDataProvider = quitScheduleDataProvider;
    }

    public void calculateCalendarLimits(final OnLimitsCalculated resultObserver){
        quitScheduleDataProvider.fetch(true,new DataProvider.FetchObserver<GetSmokeQuitSchedule.QuitSchedule>() {
            @Override
            public void onFetch(GetSmokeQuitSchedule.QuitSchedule quitSchedule) {

                if (quitSchedule.scheduleDates == null || quitSchedule.scheduleDates.size() == 0){
                    resultObserver.onLimit(null, null);
                    return;
                }

                Date startDate = quitSchedule.scheduleDates.get(0).date;
                Date endDate = Lists.getLast(quitSchedule.scheduleDates).date;

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

    public List<Pair<String,Boolean>> weekDaysNames() {
        List<Pair<String,Boolean>> answer = new ArrayList<>(7);
        int firstDay = calendar.getFirstDayOfWeek();
        DateFormatSymbols formatSymbols = new DateFormatSymbols();
        String[] shortWeekdays = formatSymbols.getShortWeekdays();
        for (int i=0;i<7;i++){
            int dayIndex = (i+firstDay) % 8;
            if (dayIndex == 0) dayIndex = 1;
            String name = shortWeekdays[dayIndex];
            answer.add(i,new Pair<String, Boolean>(name,dayIndex==Calendar.SATURDAY||dayIndex==Calendar.SUNDAY));
        }
        return answer;
    }


    public static interface  OnLimitsCalculated{
        public void onLimit(Date startDate, Date endDate);
        public void onError(Exception e);
    }

    public DisplayDetails getSmokeQuitDateDisplayDetails(Date probeDate) {
        DisplayDetails answer = new DisplayDetails();
        answer.mainText = dayOnlyDateFormat.format(probeDate);
        answer.isMonthStart = false;
        if ("1".equals(answer.mainText)){
            answer.mainText = monthOnlyDateFormat.format(probeDate);
            if (answer.mainText.length() > 3){
                answer.mainText = answer.mainText.substring(0,3)+".";
            }
            answer.isMonthStart = true;
        }

        calendar.setTime(probeDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        answer.isWeekEnd  = dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY;

        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        if (dayOfMonth != 1){
            int positionInALocalWeek = dayOfWeek - calendar.getFirstDayOfWeek();
            if (positionInALocalWeek < 0) positionInALocalWeek = 7+positionInALocalWeek;
            int dayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if ((dayOfMonth+7-positionInALocalWeek) > dayInMonth){
                answer.isMonthEndWeek = true;
            }
            if (dayOfMonth <= positionInALocalWeek){
                answer.isMonthStartWeek = true;
            }
        }

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
        public boolean isMonthEndWeek = false;
        public boolean isMonthStartWeek = false;
    }
}
