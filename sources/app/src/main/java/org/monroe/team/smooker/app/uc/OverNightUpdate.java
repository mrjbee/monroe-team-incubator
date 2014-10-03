package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.manager.EventMessenger;
import org.monroe.team.android.box.manager.SettingManager;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.android.box.manager.ServiceRegistry;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

import java.util.Date;

public class OverNightUpdate extends TransactionUserCase<Void,Void> {

    public OverNightUpdate(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Void request, final DAO dao) {
        Date today = DateUtils.dateOnly(DateUtils.now());
        Date date = usingModel().usingService(SettingManager.class).getAs(Settings.LAST_OVERNIGHT_UPDATE_DATE, Settings.CONVERT_DATE);
        if (date != null && date.compareTo(today) == 0){
            return null;
        }

        GetStatisticState.StatisticState statisticState = usingModel().execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(
                        GetStatisticState.StatisticName.SMOKE_TODAY));
        using(EventMessenger.class).send(Events.SMOKE_COUNT_CHANGED, statisticState.getTodaySmokeDates().size());
        using(QuitSmokeProgramManager.class).update(new Closure<QuitSmokeProgram, Void>() {
            @Override
            public Void execute(QuitSmokeProgram program) {
                    Date examDate = program.getLastLoggedDate();
                    if (examDate == null) {
                        examDate = program.getStartDate();
                    } else {
                        examDate = DateUtils.mathDays(examDate,1);
                    }
                    Date today = DateUtils.dateOnly(DateUtils.now());
                    boolean scheduleChanged = false;
                    while (examDate.compareTo(today) < 0){
                        Date nextAfterExamDate = DateUtils.mathDays(examDate,1);
                        int smokeCount  = dao.getSmokesForPeriod(examDate, nextAfterExamDate).size();
                        if(program.doLogSmokesForDate(examDate, smokeCount)) {
                            scheduleChanged = true;
                        }
                        examDate = nextAfterExamDate;
                    }

                    if (scheduleChanged){
                        using(EventMessenger.class).send(Events.QUIT_SCHEDULE_UPDATED, true);
                    }
                return null;
            }
        });

        using(SettingManager.class).set(Settings.LAST_OVERNIGHT_UPDATE_DATE, today.getTime());
        return null;
    }
}
