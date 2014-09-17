package org.monroe.team.smooker.app.common;

public class QuitSmokeStrategy {

    private final QuitSmokeDataManager dataManager;

    public QuitSmokeStrategy(QuitSmokeDataManager dataManager) {
        this.dataManager = dataManager;
    }

    protected synchronized void update(Closure<QuitSmokeDataManager.QuitSmokeScheduleData, Void> update) {
        dataManager.updateSchedule(update);

    }

    public synchronized QuitSmokeDataManager.QuitSmokeScheduleData data(){
        return dataManager.getSchedule();
    }

}
