package org.monroe.team.smooker.app.android;

import android.os.Bundle;
import android.view.ViewGroup;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.smooker.app.R;

public abstract class SetupGeneralActivity extends ActivitySupport<SmookerApplication> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_general);
        getLayoutInflater().inflate(setup_layout(), (ViewGroup)view(R.id.setup_content_panel), true);
    }

    protected abstract int setup_layout();
}
