package org.monroe.team.smooker.app.common;

public enum SmokeQuitProgramDifficult {

    HARDEST , //once and forever
    HARD , // one cigarette per day
    SMART, // one cigarette per day - one cigarette per month
    SMARTEST, // one cigarette per day - one cigarette per month
    LOW , // one cigarette per week
    LOWEST,  // one cigarette per month
    DISABLED;

    private final static  SmokeQuitProgramDifficult[] LEVELS = {DISABLED, LOWEST,LOW, SMARTEST,HARD,HARDEST};

    public static int difficultCount(){return LEVELS.length;}

    public static SmokeQuitProgramDifficult levelByIndex(int index){
        return LEVELS[index];
    }

    public static int indexByLevel(SmokeQuitProgramDifficult level){
        for (int i = 0; i < LEVELS.length; i++) {
            if (LEVELS[i] == level) return i;
        }
        throw new IllegalStateException();
    }

    public int toIndex() {
        return indexByLevel(this);
    }

    public boolean mayHaveDifferentTargetCount() {
        return this != DISABLED && this != HARDEST;
    }
}
