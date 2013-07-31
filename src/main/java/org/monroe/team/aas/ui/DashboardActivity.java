package org.monroe.team.aas.ui;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import org.monroe.team.aas.R;
import roboguice.activity.RoboFragmentActivity;

/**
 * User: MisterJBee
 * Date: 7/31/13 Time: 11:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DashboardActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
