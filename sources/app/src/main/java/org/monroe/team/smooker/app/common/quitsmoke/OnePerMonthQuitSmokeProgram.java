package org.monroe.team.smooker.app.common.quitsmoke;

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


}
