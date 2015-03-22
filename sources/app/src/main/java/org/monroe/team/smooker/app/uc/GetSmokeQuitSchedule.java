package org.monroe.team.smooker.app.uc;

import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeData;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetSmokeQuitSchedule extends UserCaseSupport<Void, GetSmokeQuitSchedule.QuitSchedule>{

    public GetSmokeQuitSchedule(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected QuitSchedule executeImpl(Void request) {
        QuitSmokeProgram program = using(QuitSmokeProgramManager.class).get();
        if (program == null)
            return new QuitSchedule(null);
        List<QuitScheduleDate> quitScheduleDateList = new ArrayList<>();
        for (int i=0; i<program.getStages().size(); i++) {
            QuitSmokeData.Stage stage = program.getStages().get(i);
            if (i == 0) {
                 QuitScheduleDate quitScheduleDate = new QuitScheduleDate(stage.date, true, stage.result == QuitSmokeData.QuiteStageResult.PASS, stage.smokeLimit);
                quitScheduleDateList.add(quitScheduleDate);
            } else {
                QuitSmokeData.Stage prevStage = program.getStages().get(i-1);
                if (prevStage.smokeLimit != stage.smokeLimit) {
                    quitScheduleDateList.add(new QuitScheduleDate(stage.date, true, stage.result == QuitSmokeData.QuiteStageResult.PASS, stage.smokeLimit));
                } else if (stage.result == QuitSmokeData.QuiteStageResult.FAILS) {
                    quitScheduleDateList.add(new QuitScheduleDate(stage.date, false, false, stage.smokeLimit));
                }
            }
        }
        return new QuitSchedule(quitScheduleDateList);
    }

    public static class QuitSchedule implements Serializable {

        public final List<QuitScheduleDate> scheduleDates;

        public QuitSchedule(List<QuitScheduleDate> scheduleDates) {
            this.scheduleDates = scheduleDates;
        }

        public boolean isDisabled() {
            return scheduleDates == null;
        }
    }

    public static class QuitScheduleDate implements Serializable{

        public final Date date;
        public final boolean isNewLimitDate;
        public final boolean successful;
        public final int limit;

        public QuitScheduleDate(Date date, boolean isNewLimitDate, boolean isSuccessful, int limit) {
            this.limit = limit;
            //EEST EET issue with date calculation
            this.date = DateUtils.dateOnly(date);
            this.isNewLimitDate = isNewLimitDate;
            successful = isSuccessful;
        }
    }
}
