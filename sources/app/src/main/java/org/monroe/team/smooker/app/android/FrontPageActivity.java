package org.monroe.team.smooker.app.android;

import android.os.Bundle;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.smooker.app.R;


public class FrontPageActivity extends ActivitySupport<SmookerApplication> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        FrontPageFragment mainFragment;
        if (isLandscape(R.bool.class)){
            mainFragment = new TrackerFragment();
        }else {
            mainFragment = new TilesFragment();
        }
        mainFragment.setArguments(getIntent().getExtras());

        if (isFirstRun(savedInstanceState)){
            getFragmentManager().beginTransaction()
                    .add(R.id.fp_fragment_panel, mainFragment,"main_fragment")
            .commit();
        } else {
            getFragmentManager().beginTransaction()
                .replace(R.id.fp_fragment_panel, mainFragment, "main_fragment")
            .commit();
        }
    }

    @Override
    protected void onActivitySize(int width, int height) {
        super.onActivitySize(width, height);
        getMainFragment().onScreenSizeCalculated(width, height);
    }

    private FrontPageFragment getMainFragment() {
        return (FrontPageFragment) getFragmentManager().findFragmentByTag("main_fragment");
    }


    @Override
    public void onBackPressed() {
        if (!getMainFragment().onBackPressed()){
            super.onBackPressed();
        }
    }

    public boolean isFragmentActive(Class<? extends FrontPageFragment> fragmentClass) {
        return  (isLandscape(R.bool.class) &&  TrackerFragment.class == fragmentClass)
                || (!isLandscape(R.bool.class) &&  TilesFragment.class == fragmentClass);
    }
}
