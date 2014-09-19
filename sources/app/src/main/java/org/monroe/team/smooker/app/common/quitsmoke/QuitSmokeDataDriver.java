package org.monroe.team.smooker.app.common.quitsmoke;

import org.monroe.team.smooker.app.common.Closure;

public class QuitSmokeDataDriver {

    private final QuitSmokeDataManager dataManager;
    private final QuitSmokeData quitSmokeData;

    private QuitSmokeDataDriver(QuitSmokeDataManager dataManager, QuitSmokeData quitSmokeData) {
        this.dataManager = dataManager;
        this.quitSmokeData = quitSmokeData;
    }

    public static QuitSmokeDataDriver create(QuitSmokeDataManager dataManager){
        QuitSmokeData quitSmokeData = dataManager.restore();
        QuitSmokeDataDriver answer = new QuitSmokeDataDriver(dataManager,quitSmokeData);
        return answer;
    }

    public static QuitSmokeDataDriver createEmpty(QuitSmokeDataManager dataManager, QuitSmokeStrategyLevel level) {
        QuitSmokeData quitSmokeData = new QuitSmokeData(level);
        return new QuitSmokeDataDriver(dataManager,quitSmokeData);
    }

    public QuitSmokeStrategyLevel getLevel() {
        return quitSmokeData.level;
    }

    public void updateData(Closure<QuitSmokeData, Void> updateAction) {
        updateAction.execute(quitSmokeData);
        dataManager.persist(quitSmokeData);
    }
}
