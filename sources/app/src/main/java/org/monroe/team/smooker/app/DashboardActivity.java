package org.monroe.team.smooker.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.SupportActivity;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.CalculateRequiredSetupPages;
import org.monroe.team.smooker.app.uc.GetStatisticState;

import java.util.ArrayList;
import java.util.Arrays;

public class DashboardActivity extends SupportActivity {

    private static final int WIZARD_ACTIVITY_FORCE_REQUEST = 1;
    private static final int WIZARD_ACTIVITY_REQUEST = 2;
    private PopupMenu settingsMenu;
    private boolean isQuitProgramAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        checkSetupRequired();

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
    }

    private void showSettingPopup() {
        if (settingsMenu == null){
            settingsMenu = new PopupMenu(this,view(R.id.d_setting_btn));
            settingsMenu.inflate(R.menu.setting_popup_menu_layout);
            settingsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();
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
        settingsMenu.getMenu().getItem(2).setEnabled(isQuitProgramAvailable);
        settingsMenu.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        subscribeOnEvent(Events.ADD_SMOKE,new Closure<Integer, Void>() {
            @Override
            public Void execute(Integer arg) {
                requestAndUpdateUiPerStatisticState();
                return null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestAndUpdateUiPerStatisticState();
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
            view(SmokeChartView.class,R.id.d_smoke_chart_view).setModel(statistics.getTodaySmokeDates());
        }
        if (exists(statistics.getSpendMoney())){
            view(TextView.class,R.id.d_spend_money_counter_text).setText(statistics.getSpendMoney());
        }

        if(exists(statistics.getAverageSmoke())){
            view(SmokeChartView.class,R.id.d_smoke_chart_view).setLimit(statistics.getAverageSmoke());
        }
    }

    private boolean exists(Object value) {
        return value != null;
    }

    private void checkSetupRequired() {
        CalculateRequiredSetupPages.RequiredSetupResponse requiredSetup = model().execute(CalculateRequiredSetupPages.class,null);
        if (!requiredSetup.setupPageList.isEmpty()){
            Intent intent = new Intent(this, WizardActivity.class);
            intent.putExtra("PAGE_INDEX", 0);
            intent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(requiredSetup.setupPageList));
            intent.putExtra("FORCE",requiredSetup.force);
            startActivityForResult(intent, requiredSetup.force ? WIZARD_ACTIVITY_FORCE_REQUEST : WIZARD_ACTIVITY_REQUEST);
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
