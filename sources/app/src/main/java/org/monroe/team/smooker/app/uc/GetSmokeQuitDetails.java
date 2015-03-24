package org.monroe.team.smooker.app.uc;

import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;

import java.io.Serializable;

public class GetSmokeQuitDetails extends UserCaseSupport<Void,GetSmokeQuitDetails.Details> {


    public GetSmokeQuitDetails(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Details executeImpl(Void request) {

        QuitSmokeProgram quitSmokeProgram = using(QuitSmokeProgramManager.class).get();
        if (quitSmokeProgram != null) {
            return new Details(
                    quitSmokeProgram.getLevel(),
                    quitSmokeProgram.getTodaySmokeCount(),
                    quitSmokeProgram.isChangedToday());

        } else {
            return new Details(
                    QuitSmokeDifficultLevel.DISABLED,
                    -1,
                    false);
        }
    }


    public static class Details implements Serializable {

        public final int limit;
        public final QuitSmokeDifficultLevel level;
        public final boolean isChangedToday;

        public Details(QuitSmokeDifficultLevel level, int limit, boolean isChangedToday) {
            this.level = level;
            this.limit = limit;
            this.isChangedToday = isChangedToday;
        }
    }
}
