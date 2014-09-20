package org.monroe.team.smooker.app.common.quitsmoke;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuitSmokeData implements Serializable {

    public final QuitSmokeDifficultLevel level;
    public final List<Stage> stageList = new ArrayList<Stage>();

    public QuitSmokeData(QuitSmokeDifficultLevel level) {
           this.level = level;
    }

    public static class Stage implements Serializable {

        public QuiteStageResult result = QuiteStageResult.IN_FUTURE;
        public final int smokeLimit;
        public final Date date;

        public Stage(int smokeLimit, Date date) {
            this.smokeLimit = smokeLimit;
            this.date = date;
        }
    }

    public static enum QuiteStageResult implements Serializable{
        PASS, FAILS, IN_FUTURE
    }



}
