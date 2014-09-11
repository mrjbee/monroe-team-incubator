package org.monroe.team.smooker.app;

import android.content.Intent;
import android.os.Bundle;

import org.monroe.team.smooker.app.common.SupportActivity;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.uc.CalculateRequiredSetupPages;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends SupportActivity {

    private static final int WIZARD_ACTIVITY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        List<SetupPage> requestStack = model().execute(CalculateRequiredSetupPages.class,null);
        if (!requestStack.isEmpty()){
            Intent intent = new Intent(this, WizardActivity.class);
            intent.putExtra("PAGE_INDEX", 0);
            intent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(requestStack));
            startActivityForResult(intent, WIZARD_ACTIVITY_REQUEST);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case WIZARD_ACTIVITY_REQUEST:{
                if (resultCode == RESULT_CANCELED){
                    finish();
                    return;
                }
            }
        }
    }
}
