package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeStrategyLevel;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

public class SetupQuitSmokeProgram extends UserCaseSupport<SetupQuitSmokeProgram.QuitSmokeProgramRequest,Void>{

    public SetupQuitSmokeProgram(Registry registry) {
        super(registry);
    }

    @Override
    public Void execute(QuitSmokeProgramRequest request) {
        if (request.level == QuitSmokeStrategyLevel.DISABLED){
            using(QuitSmokeProgramManager.class).disable();
        } else {
            QuitSmokeProgram quitSmokeProgram = using(QuitSmokeProgramManager.class).setup(request.level,
                    request.startSmokeCount,
                    request.endSmokeCount);
        }

        return null;
    }

    public static class QuitSmokeProgramRequest{

        private final QuitSmokeStrategyLevel level;
        private final int startSmokeCount;
        private final int endSmokeCount;

        public QuitSmokeProgramRequest(QuitSmokeStrategyLevel level, int startSmokeCount, int endSmokeCount) {
            this.level = level;
            this.startSmokeCount = startSmokeCount;
            this.endSmokeCount = endSmokeCount;
            if (level != QuitSmokeStrategyLevel.DISABLED && startSmokeCount == -1){
                throw new IllegalStateException();
            }
        }
    }
}
