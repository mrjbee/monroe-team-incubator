package org.monroe.team.smooker.promo.common.quitsmoke;

import android.content.Context;

import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.DateUtils;

import java.util.Date;

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

    public synchronized QuitSmokeProgram setup(QuitSmokeDifficultLevel level, int startSmokeCount, int endSmokeCount) {
        if (currentInstance != null){
            disable();
        }
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
            case LOWEST: return new OnePerMonthQuitSmokeProgram(dataDriver);
            case LOW: return new OnePerWeekQuiteSmokeProgram(dataDriver);
            case SMART: return new DayLinearQuiteSmokeProgram(dataDriver);
            case SMARTEST: return new DayKLinearQuiteSmokeProgram(dataDriver);
            case HARD: return new OnePerDayQuiteSmokeProgram(dataDriver);
            case HARDEST: return new SinceTomorrowProgram(dataDriver);
        }
        throw new IllegalStateException("Unsupported quit program "+dataDriver.getLevel());
    }

    public void update(Closure<QuitSmokeProgram, Void> updateClosure) {
        QuitSmokeProgram quitSmokeProgram = get();
        if (quitSmokeProgram != null){
            updateClosure.execute(quitSmokeProgram);
        }
    }


    public static class DayKLinearQuiteSmokeProgram extends QuitSmokeProgram{

        protected DayKLinearQuiteSmokeProgram(QuitSmokeDataDriver dataDriver) {
            super(dataDriver);
        }

        /*
        days
        :
        :                           x(12,30)
        :
        :
        :
        :
        :
        :
        : x (1,1)
        .......................................... smoke counts to decrease
        0                                        20
        y = kx
         */
        @Override
        protected void doInitialize(QuitSmokeData smokeData, int startSmokeCount, int endSmokeCount) {

            Date date = DateUtils.dateOnly(DateUtils.now());
            smokeData.stageList.add(new QuitSmokeData.Stage(startSmokeCount, date));
            int stageCount = startSmokeCount - endSmokeCount + 1;
            if (stageCount > 2) {
                float k = (30f-1f)/((float)stageCount);
                for (int decreaseDelta = 1; (startSmokeCount - decreaseDelta) >= endSmokeCount; decreaseDelta++) {
                    date = DateUtils.mathDays(date,Math.round(decreaseDelta*k));
                    smokeData.stageList.add(new QuitSmokeData.Stage(startSmokeCount-decreaseDelta, date));
                }
            } else {
                smokeData.stageList.add(new QuitSmokeData.Stage(startSmokeCount-1, DateUtils.mathDays(date,30)));
            }
        }
    }


    public static class DayLinearQuiteSmokeProgram extends QuitSmokeProgram{

        protected DayLinearQuiteSmokeProgram(QuitSmokeDataDriver dataDriver) {
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
                    nextStageDate = DateUtils.mathDays(nextStageDate, i);
                    nextStageLimit -= 1;
                    smokeData.stageList.add(new QuitSmokeData.Stage(nextStageLimit, nextStageDate));
                }
            }
        }
    }


    public static class OnePerWeekQuiteSmokeProgram extends QuitSmokeProgram{

        protected OnePerWeekQuiteSmokeProgram(QuitSmokeDataDriver dataDriver) {
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
                    nextStageDate = DateUtils.mathWeek(nextStageDate, 1);
                    nextStageLimit -= 1;
                    smokeData.stageList.add(new QuitSmokeData.Stage(nextStageLimit, nextStageDate));
                }
            }
        }
    }

    public static class OnePerDayQuiteSmokeProgram extends QuitSmokeProgram{

        protected OnePerDayQuiteSmokeProgram(QuitSmokeDataDriver dataDriver) {
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
                    nextStageDate = DateUtils.mathDays(nextStageDate, 1);
                    nextStageLimit -= 1;
                    smokeData.stageList.add(new QuitSmokeData.Stage(nextStageLimit, nextStageDate));
                }
            }
        }
    }

    public static class SinceTomorrowProgram extends QuitSmokeProgram{

        protected SinceTomorrowProgram(QuitSmokeDataDriver dataDriver) {
            super(dataDriver);
        }

        @Override
        protected void doInitialize(QuitSmokeData smokeData, int startSmokeCount, int endSmokeCount) {
            Date nextStageDate = DateUtils.dateOnly(DateUtils.now());
            if (startSmokeCount == -1){
                startSmokeCount = 20;
            }
            smokeData.stageList.add(new QuitSmokeData.Stage(startSmokeCount, nextStageDate));
            smokeData.stageList.add(new QuitSmokeData.Stage(0, DateUtils.mathDays(nextStageDate, 1)));
        }
    }
}
