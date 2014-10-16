package org.monroe.team.chekit.uc;

import org.monroe.team.chekit.common.DateUtils;
import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.uc.entity.check.CheckSuite;
import org.monroe.team.chekit.uc.entity.run.CheckSuiteRun;

import java.text.DateFormat;

public class CreateCheckRun extends UseCaseSupport<CreateCheckRun.CheckRunDetails, String> {
    @Override
    public CheckRunDetails perform(String suiteId) {
        CheckSuite suite = using(SuitesCache.class).load(suiteId);
        CheckSuiteRun suiteRun = new CheckSuiteRun(DateUtils.now(),suite);
        using(SuitesCache.class).manageRun(suiteRun);
        return new CheckRunDetails(
                suiteRun.id,
                suiteRun.suite.caption,
                suiteRun.details,
                DateFormat.getDateTimeInstance().format(suiteRun.startDate));
    }

    public static class CheckRunDetails{

        public final String id;
        public final String name;
        public final String details;
        public final String date;

        public CheckRunDetails(String id, String name, String details, String date) {
            this.id = id;
            this.name = name;
            this.details = details;
            this.date = date;
        }
    }

}
