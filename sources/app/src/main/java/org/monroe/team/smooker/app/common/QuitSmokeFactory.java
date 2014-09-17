package org.monroe.team.smooker.app.common;

import android.content.Context;

public class QuitSmokeFactory {

    private final Context context;
    private QuitSmokeStrategy currentStrategy;

    public QuitSmokeFactory(Context context) {
        this.context = context;
    }

    QuitSmokeStrategy get(QuitSmokeStrategyLevel level){
        return null;
    }
}
