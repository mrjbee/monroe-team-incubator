package org.monroe.team.aas.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import org.monroe.team.aas.R;
import org.monroe.team.aas.model.PublicModelService;
import org.monroe.team.aas.ui.common.Logs;
import org.monroe.team.aas.ui.common.MilestoneDependedExecutionQueue;
import org.monroe.team.aas.ui.common.ServiceManager;

/**
 * User: MisterJBee
 * Date: 7/31/13 Time: 11:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DashboardActivity extends ActionBarActivity implements ServiceManager.ServiceBinderOwner<PublicModelService.PublicModel>{

    private final ServiceManager<PublicModelService.PublicModel> mPublicModelManager =
            new ServiceManager<PublicModelService.PublicModel>(this, PublicModelService.class);

    private MilestoneDependedExecutionQueue mModelObtainMilestoneQueue = new MilestoneDependedExecutionQueue();
    private ToggleButton mPublicGatewaySwitcherView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logs.UI.v("onCreateOptionsMenu() Activity = %s, menu = %s", this, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu, menu);
        mPublicGatewaySwitcherView = (ToggleButton) menu.findItem(R.id.test).getActionView();
        mPublicGatewaySwitcherView.setChecked(false);
        mPublicGatewaySwitcherView.setEnabled(false);
        mModelObtainMilestoneQueue.post(new Runnable() {
            @Override
            public void run() {
                mPublicGatewaySwitcherView.setChecked(mPublicModelManager.get().isPublicGatewayVisible());
                mPublicGatewaySwitcherView.setEnabled(true);
                mPublicModelManager.get().addPublicGatewayVisibilityListener(new PublicModelService.PublicModel.PublicGatewayVisibilityListener() {
                    @Override
                    public void onVisibilityChange(boolean newVisibility) {
                        if(mPublicGatewaySwitcherView != null)
                            mPublicGatewaySwitcherView.setChecked(newVisibility);
                    }
                });
                mPublicGatewaySwitcherView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                         compoundButton.setEnabled(false);
                         if (value == true){
                             mPublicModelManager.get().openPublicGateway();
                         } else {
                             mPublicModelManager.get().closePublicGateway();
                         }
                    }
                });
            }
        });
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
        mModelObtainMilestoneQueue.post(new Runnable() {
            @Override
            public void run() {
                mPublicModelManager.get().addPublicGatewayVisibilityListener(new PublicModelService.PublicModel.PublicGatewayVisibilityListener() {
                    @Override
                    public void onVisibilityChange(boolean newVisibility) {
                        if(mPublicGatewaySwitcherView != null){
                            mPublicGatewaySwitcherView.setEnabled(true);
                            mPublicGatewaySwitcherView.setChecked(newVisibility);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStop() {
        Logs.UI.v("onStop() Activity = %s", this);
        super.onStart();
        mPublicModelManager.get().removeAllListeners();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onObtain(PublicModelService.PublicModel publicModel) {
        Logs.UI.d("Obtain model = %s. Activity = %s", publicModel, this);
        mModelObtainMilestoneQueue.onMilestone();
    }

    @Override
    public void onRelease() {
        Logs.UI.d("Release model. Activity = %s", this);
    }

}
