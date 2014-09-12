package org.monroe.team.smooker.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
                updateUiPerStatistic(model().execute(AddSmoke.class,null));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetStatisticState.StatisticState statistics = model().execute(GetStatisticState.class, null);
        updateUiPerStatistic(statistics);
    }

    private void updateUiPerStatistic(GetStatisticState.StatisticState statistics) {
        if (exists(statistics.getSmokeTimesTodayCounter())){
            view(TextView.class,R.id.d_smoke_today_counter_text).setText(Integer.toString(statistics.getSmokeTimesTodayCounter()));
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
