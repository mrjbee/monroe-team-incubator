package org.monroe.team.smooker.app.common.quitsmoke;

import android.content.Context;

public class QuitSmokeProgramManager {

    private final Context context;
    private final QuitSmokeDataManager dataManager;
    private QuitSmokeProgram currentInstance;

    public QuitSmokeProgramManager(Context context) {
        this.context = context;
        dataManager = new QuitSmokeDataManager(context);
    }

    public synchronized void disable() {
        currentInstance = null;
        if (dataManager.exists()){
            if (!dataManager.delete()){
                throw new RuntimeException("Couldn`t disable schedule");
            }
        }
    }

    public synchronized QuitSmokeProgram setup(QuitSmokeStrategyLevel level, int startSmokeCount, int endSmokeCount) {
        if (currentInstance != null) throw new IllegalStateException("There are running quit smoke program");
        QuitSmokeDataDriver dataDriver = QuitSmokeDataDriver.createEmpty(dataManager, level);
        currentInstance = instanceBy(dataDriver);
        currentInstance.initialize(startSmokeCount, endSmokeCount);
        return currentInstance;
    }

    private QuitSmokeProgram restore() {
        QuitSmokeDataDriver dataDriver = QuitSmokeDataDriver.create(dataManager);
        QuitSmokeProgram answer = instanceBy(dataDriver);
        return answer;
    }

    public synchronized QuitSmokeProgram get(){
        if (currentInstance == null && dataManager.exists()){
            currentInstance = restore();
        }
        return currentInstance;
    }


    private QuitSmokeProgram instanceBy(QuitSmokeDataDriver dataDriver) {
        switch (dataDriver.getLevel()){
            case LOWEST: return new DemoQuitSmokeProgram(dataDriver);
            case LOW: return new DemoQuitSmokeProgram(dataDriver);
            case SMART: return new DemoQuitSmokeProgram(dataDriver);
            case SMARTEST: return new DemoQuitSmokeProgram(dataDriver);
            case HARD: return new DemoQuitSmokeProgram(dataDriver);
            case HARDEST: return new DemoQuitSmokeProgram(dataDriver);
        }
        throw new IllegalStateException("Unsupported quit program "+dataDriver.getLevel());
    }

}
