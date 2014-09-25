package org.monroe.team.smooker.app.common.quitsmoke;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.uc.common.DateUtils;

import java.util.Date;
import java.util.List;

public abstract class QuitSmokeProgram {

    protected final QuitSmokeDataDriver dataDriver;

    protected QuitSmokeProgram(QuitSmokeDataDriver dataDriver) {
        this.dataDriver = dataDriver;
    }

    final public void initialize(final int startSmokeCount, final int endSmokeCount){
        dataDriver.updateData(new Closure<QuitSmokeData,Void> (){
            @Override
            public Void execute(QuitSmokeData smokeData) {
                doInitialize(smokeData, startSmokeCount, endSmokeCount);
                return null;
            }
        });
    }

    protected abstract void doInitialize(QuitSmokeData smokeData, int startSmokeCount, int endSmokeCount);

    public int getTodaySmokeCount() {
        QuitSmokeData.Stage stage = getStageForDay(DateUtils.dateOnly(DateUtils.now()));
        if (stage == null){
            stage = getStageBeforeDay(DateUtils.now());
        }
        if (stage == null) return -1;
        return stage.smokeLimit;
    }


    protected QuitSmokeData.Stage getStageBeforeDay(Date date) {
        for (int i =dataDriver.getData().stageList.size()-1; i>-1; i--){
            if (dataDriver.getData().stageList.get(i).date.compareTo(date) < 0)
                return dataDriver.getData().stageList.get(i);
        }
        return null;
    }

    protected QuitSmokeData.Stage getStageForDay(Date date) {
        for (QuitSmokeData.Stage stage : dataDriver.getData().stageList) {
            if (stage.date.compareTo(date) == 0) return stage;
        }
        return null;
    }

    public QuitSmokeDifficultLevel getLevel() {
        return dataDriver.getLevel();
    }

    public List<QuitSmokeData.Stage> getStages() {
        return dataDriver.getData().stageList;
    }

    final public boolean doLogSmokesForDate(final Date date, final int smokeCount){

        if (date.compareTo(getStartDate()) < 0) return false;

        return dataDriver.updateData(new Closure<QuitSmokeData, Boolean>() {
            @Override
            public Boolean execute(QuitSmokeData smokeData) {
                boolean changed = doLogSmokesForDate(smokeData, date ,smokeCount);
                smokeData.lastLoggedDate = date;
                return changed;
            }
        });
    }

    public Date getStartDate() {
        return getStages().get(0).date;
    }

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


    public Date getLastLoggedDate() {
        return dataDriver.getData().lastLoggedDate;
    }

    public boolean isChangedToday() {
        return getStageForDay(DateUtils.dateOnly(DateUtils.now())) != null;
    }
}
