package org.monroe.team.aas.ui;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import org.monroe.team.aas.R;
import org.monroe.team.aas.ui.common.Logs;

import roboguice.activity.RoboFragmentActivity;

/**
 * User: MisterJBee
 * Date: 7/31/13 Time: 11:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DashboardActivity extends ActionBarActivity {


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logs.UI.v("onCreateOptionsMenu() Activity = %s, menu = %s", this, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        Logs.UI.v("onStart() Activity = %s", this);
        super.onStart();
    }
}
