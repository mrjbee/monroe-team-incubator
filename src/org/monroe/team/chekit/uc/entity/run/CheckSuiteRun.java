package org.monroe.team.chekit.uc.entity.run;

import org.monroe.team.chekit.common.DateUtils;
import org.monroe.team.chekit.uc.entity.check.CheckSuite;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CheckSuiteRun implements Serializable{

    public String id;

    public final Date startDate;
    public final CheckSuite suite;
    public String details;
    public String saveToFilePath;

    public final Map<String, StepRun> stepRunMap = new HashMap<>();

    public CheckSuiteRun(Date startDate, CheckSuite suite) {
        this.startDate = startDate;
        this.suite = suite;
    }

    public void updateID(){
        this.id = suite.id + ":run:"+ DateUtils.now().getTime();
    }
}
