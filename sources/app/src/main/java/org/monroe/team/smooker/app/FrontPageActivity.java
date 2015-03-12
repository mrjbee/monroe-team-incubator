package org.monroe.team.smooker.app;

import android.os.Bundle;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.smooker.app.android.SmookerApplication;


public class FrontPageActivity extends ActivitySupport<SmookerApplication> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        if (isFirstRun(savedInstanceState)){
            TilesFragment tilesFragment = new TilesFragment();
            TrackerFragment trackerFragment = new TrackerFragment();
            trackerFragment.setArguments(getIntent().getExtras());
            tilesFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .add(R.id.fp_tiles_fragment_panel, tilesFragment,"tiles_fragment")
                    .add(R.id.fp_tracker_fragment_panel, trackerFragment, "tracker_fragment" )
            .commit();
        }
    }

    @Override
    protected void onActivitySize(int width, int height) {
        super.onActivitySize(width, height);
        getTilesFragment().onScreenSizeCalculated(width, height);
    }

    private TilesFragment getTilesFragment() {
        TilesFragment tilesFragment = (TilesFragment) getFragmentManager().findFragmentByTag("tiles_fragment");
        return tilesFragment;
    }

    private TilesFragment getTrackerFragment() {
        TilesFragment tilesFragment = (TilesFragment) getFragmentManager().findFragmentByTag("tracker_fragment");
        return tilesFragment;
    }

    @Override
    public void onBackPressed() {
        if (!getTilesFragment().onBackPressed()){
            super.onBackPressed();
        }
    }
}
