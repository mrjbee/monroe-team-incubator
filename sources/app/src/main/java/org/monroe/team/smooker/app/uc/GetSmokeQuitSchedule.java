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
    private static long requestId = 1;
    @Override
    protected QuitSchedule executeImpl(Void request) {
        QuitSmokeProgram program = using(QuitSmokeProgramManager.class).get();
        if (program == null)
            return new QuitSchedule(null, requestId++);
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
        return new QuitSchedule(quitScheduleDateList, requestId++);
    }

    public static class QuitSchedule implements Serializable {

        public final List<QuitScheduleDate> scheduleDates;
        public final long incrementalId;

        public QuitSchedule(List<QuitScheduleDate> scheduleDates, long incrementalId) {
            this.scheduleDates = scheduleDates;
            this.incrementalId = incrementalId;
        }

        public boolean isDisabled() {
            return scheduleDates == null;
        }

        public QuitScheduleDate getForDate(Date probeDate){
            if (isDisabled()) return null;
            GetSmokeQuitSchedule.QuitScheduleDate itQuitScheduleDate;
            for (int i = 0; i < scheduleDates.size(); i++){
                itQuitScheduleDate = scheduleDates.get(i);
                if (probeDate.before(itQuitScheduleDate.date)){
                    if (i == 0){
                        return null;
                    }else{
                        return  new QuitScheduleDate(probeDate,
                                false,
                                true,
                                scheduleDates.get(i-1).limit);
                    }
                }
                if (!probeDate.after(itQuitScheduleDate.date)){
                    //mean same date
                    return new QuitScheduleDate(probeDate,
                            itQuitScheduleDate.isNewLimitDate,
                            itQuitScheduleDate.successful,
                            itQuitScheduleDate.limit);
                }
            }
            return null;
        }
    }

    public static class QuitScheduleDate implements Serializable{

        public final Date date;
        public final int limit;
        public final boolean isNewLimitDate;
        public final boolean successful;

        public QuitScheduleDate(Date date, boolean isNewLimitDate, boolean isSuccessful, int limit) {
            this.limit = limit;
            //EEST EET issue with date calculation
            this.date = DateUtils.dateOnly(date);
            this.isNewLimitDate = isNewLimitDate;
            successful = isSuccessful;
        }
    }
}
