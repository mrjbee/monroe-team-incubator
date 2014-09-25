package org.monroe.team.smooker.app;

import android.app.PendingIntent;
import android.content.Context;
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

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.Settings;
import org.monroe.team.smooker.app.common.SupportActivity;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.GetStatisticState;
import org.monroe.team.smooker.app.uc.RemoveSmoke;
import org.monroe.team.smooker.app.uc.UpdateQuitSmokeSchedule;
import org.monroe.team.smooker.app.uc.common.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardActivity extends SupportActivity {

    static final int WIZARD_ACTIVITY_FORCE_REQUEST = 1;
    static final int WIZARD_ACTIVITY_REQUEST = 2;
    private PopupMenu settingsMenu;
    private SmokeChartView chartView;
    private ListView calendarListView;
    private View lastTimeSmokeView;
    private ArrayAdapter<UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel> calendarListAdapter;
    private Timer lastSmokeTimer;
    private long lastTimeSmokeTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        chartView = new SmokeChartView(this);
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
                requestAndUpdateUiPerStatisticState();
                return null;
            }
        });

        subscribeOnEvent(Events.QUIT_SCHEDULE_UPDATED,new Closure<Boolean, Void>() {
            @Override
            public Void execute(Boolean arg) {
                requestAndUpdateQuitSchedule();
                return null;
            }
        });



        view(RadioButton.class,R.id.d_chart_radio).setChecked(application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 0);
        view(RadioButton.class,R.id.d_calendar_radio).setChecked(application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 1);

        view(RadioButton.class,R.id.d_chart_radio).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    application().settings().set(Settings.CONTENT_VIEW_CONFIG,0);
                }
                updateContentView();
            }
        });

        view(RadioButton.class,R.id.d_calendar_radio).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    application().settings().set(Settings.CONTENT_VIEW_CONFIG,1);
                }
                updateContentView();
            }
        });

        view(RadioButton.class,R.id.d_time_radio).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    application().settings().set(Settings.CONTENT_VIEW_CONFIG,2);
                }
                updateContentView();
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
                    if(position % 2 == 0) {
                        convertView.setBackgroundColor(getResources().getColor(R.color.calendar_future_bkg));
                    }else {
                        convertView.setBackgroundColor(getResources().getColor(R.color.calendar_future_bkg2));
                    }
                    convertView.findViewById(R.id.cal_date_panel).setBackgroundResource(R.drawable.date_bkg_blue);
                    ((TextView)convertView.findViewById(R.id.cal_date_comment)).setText("next day limit");
                    ((TextView) convertView.findViewById(R.id.cal_date_text)).setTextColor(Color.WHITE);
                    ((TextView) convertView.findViewById(R.id.cal_text)).setShadowLayer(0,0,0,Color.BLACK);
                    ((TextView) convertView.findViewById(R.id.cal_text)).setTextColor(getResources().getColor(R.color.calendar_future_text));
                } else {
                    ((TextView) convertView.findViewById(R.id.cal_text)).setShadowLayer(6,2,2,Color.BLACK);
                    ((TextView) convertView.findViewById(R.id.cal_text)).setTextColor(Color.WHITE);
                    ((TextView) convertView.findViewById(R.id.cal_date_text)).setTextColor(getResources().getColor(R.color.calendar_past_text));
                    convertView.findViewById(R.id.cal_date_panel).setBackgroundResource(R.drawable.date_bkg_white);

                    if (dayModel.isSuccessful()){
                        ((TextView)convertView.findViewById(R.id.cal_date_comment)).setText("day limit decreased to");
                        convertView.setBackgroundColor(getResources().getColor(R.color.calendar_past_bkg));
                    }else {
                        ((TextView)convertView.findViewById(R.id.cal_state_text)).setText("FAILED");
                        ((TextView)convertView.findViewById(R.id.cal_date_comment)).setText("day limit exceeded");
                        convertView.setBackgroundColor(getResources().getColor(R.color.calendar_past_failed_bkg));
                    }
                }

                return convertView;
            }
        };
        calendarListView.setAdapter(calendarListAdapter);

        updateContentView();

        if (!checkIfSetupRequested(getIntent())){
            checkSetupRequired();
        }
    }


    private void updateContentView() {
        view(LinearLayout.class, R.id.d_content_layout).removeAllViews();
        if (application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 0){
            view(LinearLayout.class, R.id.d_content_layout).setPadding(10,0,0,0);
            view(LinearLayout.class, R.id.d_content_layout).addView(chartView);
        } else if (application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 1) {
            view(LinearLayout.class, R.id.d_content_layout).setPadding(0,0,0,0);
            view(LinearLayout.class, R.id.d_content_layout).addView(calendarListView);
        } else {
            view(LinearLayout.class, R.id.d_content_layout).setPadding(0, 0, 0, 0);
            view(LinearLayout.class, R.id.d_content_layout).addView(lastTimeSmokeView);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIfSetupRequested(intent);
    }

    private boolean checkIfSetupRequested(Intent intent) {
        if (intent != null && intent.getExtras() != null && intent.getExtras().get("PAGE_STACK")!=null){
            Intent s = new Intent(this, WizardActivity.class);
            s.putExtras(intent.getExtras());
            startActivityForResult(s,WIZARD_ACTIVITY_REQUEST);
            return true;
        }
        return false;
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
                            Toast.makeText(DashboardActivity.this,"Last logged smoke was removed",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DashboardActivity.this,"There were no smoke breaks in last 30 minutes",Toast.LENGTH_SHORT).show();
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
        requestAndUpdateUiPerStatisticState();
        requestAndUpdateQuitSchedule();
        lastSmokeTimer = new Timer("last_smoke_time",true);
        lastSmokeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (lastTimeSmokeTime < 1){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)lastTimeSmokeView.findViewById(R.id.lts_days_text)).setText("0 day ago");
                            ((TextView)lastTimeSmokeView.findViewById(R.id.lts_time_text)).setText("00:00:00");
                        }
                    });
                } else {
                   long nowMs = DateUtils.now().getTime();
                   long rest = nowMs - lastTimeSmokeTime;
                   final long days = rest / (24*60*60*1000);
                   rest = rest % (24*60*60*1000);
                   long hours = rest / (60*60*1000);
                   rest = rest % (60*60*1000);
                   long minutes = rest / (60*1000);
                   rest = rest % (60*1000);
                   long seconds = rest / 1000;
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
                            ((TextView) lastTimeSmokeView.findViewById(R.id.lts_days_text)).setText(days + " days ago");
                            ((TextView) lastTimeSmokeView.findViewById(R.id.lts_time_text)).setText(time);
                        }
                    });
                }
            }
        }, 0, 1500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lastSmokeTimer != null) {
            lastSmokeTimer.cancel();
        }
        lastSmokeTimer = null;
    }

    private void requestAndUpdateQuitSchedule() {
        UpdateQuitSmokeSchedule.QuitSmokeSchedule smokeSchedule = model().execute(UpdateQuitSmokeSchedule.class, null);
        if (smokeSchedule != null){
            calendarListAdapter.clear();
            for (UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel dayModel : smokeSchedule.scheduleList) {
                calendarListAdapter.add(dayModel);
            }
            calendarListAdapter.notifyDataSetChanged();
        }
    }

    private void requestAndUpdateUiPerStatisticState() {
        updateUiPerStatistic(model().execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(
                        GetStatisticState.StatisticName.ALL
                )));
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

    private void updateUiPerStatistic(GetStatisticState.StatisticState statistics) {
        if (exists(statistics.getTodaySmokeDates())){
            view(TextView.class,R.id.d_smoke_today_counter_text).setText(
                    Integer.toString(statistics.getTodaySmokeDates().size())+"/"+
                            Integer.toString(statistics.getAverageSmoke()));
            chartView.setModel(statistics.getTodaySmokeDates());
        }
        if (exists(statistics.getSpendMoney())){
            view(TextView.class,R.id.d_spend_money_counter_text).setText(statistics.getSpendMoney());
        }

        if (statistics.isRequested(GetStatisticState.StatisticName.QUIT_SMOKE)){
            chartView.setLimit(statistics.getTodaySmokeLimit());
            if (statistics.getQuitSmokeDifficult() == QuitSmokeDifficultLevel.DISABLED){
                view(R.id.d_calendar_radio).setVisibility(View.GONE);
                if (view(RadioButton.class,R.id.d_calendar_radio).isChecked()){
                    view(RadioButton.class,R.id.d_time_radio).setChecked(true);
                }
            } else {
                view(R.id.d_calendar_radio).setVisibility(View.VISIBLE);
            }
        }
        if (statistics.isRequested(GetStatisticState.StatisticName.LAST_LOGGED_SMOKE)){
            if (exists(statistics.getLastSmokeDate())){
                lastTimeSmokeTime = statistics.getLastSmokeDate().getTime();
            } else {
              //TODO: think if leave blank
            }
        }
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
        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // Open NotificationView.java Activity
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
