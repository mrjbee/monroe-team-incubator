package org.monroe.team.aas.ui.common;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: MisterJBee
 * Date: 8/15/13 Time: 10:51 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class MilestoneDependedExecutionQueue {

    public final ArrayList<Runnable> mDelayedActionList = new ArrayList<Runnable>(5);
    public final AtomicBoolean mReadyToExecute = new AtomicBoolean(false);

    public void post(Runnable runnable){
        if (mReadyToExecute.get()){
            runnable.run();
        } else {
            mDelayedActionList.add(runnable);
        }
    }

    public void onMilestone(){
        mReadyToExecute.set(true);
        for (int i = mDelayedActionList.size()-1; i>=0; i--){
            mDelayedActionList.remove(mDelayedActionList.size()-1).run();
        }
    }

}
