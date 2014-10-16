package org.monroe.team.chekit.uc.entity.run;

import org.monroe.team.chekit.common.DateUtils;
import org.monroe.team.chekit.uc.entity.check.Step;

import java.io.Serializable;
import java.util.Date;

public class StepRun implements Serializable {

    public final Step step;
    public StepStatus status = StepStatus.AWAITING;
    public Date updateDate;
    public String comment = "";

    public StepRun(Step step) {
        this.step = step;
        updateDate = DateUtils.now();
    }

    public void updateState(StepStatus stepStatus) {
        status = stepStatus;
        updateDate = DateUtils.now();
    }

    public static enum StepStatus{
        AWAITING, SKIPPED, PASSED, FAILED
    }
}
