package org.monroe.team.smooker.app.common.quitsmoke;

import org.monroe.team.smooker.app.common.Closure;

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

    public int getTodaySmokeCount() {
        return 12;
    }
}
