package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.EventMessenger;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

public class SetupQuitSmokeProgram extends UserCaseSupport<SetupQuitSmokeProgram.QuitSmokeProgramRequest,Void>{

    public SetupQuitSmokeProgram(Registry registry) {
        super(registry);
    }

    @Override
    public Void execute(QuitSmokeProgramRequest request) {
        if (request.level == QuitSmokeDifficultLevel.DISABLED){
            using(QuitSmokeProgramManager.class).disable();
        } else {
            QuitSmokeProgram quitSmokeProgram = using(QuitSmokeProgramManager.class).setup(request.level,
                    request.startSmokeCount,
                    request.endSmokeCount);
        }

        using(EventMessenger.class).send(Events.QUIT_SCHEDULE_UPDATED, true);
        return null;
    }

    public static class QuitSmokeProgramRequest{

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
