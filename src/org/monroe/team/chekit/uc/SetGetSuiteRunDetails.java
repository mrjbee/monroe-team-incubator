package org.monroe.team.chekit.uc;

import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.uc.entity.run.CheckSuiteRun;

public class SetGetSuiteRunDetails extends UseCaseSupport<String, SetGetSuiteRunDetails.UpdateDetailsRequest> {

    @Override
    public String perform(UpdateDetailsRequest updateDetailsRequest) {
        CheckSuiteRun suiteRun = using(SuitesCache.class).getRun(updateDetailsRequest.runId);
        if (updateDetailsRequest.details != null){
            suiteRun.details = updateDetailsRequest.details;
        }
        return suiteRun.details;
    }

    public static class UpdateDetailsRequest {

        public final String runId;
        public final String details;

        public UpdateDetailsRequest(String runId, String details) {
            this.runId = runId;
            this.details = details;
        }
    }
}
