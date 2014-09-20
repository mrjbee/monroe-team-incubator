package org.monroe.team.smooker.app.common.quitsmoke;

import org.monroe.team.smooker.app.common.Closure;

import java.util.Date;
import java.util.List;

public abstract class QuitSmokeProgram {

    private final QuitSmokeDataDriver dataDriver;

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

    public abstract int getTodaySmokeCount();

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
}
