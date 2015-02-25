package org.monroe.team.smooker.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.smooker.app.android.SmookerApplication;


public class StartActivity extends ActivitySupport<SmookerApplication> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }


}
