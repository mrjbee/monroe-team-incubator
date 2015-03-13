package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.EventMessenger;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.smooker.app.common.Model;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.db.Dao;

import java.util.Date;

public class OverNightUpdate extends TransactionUserCase<Void,Void,Dao> {

    public OverNightUpdate(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Void request, final Dao dao) {

        Date today = DateUtils.dateOnly(DateUtils.now());
        Date date = using(Model.class).usingService(SettingManager.class).getAs(Settings.LAST_OVERNIGHT_UPDATE_DATE, Settings.CONVERT_DATE);
        if (date != null && date.compareTo(today) == 0){
            return null;
        }

        GetStatisticState.StatisticState statisticState = using(Model.class).execute(GetStatisticState.class,
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
