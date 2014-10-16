package org.monroe.team.chekit.services;

import org.monroe.team.chekit.uc.entity.check.CheckSuite;
import org.monroe.team.chekit.uc.entity.run.CheckSuiteRun;

import java.util.HashMap;
import java.util.Map;

public class SuitesCache{

    private final Map<String,CheckSuite> suitePerIdMap = new HashMap<>();

    private final Map<String,CheckSuiteRun> runPerIdMap = new HashMap<>();

    public void manageSuit(CheckSuite suite) {
        suitePerIdMap.put(suite.id,suite);
    }

    public CheckSuite load(String suiteId) {
        return suitePerIdMap.get(suiteId);
    }

    public void manageRun(CheckSuiteRun suiteRun) {
        runPerIdMap.put(suiteRun.id, suiteRun);
    }

    public CheckSuiteRun getRun(String runId) {
        return runPerIdMap.get(runId);
    }
}
