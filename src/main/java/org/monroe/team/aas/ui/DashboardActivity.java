package org.monroe.team.aas.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import org.monroe.team.aas.R;
import org.monroe.team.aas.model.ModelService;
import org.monroe.team.aas.ui.common.Logs;

/**
 * User: MisterJBee
 * Date: 7/31/13 Time: 11:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DashboardActivity extends ActionBarActivity {

    private final PublicModelConnection mPublicModelConnection = new PublicModelConnection();
    private ModelService.PublicModel mModel;

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
        Logs.UI.v("Start service() Activity = %s", this);
        startService(new Intent(this, ModelService.class));
        Logs.UI.v("Bind to service. Activity = %s", this);
        //TODO: Choose appropriate for model service
        bindService(new Intent(this, ModelService.class), mPublicModelConnection, BIND_ADJUST_WITH_ACTIVITY);
    }

    @Override
    protected void onStop() {
        Logs.UI.v("onStop() Activity = %s", this);
        super.onStart();
        Logs.UI.v("Unbind from service. Activity = %s", this);
        unbindService(mPublicModelConnection);
    }

    private class PublicModelConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DashboardActivity.this.bindModel((ModelService.PublicModel) iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            DashboardActivity.this.unbindModel();
        }
    }

    private void bindModel(ModelService.PublicModel iBinder) {
        Logs.UI.i("Model attached. Activity = %s. Model = %s", this, iBinder);
        mModel = iBinder;
    }

    private void unbindModel() {
        Logs.UI.i("Model detached. Activity = %s.", this);
        mModel = null;
    }

}
