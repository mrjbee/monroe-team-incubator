package org.monroe.team.smooker.promo.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.promo.common.SmookerModel;
import org.monroe.team.smooker.promo.common.constant.Settings;
import org.monroe.team.smooker.promo.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.promo.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.promo.db.Dao;

import java.util.Date;

public class OverNightUpdate extends TransactionUserCase<Void,Void,Dao> {

    public OverNightUpdate(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Void request, final Dao dao) {
        Date today = DateUtils.dateOnly(DateUtils.now());
        Date date = using(SmookerModel.class).usingService(SettingManager.class).getAs(Settings.LAST_OVERNIGHT_UPDATE_DATE, Settings.CONVERT_DATE);
        if (date != null && date.compareTo(today) == 0){
            return null;
        }
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
                return null;
            }
        });
        using(SettingManager.class).set(Settings.LAST_OVERNIGHT_UPDATE_DATE, today.getTime());
        return null;
    }
}
