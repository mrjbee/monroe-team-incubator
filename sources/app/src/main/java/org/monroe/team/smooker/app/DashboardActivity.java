package org.monroe.team.smooker.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.SupportActivity;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.CalculateRequiredSetupPages;
import org.monroe.team.smooker.app.uc.GetStatisticState;

import java.util.ArrayList;

public class DashboardActivity extends SupportActivity {

    private static final int WIZARD_ACTIVITY_FORCE_REQUEST = 1;
    private static final int WIZARD_ACTIVITY_REQUEST = 2;

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
        onDestroy();
        unSubscribeFromEvents();
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
