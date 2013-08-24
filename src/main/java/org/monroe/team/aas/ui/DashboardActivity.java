package org.monroe.team.aas.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import org.monroe.team.aas.R;
import org.monroe.team.aas.model.PublicModelModelService;
import org.monroe.team.aas.ui.common.Logs;
import org.monroe.team.aas.ui.common.MilestoneDependedExecutionQueue;
import org.monroe.team.aas.ui.components.ApplicationDetailsComponent;
import org.monroe.team.aas.ui.components.AvailableApplicationListComponent;
import org.monroe.team.libdroid.mservice.ModelClient;

/**
 * User: MisterJBee
 * Date: 7/31/13 Time: 11:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DashboardActivity extends ActionBarActivity
        implements ModelClient.ModelServiceClient<PublicModelModelService.PublicModel>,
        AvailableApplicationListComponent.AvailableApplicationListComponentDelegate,
        ApplicationDetailsComponent.ApplicationDetailsComponentDelegate{

    private final ModelClient<PublicModelModelService.PublicModel> mPublicModelManagerModel =
            new ModelClient<PublicModelModelService.PublicModel>(this, PublicModelModelService.class);

    private MilestoneDependedExecutionQueue mModelObtainMilestoneQueue = new MilestoneDependedExecutionQueue();
    private ToggleButton mPublicGatewaySwitcherView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logs.UI.v("onCreateOptionsMenu() Activity = %s, menu = %s", this, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu, menu);

        mPublicGatewaySwitcherView = (ToggleButton) ((SupportMenuItem)menu.findItem(R.id.test)).getActionView();
        mPublicGatewaySwitcherView.setChecked(false);
        mPublicGatewaySwitcherView.setEnabled(false);
        mModelObtainMilestoneQueue.post(new Runnable() {
            @Override
            public void run() {
                mPublicGatewaySwitcherView.setChecked(mPublicModelManagerModel.get().isPublicGatewayVisible());
                mPublicGatewaySwitcherView.setEnabled(true);
                mPublicModelManagerModel.get().addPublicGatewayVisibilityListener(new PublicModelModelService.PublicModel.PublicGatewayVisibilityListener() {
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
                             mPublicModelManagerModel.get().openPublicGateway();
                         } else {
                             mPublicModelManagerModel.get().closePublicGateway();
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
        setContentView(R.layout.main_layout);
        mPublicModelManagerModel.obtain();
        if (savedInstanceState == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.m_apps_frag, new AvailableApplicationFragment(),"av_app_frag")
                    .commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.m_apps_details_frag, new ApplicationDetailsFragment(),"app_det_frag")
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        Logs.UI.v("onDestroy() Activity = %s", this);
        super.onDestroy();
        mPublicModelManagerModel.get().removeAllListeners();
        mModelObtainMilestoneQueue.clear();
        mPublicModelManagerModel.releaseAndDestroy();
        if(mPublicGatewaySwitcherView!=null){
            mPublicGatewaySwitcherView.setOnCheckedChangeListener(null);
        }
    }

    @Override
    protected void onStart() {
        Logs.UI.v("onStart() Activity = %s", this);
        super.onStart();
        mModelObtainMilestoneQueue.post(new Runnable() {
            @Override
            public void run() {
                mPublicModelManagerModel.get().addPublicGatewayVisibilityListener(new PublicModelModelService.PublicModel.PublicGatewayVisibilityListener() {
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
        mPublicModelManagerModel.get().removeAllListeners();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onObtain(PublicModelModelService.PublicModel publicModel) {
        Logs.UI.d("Obtain model = %s. Activity = %s", publicModel, this);
        mModelObtainMilestoneQueue.onMilestone();
    }

    @Override
    public void onRelease(PublicModelModelService.PublicModel publicModel) {
        Logs.UI.d("Release model. Activity = %s", this);
    }


    @Override
    public void onApplicationSelected(String appId) {
       getAppDetailsComponent().showApplicationDetails(appId);
    }


    private ApplicationDetailsComponent getAppDetailsComponent(){
        return (ApplicationDetailsComponent) getSupportFragmentManager().findFragmentByTag("app_det_frag");
    }

    private AvailableApplicationListComponent getAppListComponent(){
        return (AvailableApplicationListComponent) getSupportFragmentManager().findFragmentByTag("av_app_frag");
    }

}
