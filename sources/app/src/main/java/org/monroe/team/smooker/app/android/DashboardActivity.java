package org.monroe.team.smooker.app.android;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.Lists;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.SmokeBreakActivity;
import org.monroe.team.smooker.app.android.view.SmokeChartView;
import org.monroe.team.smooker.app.android.view.SmokeHistogramView;
import org.monroe.team.smooker.app.android.view.TimerView;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.SupportActivity;
import org.monroe.team.smooker.app.common.constant.SetupPage;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.CalculateTodaySmokeSchedule;
import org.monroe.team.smooker.app.uc.CancelSmoke;
import org.monroe.team.smooker.app.uc.CheckDateAndAdjustData;
import org.monroe.team.smooker.app.uc.GetStatisticState;
import org.monroe.team.smooker.app.uc.OverNightUpdate;
import org.monroe.team.smooker.app.uc.RemoveSmoke;
import org.monroe.team.smooker.app.uc.UpdateQuitSmokeSchedule;
import org.monroe.team.smooker.app.uc.common.DateUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardActivity extends SupportActivity {

    public static final int WIZARD_ACTIVITY_FORCE_REQUEST = 1;
    public static final int WIZARD_ACTIVITY_REQUEST = 2;
    public static final int SMOKE_BREAK_ACTIVITY_REQUEST = 3;
    private PopupMenu settingsMenu;
    private SmokeChartView chartView;
    private SmokeHistogramView histogramView;

    private ListView calendarListView;
    private View lastTimeSmokeView;
    private ArrayAdapter<UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel> calendarListAdapter;
    private Timer lastSmokeTimer;
    private long lastTimeSmokeTime = -1;
    private long timeBeforeNextSmoke = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model().execute(CheckDateAndAdjustData.class,null);

        setContentView(R.layout.activity_dashboard);
        chartView = new SmokeChartView(this);
        histogramView = new SmokeHistogramView(this);

        calendarListView = new ListView(this);
        lastTimeSmokeView = getLayoutInflater().inflate(R.layout.last_time_smoke_panel, (ViewGroup) findViewById(R.id.d_content_layout),false);
        application().onDashboardCreate();

        view(ImageButton.class, R.id.add_smoke_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUiPerStatistic(model().execute(AddSmoke.class, null));
            }
        });

        view(ImageButton.class,R.id.d_setting_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingPopup();
            }
        });

        subscribeOnEvent(Events.SMOKE_COUNT_CHANGED,new Closure<Integer, Void>() {
            @Override
            public Void execute(Integer arg) {
                updateUiRequestStatisticState();
                return null;
            }
        });

        subscribeOnEvent(Events.QUIT_SCHEDULE_UPDATED,new Closure<Boolean, Void>() {
            @Override
            public Void execute(Boolean arg) {
                updateUIRequestQuitSchedule();
                return null;
            }
        });

        subscribeOnEvent(Events.SMOKE_SCHEDULE_CHANGED,new Closure<ArrayList<CalculateTodaySmokeSchedule.SmokeSuggestion>, Void>() {
            @Override
            public Void execute(ArrayList<CalculateTodaySmokeSchedule.SmokeSuggestion> arg) {
                updateUIForSmokeSchedule(arg);
                return null;
            }


        });

        view(RadioButton.class,R.id.d_chart_radio).setChecked(application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 0);
        view(RadioButton.class,R.id.d_calendar_radio).setChecked(application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 1);
        view(RadioButton.class,R.id.d_time_radio).setChecked(application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 2);
        view(RadioButton.class,R.id.d_histogram_radio).setChecked(application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 3);

        view(RadioButton.class,R.id.d_chart_radio).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    application().settings().set(Settings.CONTENT_VIEW_CONFIG,0);
                }
                updateUISelectContentView();
            }
        });

        view(RadioButton.class,R.id.d_calendar_radio).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    application().settings().set(Settings.CONTENT_VIEW_CONFIG,1);
                }
                updateUISelectContentView();
            }
        });

        view(RadioButton.class,R.id.d_time_radio).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    application().settings().set(Settings.CONTENT_VIEW_CONFIG,2);
                }
                updateUISelectContentView();
            }
        });

        view(RadioButton.class,R.id.d_histogram_radio).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    application().settings().set(Settings.CONTENT_VIEW_CONFIG,3);
                }
                updateUISelectContentView();
            }
        });

        calendarListAdapter = new ArrayAdapter<UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel>(this, R.layout.cal_item){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel dayModel = getItem(position);
                if (convertView == null){
                    convertView = DashboardActivity.this.getLayoutInflater().inflate(R.layout.cal_item,parent,false);
                }

                ((TextView)convertView.findViewById(R.id.cal_date_text)).setText(dayModel.getDateString());
                ((TextView)convertView.findViewById(R.id.cal_state_text)).setText("");
                ((TextView)convertView.findViewById(R.id.cal_text)).setText(dayModel.getText());

                if (!dayModel.isPast()){
                    if (dayModel.isToday()){
                        ((TextView)convertView.findViewById(R.id.cal_state_text)).setText(getString(R.string.today).toUpperCase());
                    }
                    if(position % 2 == 0) {
                        convertView.setBackgroundColor(getResources().getColor(R.color.calendar_future_bkg));
                    }else {
                        convertView.setBackgroundColor(getResources().getColor(R.color.calendar_future_bkg2));
                    }
                    convertView.findViewById(R.id.cal_date_panel).setBackgroundResource(R.drawable.date_bkg_blue);
                    ((TextView)convertView.findViewById(R.id.cal_date_comment)).setText(getString(R.string.new_day_limit));
                    ((TextView) convertView.findViewById(R.id.cal_date_text)).setTextColor(Color.WHITE);
                    ((TextView) convertView.findViewById(R.id.cal_text)).setShadowLayer(0,0,0,Color.BLACK);
                    ((TextView) convertView.findViewById(R.id.cal_text)).setTextColor(getResources().getColor(R.color.calendar_future_text));
                } else {
                    ((TextView) convertView.findViewById(R.id.cal_text)).setShadowLayer(6, 2, 2, Color.BLACK);
                    ((TextView) convertView.findViewById(R.id.cal_text)).setTextColor(Color.WHITE);
                    ((TextView) convertView.findViewById(R.id.cal_date_text)).setTextColor(getResources().getColor(R.color.calendar_past_text));
                    convertView.findViewById(R.id.cal_date_panel).setBackgroundResource(R.drawable.date_bkg_white);

                    if (dayModel.isSuccessful()){
                        ((TextView)convertView.findViewById(R.id.cal_date_comment)).setText(getString(R.string.new_day_limit_increased));
                        convertView.setBackgroundColor(getResources().getColor(R.color.calendar_past_bkg));
                    }else {
                        ((TextView)convertView.findViewById(R.id.cal_state_text)).setText(getString(R.string.limit_failed));
                        ((TextView)convertView.findViewById(R.id.cal_date_comment)).setText(getString(R.string.day_limit_exceeded));
                        convertView.setBackgroundColor(getResources().getColor(R.color.calendar_past_failed_bkg));
                    }
                }

                return convertView;
            }
        };
        calendarListView.setAdapter(calendarListAdapter);

        updateUISelectContentView();

        if (ExtraActionName.SETUP != checkIfExtraActionRequired(getIntent())){
            checkSetupRequired();
        }
        model().execute(OverNightUpdate.class,null);
        //will setup future model
        model().execute(CalculateTodaySmokeSchedule.class,null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIfExtraActionRequired(intent);

    }

    private ExtraActionName checkIfExtraActionRequired(Intent intent) {
        if (intent == null || intent.getExtras() == null){
            return ExtraActionName.NONE;
        }
        setIntent(new Intent(this,DashboardActivity.class));
        if (intent.getExtras().get("PAGE_STACK") != null){
            Intent s = new Intent(this, WizardActivity.class);
            s.putExtras(intent.getExtras());
            startActivityForResult(s, WIZARD_ACTIVITY_REQUEST);
            return ExtraActionName.SETUP;
        }

        if (ExtraActionName.SMOKE_DECISION.equals(intent.getExtras().get(ExtraActionName.class.getSimpleName()))){
            startActivity(new Intent(this, SmokeBreakActivity.class));//,SMOKE_BREAK_ACTIVITY_REQUEST);
            return ExtraActionName.SMOKE_DECISION;

        }
        return ExtraActionName.NONE;
    }

    private void showSettingPopup() {
        if (settingsMenu == null){
            settingsMenu = new PopupMenu(this,view(R.id.d_setting_btn));
            settingsMenu.inflate(R.menu.setting_popup_menu_layout);
            settingsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();

                    if (id == R.id.remove_smoke_item){
                        if (model().execute(RemoveSmoke.class,null)){
                            Toast.makeText(DashboardActivity.this,getString(R.string.removed_last_logged_smoke),Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DashboardActivity.this,getString(R.string.nothing_to_remove_as_last_loged_smoke),Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }

                    SetupPage page = null;
                    switch (id){
                        case R.id.setting_general_item: page = SetupPage.GENERAL; break;
                        case R.id.setting_quit_item: page = SetupPage.QUIT_PROGRAM; break;
                        case R.id.setting_ui_item: page = SetupPage.UI; break;
                        default: throw new IllegalStateException();
                    }
                    Intent intent = new Intent(DashboardActivity.this, WizardActivity.class);
                    intent.putExtra("PAGE_INDEX", 0);
                    intent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(Arrays.asList(page)));
                    intent.putExtra("FORCE",false);
                    startActivityForResult(intent, WIZARD_ACTIVITY_REQUEST);
                    return true;
                }
            });
        }
        settingsMenu.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUiRequestStatisticState();
        updateUIRequestQuitSchedule();
        lastSmokeTimer = new Timer("last_smoke_time",true);
        lastSmokeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (timeBeforeNextSmoke < 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (timeBeforeNextSmoke == -1) {
                                ((TextView)lastTimeSmokeView.findViewById(R.id.lts_time_before_caption_text)).setVisibility(View.GONE);
                                ((TextView)lastTimeSmokeView.findViewById(R.id.lts_time_before_text)).setVisibility(View.GONE);
                                ((TimerView) lastTimeSmokeView.findViewById(R.id.lts_timer_view)).setTimeOutProgress(-1f);
                            }
                        }
                    });
                }else {
                    long[] dayHrMinSec = DateUtils.splitPeriod(new Date(timeBeforeNextSmoke), DateUtils.now());
                    long hours = Math.max(0,dayHrMinSec[1]);
                    long minutes = Math.max(0,dayHrMinSec[2]);
                    long seconds = Math.max(0,dayHrMinSec[3]);
                    final long leftMinutes = DateUtils.asMinutes(timeBeforeNextSmoke-DateUtils.now().getTime());
                    final String time =  new StringBuilder()
                            .append((hours<10)?"0"+hours:hours)
                            .append(":")
                            .append((minutes<10)?"0"+minutes:minutes)
                            .append(":")
                            .append((seconds<10)?"0"+seconds:seconds)
                            .toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)lastTimeSmokeView.findViewById(R.id.lts_time_before_caption_text)).setVisibility(View.VISIBLE);
                            ((TextView)lastTimeSmokeView.findViewById(R.id.lts_time_before_text)).setVisibility(View.VISIBLE);
                            ((TextView) lastTimeSmokeView.findViewById(R.id.lts_time_before_text)).setText(time);
                            float val = leftMinutes > 60 ? 1f:(leftMinutes < 0 ? 0f : (float)leftMinutes/(float)60);
                            ((TimerView)lastTimeSmokeView.findViewById(R.id.lts_timer_view)).animateTimeOutProgress(val);
                        }
                    });
                }

                if (lastTimeSmokeTime < 1){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)lastTimeSmokeView.findViewById(R.id.lts_days_text)).setText("0 "+getString(R.string.single_day));
                            ((TextView)lastTimeSmokeView.findViewById(R.id.lts_time_text)).setText("00:00:00");
                            ((TimerView)lastTimeSmokeView.findViewById(R.id.lts_timer_view)).animateTimeProgress(0.5f);
                        }
                    });
                } else {
                   long[] dayHrMinSec = DateUtils.splitPeriod(DateUtils.now(), new Date(lastTimeSmokeTime));

                   final long days = dayHrMinSec[0];
                   long hours = dayHrMinSec[1];
                   final long minutes = dayHrMinSec[2];
                   final long seconds = dayHrMinSec[3];
                   final String time =  new StringBuilder()
                           .append((hours<10)?"0"+hours:hours)
                           .append(":")
                           .append((minutes<10)?"0"+minutes:minutes)
                           .append(":")
                           .append((seconds<10)?"0"+seconds:seconds)
                           .toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) lastTimeSmokeView.findViewById(R.id.lts_days_text)).setText(days +" "+ getString(R.string.few_days));
                            ((TextView) lastTimeSmokeView.findViewById(R.id.lts_time_text)).setText(time);
                            ((TimerView)lastTimeSmokeView.findViewById(R.id.lts_timer_view)).animateTimeProgress(seconds/(float)60);
                        }
                    });
                }
            }
        }, 0, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lastSmokeTimer != null) {
            lastSmokeTimer.cancel();
        }
        lastSmokeTimer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribeFromEvents();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
            switch(keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    showSettingPopup();
                    return true;
            }
            return super.onKeyUp(keyCode, event);
    }

    private void updateUiRequestStatisticState() {
        updateUiPerStatistic(model().execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(
                        GetStatisticState.StatisticName.ALL
                )
        ));
    }


    private void updateUiPerStatistic(GetStatisticState.StatisticState statistics) {
        if (exists(statistics.getTodaySmokeDates())){
            view(TextView.class,R.id.d_smoke_today_counter_text).setText(
                            (statistics.getAverageSmoke() < 1) ?
                                    Integer.toString(statistics.getTodaySmokeDates().size()):
                                    Integer.toString(statistics.getTodaySmokeDates().size())+"/"+Integer.toString(statistics.getAverageSmoke()));

            view(TextView.class,R.id.d_smoke_today_text).setText(
                    (statistics.getAverageSmoke() < 1) ?
                            getString(R.string.today_smokes):
                            getString(R.string.today_per_average_smokes));


            int modelSizeBefore = chartView.getModel() == null? 0 : chartView.getModel().size();
            int modelSizeAfter = statistics.getTodaySmokeDates().size();
            chartView.setModel(statistics.getTodaySmokeDates());
            if (modelSizeAfter != modelSizeBefore){
                //will be invalid
                chartView.setFutureModel(Collections.EMPTY_LIST);
            }
            chartView.invalidate();
        }

        if (exists(statistics.getMonthSmokeList())){
            histogramView.setModel(statistics.getMonthSmokeList());
        }

        if (exists(statistics.getTotalSmokes())){
            view(TextView.class,R.id.d_total_smokes_counter_text).setText(statistics.getTotalSmokes().toString()   );
        }


        if (exists(statistics.getSpendMoney())){
            view(TextView.class,R.id.d_spend_money_counter_text).setText(statistics.getSpendMoney());
        }

        if (statistics.isRequested(GetStatisticState.StatisticName.QUIT_SMOKE)){
            chartView.setLimit(statistics.getTodaySmokeLimit());
            chartView.invalidate();
            if (statistics.getQuitSmokeDifficult() == QuitSmokeDifficultLevel.DISABLED){
                view(R.id.d_calendar_radio).setVisibility(View.GONE);
                if (view(RadioButton.class,R.id.d_calendar_radio).isChecked()){
                    view(RadioButton.class,R.id.d_time_radio).setChecked(true);
                }
            } else {
                view(R.id.d_calendar_radio).setVisibility(View.VISIBLE);
            }
          }

        if (exists(statistics.getLastSmokeDate())){
                lastTimeSmokeTime = statistics.getLastSmokeDate().getTime();
        }

    }


    private void updateUISelectContentView() {
        view(LinearLayout.class, R.id.d_content_layout).removeAllViews();
        if (application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 0){
            view(LinearLayout.class, R.id.d_content_layout).setPadding(10,0,0,0);
            view(LinearLayout.class, R.id.d_content_layout).addView(chartView);
        } else if (application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 1) {
            view(LinearLayout.class, R.id.d_content_layout).setPadding(0,0,0,0);
            view(LinearLayout.class, R.id.d_content_layout).addView(calendarListView);
        } else  if (application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 2) {
            view(LinearLayout.class, R.id.d_content_layout).setPadding(0, 0, 0, 0);
            view(LinearLayout.class, R.id.d_content_layout).addView(lastTimeSmokeView);
        } else{
            view(LinearLayout.class, R.id.d_content_layout).setPadding(10, 0, 0, 0);
            view(LinearLayout.class, R.id.d_content_layout).addView(histogramView);
        }
    }


    private void updateUIRequestQuitSchedule() {
        UpdateQuitSmokeSchedule.QuitSmokeSchedule smokeSchedule = model().execute(UpdateQuitSmokeSchedule.class, null);
        if (smokeSchedule != null){
            calendarListAdapter.clear();
            for (UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel dayModel : smokeSchedule.scheduleList) {
                calendarListAdapter.add(dayModel);
            }
            calendarListAdapter.notifyDataSetChanged();
        }
    }

    private void updateUIForSmokeSchedule(ArrayList<CalculateTodaySmokeSchedule.SmokeSuggestion> scheduledSmokesList) {
        if (!scheduledSmokesList.isEmpty()){
            Date date = scheduledSmokesList.get(0).date;
            timeBeforeNextSmoke = date.getTime();
        } else {
            timeBeforeNextSmoke = -1;
        }

        chartView.setFutureModel(Lists.collect(scheduledSmokesList,new Closure<CalculateTodaySmokeSchedule.SmokeSuggestion, Date>() {
            @Override
            public Date execute(CalculateTodaySmokeSchedule.SmokeSuggestion arg) {
                return arg.date;
            }
        }));
        chartView.invalidate();
    }
    private boolean exists(Object value) {
        return value != null;
    }

    private void checkSetupRequired() {

        Pair<Boolean, List<SetupPage>> requiredSetup = application().getRequiredSetupPages();

        if (!requiredSetup.second.isEmpty()){
            Intent intent = new Intent(this, WizardActivity.class);
            intent.putExtra("PAGE_INDEX", 0);
            intent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(requiredSetup.second));
            intent.putExtra("FORCE",requiredSetup.first.booleanValue());
            startActivityForResult(intent, requiredSetup.first.booleanValue() ? WIZARD_ACTIVITY_FORCE_REQUEST : WIZARD_ACTIVITY_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case WIZARD_ACTIVITY_FORCE_REQUEST:{
                if (resultCode == RESULT_CANCELED){
                    finish();
                    return;
                }
                if (application().firstSetupDoneTrigger()){
                    application().updateStickyNotification(true);
                    application().settings().set(Settings.ENABLED_STATISTIC_NOTIFICATION,true);
                }
                checkSetupRequired();
                break;
            }
            case WIZARD_ACTIVITY_REQUEST:{
                checkSetupRequired();
                break;
            }
        }
    }

    public static PendingIntent openDashboard(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent openDashboardWithExtraAction(Context context, ExtraActionName name, Pair<String,Serializable> ... parameters){
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        intent.putExtra(ExtraActionName.class.getSimpleName(), name);
        for (Pair<String, Serializable> parameter : parameters) {
            intent.putExtra(parameter.first, parameter.second);
        }
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static enum ExtraActionName implements Serializable {
        SETUP, SMOKE_DECISION, NONE
    }
}
