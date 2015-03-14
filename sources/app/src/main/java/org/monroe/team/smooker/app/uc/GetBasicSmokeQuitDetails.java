package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.db.Dao;

import java.io.Serializable;

public class GetBasicSmokeQuitDetails extends UserCaseSupport<Void,GetBasicSmokeQuitDetails.BasicSmokeQuitDetails> {


    public GetBasicSmokeQuitDetails(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected BasicSmokeQuitDetails executeImpl(Void request) {

        QuitSmokeProgram quitSmokeProgram = using(QuitSmokeProgramManager.class).get();
        if (quitSmokeProgram != null) {
            return new BasicSmokeQuitDetails(
                    quitSmokeProgram.getLevel(),
                    quitSmokeProgram.getTodaySmokeCount(),
                    quitSmokeProgram.isChangedToday());

        } else {
            return new BasicSmokeQuitDetails(
                    QuitSmokeDifficultLevel.DISABLED,
                    -1,
                    false);
        }
    }


    public static class BasicSmokeQuitDetails implements Serializable {

        public final int limit;
        public final QuitSmokeDifficultLevel level;
        public final boolean isChangedToday;

        public BasicSmokeQuitDetails(QuitSmokeDifficultLevel level, int limit, boolean isChangedToday) {
            this.level = level;
            this.limit = limit;
            this.isChangedToday = isChangedToday;
        }
    }
}
