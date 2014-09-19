package org.monroe.team.smooker.app.common.quitsmoke;

import java.io.Serializable;

public class QuitSmokeData implements Serializable {

    public final QuitSmokeStrategyLevel level;

    public QuitSmokeData(QuitSmokeStrategyLevel level) {
        this.level = level;
    }


}
