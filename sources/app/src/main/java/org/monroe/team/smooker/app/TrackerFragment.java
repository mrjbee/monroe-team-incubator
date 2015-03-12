package org.monroe.team.smooker.app;

import org.monroe.team.android.box.app.FragmentSupport;
import org.monroe.team.smooker.app.android.SmookerApplication;

public class TrackerFragment extends FragmentSupport<SmookerApplication>{

    @Override
    protected int getLayoutId() {
        return R.layout.fragement_tracker;
    }
}
