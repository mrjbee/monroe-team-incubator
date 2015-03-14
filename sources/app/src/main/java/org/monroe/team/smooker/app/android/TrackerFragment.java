package org.monroe.team.smooker.app.android;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.android.view.DayTrackChartView;
import org.monroe.team.smooker.app.uc.GetSmokeStatistic;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeDetails;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeSchedule;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;

public class TrackerFragment extends FrontPageFragment{

    private AppearanceController addSmokeBtnAC;

    @Override
    protected int getLayoutId() {
        return R.layout.fragement_tracker;
    }


    @Override
    protected void onActivityCreatedSafe(Bundle savedInstanceState) {
        addSmokeBtnAC =  animateAppearance(view(R.id.add_btn), scale(1f,0f))
                .showAnimation(duration_constant(300), interpreter_overshot())
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.4f))
                .hideAndInvisible()
                .build();
    }

    private void fetchSchedule(boolean updateRequired) {
        application().data_smokeSchedule().fetch(updateRequired, new DataProvider.FetchObserver<PrepareTodaySmokeSchedule.TodaySmokeSchedule>() {
            @Override
            public void onFetch(PrepareTodaySmokeSchedule.TodaySmokeSchedule todaySmokeSchedule) {
                updateChart(todaySmokeSchedule);
            }
            @Override
            public void onError(DataProvider.FetchError fetchError) {
                activity().forceCloseWithErrorCode(301);
            }
        });
    }

    private void updateChart(PrepareTodaySmokeSchedule.TodaySmokeSchedule todaySmokeSchedule) {
        DayTrackChartView chartView = view(R.id.tracker_day_chart_view, DayTrackChartView.class);
        if (todaySmokeSchedule.isLimitSet()){
            chartView.setLimit(todaySmokeSchedule.limit);
        } else {
            chartView.setLimit(-1);
        }
        chartView.setModel(todaySmokeSchedule.todaySmokes);
        chartView.setFutureModel(todaySmokeSchedule.scheduledSmokes.isEmpty()?null:todaySmokeSchedule.scheduledSmokes);
        chartView.invalidate();
    }

    @Override
    public void onScreenSizeCalculatedSafe(int width, int height) {
        final float show_edge = width/2;
        final float hide_edge = width - DisplayUtils.dpToPx(50+70+40, getResources());
        view(R.id.tracker_day_chart_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    addSmokeBtnAC.show();
                } else {
                    if (event.getX() > hide_edge){
                        addSmokeBtnAC.hide();
                    } else if (event.getX() < show_edge){
                        addSmokeBtnAC.show();
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onBackPressedSafe() {
        return false;
    }

    @Override
    protected void onInvalidData(Class invalidDataClass) {
        if (GetSmokeStatistic.SmokeStatistic.class == invalidDataClass) {
            fetchSchedule(true);
        }
    }

    @Override
    protected void onPauseSafe() {
        Event.unSubscribeFromEvents(activity(), this);
    }
}
