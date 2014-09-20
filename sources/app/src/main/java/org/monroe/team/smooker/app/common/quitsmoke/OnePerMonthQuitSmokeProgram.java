package org.monroe.team.smooker.app.common.quitsmoke;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.uc.common.DateUtils;

import java.util.Date;

public class OnePerMonthQuitSmokeProgram extends QuitSmokeProgram{

    protected OnePerMonthQuitSmokeProgram(QuitSmokeDataDriver dataDriver) {
        super(dataDriver);
    }

    @Override
    protected void doInitialize(QuitSmokeData smokeData, int startSmokeCount, int endSmokeCount) {
        int stageCount = startSmokeCount - endSmokeCount + 1;
        int nextStageLimit = startSmokeCount;
        Date nextStageDate = DateUtils.dateOnly(DateUtils.now());
        smokeData.stageList.add(new QuitSmokeData.Stage(nextStageLimit, nextStageDate));
        if (stageCount > 1) {
            for (int i = 1; i < stageCount; i++) {
                nextStageDate = DateUtils.mathMonth(nextStageDate, 1);
                nextStageLimit -= 1;
                smokeData.stageList.add(new QuitSmokeData.Stage(nextStageLimit, nextStageDate));
            }
        }
    }

    @Override
    public int getTodaySmokeCount() {
        QuitSmokeData.Stage stage = getStageForDay(DateUtils.now());
        if (stage == null){
            stage = getStageBeforeDay(DateUtils.now());
        }
        if (stage == null) return -1;
        return stage.smokeLimit;
    }

    @Override
    protected boolean doLogSmokesForDate(QuitSmokeData smokeData, Date date, int smokeCount) {
        QuitSmokeData.Stage stage = getStageForDay(date);
        if (stage != null){
            stage.result = smokeCount > stage.smokeLimit? QuitSmokeData.QuiteStageResult.FAILS: QuitSmokeData.QuiteStageResult.PASS;
            if (stage.result == QuitSmokeData.QuiteStageResult.FAILS) {
                int insertIndex = smokeData.stageList.indexOf(stage) + 1;
                QuitSmokeData.Stage newAttempt = new QuitSmokeData.Stage(stage.smokeLimit, DateUtils.mathDays(stage.date, 1));
                smokeData.stageList.add(insertIndex, newAttempt);
                moveDayForwardFutureStagesStartedFrom(smokeData, insertIndex + 1);
            }
            return true;
        } else {
            final QuitSmokeData.Stage beforeStage = getStageBeforeDay(date);
            if (beforeStage.smokeLimit < smokeCount){
                QuitSmokeData.Stage failedStage = new QuitSmokeData.Stage(beforeStage.smokeLimit,date);
                failedStage.result = QuitSmokeData.QuiteStageResult.FAILS;
                int insertIndex = smokeData.stageList.indexOf(beforeStage)+1;
                smokeData.stageList.add(insertIndex,failedStage);
                moveDayForwardFutureStagesStartedFrom(smokeData, insertIndex+1);
                return true;
            }
        }
        return false;
    }

    private void moveDayForwardFutureStagesStartedFrom(QuitSmokeData data, int startIndex) {
        if (startIndex >= data.stageList.size()) return;
        for (int i = startIndex;i<data.stageList.size();i++){
            data.stageList.get(i).date = DateUtils.mathDays(data.stageList.get(i).date, 1);
        }
    }


}
