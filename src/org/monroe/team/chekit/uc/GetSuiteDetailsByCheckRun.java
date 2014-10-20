package org.monroe.team.chekit.uc;

import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.uc.entity.check.CheckSuite;

import java.io.File;

public class GetSuiteDetailsByCheckRun extends UseCaseSupport<GetSuiteDetailsByCheckRun.CheckSuiteDetails, String> {


    @Override
    public CheckSuiteDetails perform(String id) {
        CheckSuite checkSuite = using(SuitesCache.class).getRun(id).suite;
        return new CheckSuiteDetails(new File(checkSuite.filePath));
    }

    public static class CheckSuiteDetails {

        public final File suiteFile;

        public CheckSuiteDetails(File suiteFile) {
            this.suiteFile = suiteFile;
        }
    }
}
