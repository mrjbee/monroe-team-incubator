package org.monroe.team.smooker.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rasterizer;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends SupportActivity {

    static final int WIZARD_ACTIVITY_FORCE_REQUEST = 1;
    static final int WIZARD_ACTIVITY_REQUEST = 2;
    private PopupMenu settingsMenu;
    private SmokeChartView chartView;
    private ListView calendarListView;
    private ArrayAdapter<UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel> calendarListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chartView = new SmokeChartView(this);
        calendarListView = new ListView(this);
        setContentView(R.layout.activity_dashboard);
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

        view(RadioButton.class,R.id.d_chart_radio).setChecked(application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 0);
        view(RadioButton.class,R.id.d_calendar_radio).setChecked(application().settings().get(Settings.CONTENT_VIEW_CONFIG) == 1);

        view(RadioButton.class,R.id.d_chart_radio).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                application().settings().set(Settings.CONTENT_VIEW_CONFIG,isChecked?0:1);
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
        if (application().settings().get(Settings.CONTENT_VIEW_CONFIG)==0){
            view(LinearLayout.class, R.id.d_content_layout).setPadding(10,0,0,0);
            view(LinearLayout.class, R.id.d_content_layout).addView(chartView);
        } else {
            view(LinearLayout.class, R.id.d_content_layout).setPadding(0,0,0,0);
            view(LinearLayout.class, R.id.d_content_layout).addView(calendarListView);
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
            view(TextView.class,R.id.d_smoke_today_counter_text).setText(Integer.toString(statistics.getTodaySmokeDates().size()));
            chartView.setModel(statistics.getTodaySmokeDates());
        }
        if (exists(statistics.getSpendMoney())){
            view(TextView.class,R.id.d_spend_money_counter_text).setText(statistics.getSpendMoney());
        }

        if(exists(statistics.getAverageSmoke())){
            view(TextView.class,R.id.d_smoke_average_value_text).setText(String.valueOf(statistics.getAverageSmoke()));
        }

        if (statistics.isRequested(GetStatisticState.StatisticName.AVERAGE_PER_DAY)) {
            view(TextView.class, R.id.d_smoke_average_value_text).setVisibility(exists(statistics.getAverageSmoke()) ? View.VISIBLE : View.INVISIBLE);
            view(TextView.class, R.id.d_smoke_average_text).setVisibility(exists(statistics.getAverageSmoke()) ? View.VISIBLE : View.INVISIBLE);
        }

        if (statistics.isRequested(GetStatisticState.StatisticName.QUIT_SMOKE)){
            chartView.setLimit(statistics.getTodaySmokeLimit());
            if (statistics.getQuitSmokeDifficult() == QuitSmokeDifficultLevel.DISABLED){
                view(R.id.d_content_view_group).setVisibility(View.INVISIBLE);
                view(RadioButton.class,R.id.d_chart_radio).setChecked(true);
            } else {
                view(R.id.d_content_view_group).setVisibility(View.VISIBLE);
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
}
