package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeData;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateQuitSmokeSchedule extends TransactionUserCase<Void,UpdateQuitSmokeSchedule.QuitSmokeSchedule> {

    public UpdateQuitSmokeSchedule(Registry registry) {
        super(registry);
    }

    @Override
    protected QuitSmokeSchedule transactionalExecute(Void request, DAO dao) {
        QuitSmokeProgram program = using(QuitSmokeProgramManager.class).get();
        if (program == null) return null;
        QuitSmokeSchedule smokeSchedule = new QuitSmokeSchedule();
        Date today = DateUtils.dateOnly(DateUtils.now());
        DateFormat dateFormat = DateFormat.getDateInstance();
        for (QuitSmokeData.Stage stage : program.getStages()) {
            QuitSmokeSchedule.DayModel dayModel = new QuitSmokeSchedule.DayModel();
            dayModel.date = stage.date;
            dayModel.dateString = dateFormat.format(stage.date);
            dayModel.text = stage.smokeLimit +" smokes";
            if (stage.result == QuitSmokeData.QuiteStageResult.IN_FUTURE){
                dayModel.past =false;
            } else if (stage.result == QuitSmokeData.QuiteStageResult.PASS){
                dayModel.successful = true;
                dayModel.past = true;
            } else {
                dayModel.successful = false;
                dayModel.past = true;
            }
           smokeSchedule.scheduleList.add(dayModel);
           if (today.compareTo(dayModel.date) <= 0){
               smokeSchedule.nearest = dayModel;
           }
        }
        smokeSchedule.nearest.past = true;
        smokeSchedule.nearest.successful = false;
        return smokeSchedule;
    }

    public static class QuitSmokeSchedule{

        public final List<DayModel> scheduleList = new ArrayList<DayModel>();

        public DayModel nearest;

        public static class DayModel{
            Date date;
            boolean successful;
            boolean past;
            String text;

            private String dateString;

            public Date getDate() {
                return date;
            }

            public boolean isSuccessful() {
                return successful;
            }

            public boolean isPast() {
                return past;
            }

            @Override
            public String toString() {
                return "DayModel{" +
                        "date=" + date.toString() +
                        ", successful=" + successful +
                        ", past=" + past +
                        '}';
            }

            public String getDateString() {
                return dateString;
            }

            public String getText() {
                return text;
            }
        }

    }
}
