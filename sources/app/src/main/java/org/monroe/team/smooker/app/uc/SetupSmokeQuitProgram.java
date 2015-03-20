package org.monroe.team.smooker.app.uc;

import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;

public class SetupSmokeQuitProgram extends UserCaseSupport<SetupSmokeQuitProgram.QuitSmokeProgramRequest,Void> {

    public SetupSmokeQuitProgram(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    public Void executeImpl(QuitSmokeProgramRequest request) {
        if (request.level == QuitSmokeDifficultLevel.DISABLED){
            using(QuitSmokeProgramManager.class).disable();
        } else {
           QuitSmokeProgram quitSmokeProgram = using(QuitSmokeProgramManager.class).setup(request.level,
                    request.startSmokeCount,
                    request.endSmokeCount);
        }
        return null;
    }

    public static class QuitSmokeProgramRequest {

        private final QuitSmokeDifficultLevel level;
        private final int startSmokeCount;
        private final int endSmokeCount;

        public QuitSmokeProgramRequest(QuitSmokeDifficultLevel level, int startSmokeCount, int endSmokeCount) {
            this.level = level;
            this.startSmokeCount = startSmokeCount;
            this.endSmokeCount = endSmokeCount;
            if (level != QuitSmokeDifficultLevel.DISABLED &&
                level != QuitSmokeDifficultLevel.HARDEST &&
                startSmokeCount == -1){
                throw new IllegalStateException();
            }
        }
    }
}
