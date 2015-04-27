package org.monroe.team.smooker.app.android;

import android.os.Bundle;
import android.view.View;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.smooker.app.R;

public class PreferencesActivity extends ActivitySupport<SmookerApplication>{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        view(R.id.setup_quit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
