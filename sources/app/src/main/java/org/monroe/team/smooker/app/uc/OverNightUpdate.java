package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.common.EventMessenger;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

import java.util.Date;

public class OverNightUpdate extends TransactionUserCase<Void,Void> {

    public OverNightUpdate(Registry registry) {
        super(registry);
    }

    @Override
    protected Void transactionalExecute(Void request, final DAO dao) {
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

        return null;
    }
}
