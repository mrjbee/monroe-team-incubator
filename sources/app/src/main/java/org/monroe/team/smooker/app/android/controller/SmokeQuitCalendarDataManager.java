package org.monroe.team.smooker.app.android.controller;

import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.app.uc.GetSmokeQuitSchedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmokeQuitCalendarDataManager {

    private final DataProvider<GetSmokeQuitSchedule.QuitSchedule> quitScheduleDataProvider;
    private final DateFormat dayOnlyDateFormat = new SimpleDateFormat("d");
    private final DateFormat monthOnlyDateFormat = new SimpleDateFormat("MMM");

    public SmokeQuitCalendarDataManager(DataProvider<GetSmokeQuitSchedule.QuitSchedule> quitScheduleDataProvider) {
        this.quitScheduleDataProvider = quitScheduleDataProvider;
    }

    public void calculateCalendarLimits(final OnLimitsCalculated resultObserver){
        quitScheduleDataProvider.fetch(true,new DataProvider.FetchObserver<GetSmokeQuitSchedule.QuitSchedule>() {

            @Override
            public void onFetch(GetSmokeQuitSchedule.QuitSchedule quitSchedule) {

                if (quitSchedule.scheduleDates.size() == 0){
                    resultObserver.onLimit(null, null);
                }

                resultObserver.onLimit(
                        quitSchedule.scheduleDates.get(0).date,
                        Lists.getLast(quitSchedule.scheduleDates).date);

            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                resultObserver.onError(new DataProvider.FetchException(fetchError));
            }
        });
    }

    public static interface  OnLimitsCalculated{
        public void onLimit(Date startDate, Date endDate);
        public void onError(Exception e);
    }

    public DisplayDetails getSmokeQuitDateDisplayDetails(Date date) {
        String mainText = dayOnlyDateFormat.format(date);
        if ("1".equals(mainText)){
            mainText = monthOnlyDateFormat.format(date);
        }
        DisplayDetails answer = new DisplayDetails(mainText);

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
