package org.monroe.team.smooker.app.uc;


import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeData;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgram;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.DateUtils;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

import java.util.Date;

public class CheckDateAndAdjustData extends TransactionUserCase<Void,Void>{

    public CheckDateAndAdjustData(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Void request, DAO dao) {
        final Date now = DateUtils.now();
        final Date today = DateUtils.dateOnly(now);
        //check startup date
        Date firstStart = new Date(using(SettingManager.class).get(Settings.APP_FIRST_TIME_DATE));
        if (now.compareTo(firstStart) < 0){
            using(SettingManager.class).set(Settings.APP_FIRST_TIME_DATE,now.getTime());
        }

        //remove all smokes which grater then today
        dao.removeSmokesAfter(now);
        dao.removeSmokesCancellationAfter(now);

        Date adjustQuitProgramDate =  using(SettingManager.class).has(Settings.QUIT_SMOKE_VALIDATION_DATE)?
                new Date(using(SettingManager.class).get(Settings.QUIT_SMOKE_VALIDATION_DATE)):null;

        if (adjustQuitProgramDate == null || adjustQuitProgramDate.compareTo(today) != 0){
            using(SettingManager.class).set(Settings.QUIT_SMOKE_VALIDATION_DATE,today.getTime());
            final QuitSmokeProgram program = using(QuitSmokeProgramManager.class).get();
            if (program == null) return null;
            if (program.getStartDate().compareTo(today) > 0){
                using(QuitSmokeProgramManager.class).disable();
            }else {
                program.manualUpdate(new Closure<QuitSmokeData, Void>() {
                    @Override
                    public Void execute(QuitSmokeData arg) {
                        for (QuitSmokeData.Stage stage : arg.stageList) {
                            if (stage.date.compareTo(today) >= 0){
                                stage.result = QuitSmokeData.QuiteStageResult.IN_FUTURE;
                            }
                        }
                        return null;
                    }
                });
            }
        }

        return null;
    }

}
