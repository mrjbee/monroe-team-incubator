package org.monroe.team.aas.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ToggleButton;
import org.monroe.team.aas.R;
import org.monroe.team.aas.model.ModelService;
import org.monroe.team.aas.ui.common.Logs;
import org.monroe.team.aas.ui.common.ServiceManager;
import org.monroe.team.aas.ui.common.logging.Debug;

/**
 * User: MisterJBee
 * Date: 7/31/13 Time: 11:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DashboardActivity extends ActionBarActivity implements ServiceManager.ServiceBinderOwner<ModelService.PublicModel>{

    private final ServiceManager<ModelService.PublicModel> mPublicModelManager =
            new ServiceManager<ModelService.PublicModel>(this, ModelService.class);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logs.UI.v("onCreateOptionsMenu() Activity = %s, menu = %s", this, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu, menu);
        MenuItem item = menu.findItem(R.id.test);
        Debug.i("Obtain menu item = %s [%s]", item,item.getActionView());
        ToggleButton button = (ToggleButton) item.getActionView();
        button.setChecked(false);
        button.setEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.UI.v("onCreate() Activity = %s", this);
        mPublicModelManager.obtain();
    }

    @Override
    protected void onDestroy() {
        Logs.UI.v("onDestroy() Activity = %s", this);
        super.onDestroy();
        mPublicModelManager.release();
    }

    @Override
    protected void onStart() {
        Logs.UI.v("onStart() Activity = %s", this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        Logs.UI.v("onStop() Activity = %s", this);
        super.onStart();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onObtain(ModelService.PublicModel publicModel) {
        Logs.UI.d("Obtain model = %s. Activity = %s", publicModel, this);
    }

    @Override
    public void onRelease() {
        Logs.UI.d("Release model. Activity = %s", this);
    }

}
