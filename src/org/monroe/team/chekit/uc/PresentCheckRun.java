package org.monroe.team.chekit.uc;

import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.uc.entity.check.ActionStep;
import org.monroe.team.chekit.uc.entity.check.CheckSuite;
import org.monroe.team.chekit.uc.entity.check.Step;
import org.monroe.team.chekit.uc.entity.run.CheckSuiteRun;
import org.monroe.team.chekit.uc.entity.run.StepRun;
import org.monroe.team.chekit.uc.presentations.StepRunRepresentation;

public class PresentCheckRun extends UseCaseSupport<StepRunRepresentation, String> {
    @Override
    public StepRunRepresentation perform(String runId) {
        CheckSuiteRun run = using(SuitesCache.class).getRun(runId);
        CheckSuite suite = run.suite;
        return traversOverAndConvert(suite.rootActionStep,run);
    }

    public StepRunRepresentation traversOverAndConvert(Step head, CheckSuiteRun run){
        StepRunRepresentation stepRunRepresentation = null;
        if (head instanceof ActionStep){
            stepRunRepresentation = new StepRunRepresentation(head.id,head.caption, "", StepRunRepresentation.Type.ACTION);
            ActionStep actionStep = (ActionStep) head;
            for (Step step : actionStep.stepList) {
                StepRunRepresentation son = traversOverAndConvert(step, run);
                stepRunRepresentation.subStepList.add(son);
            }
        } else {
            StepRunRepresentation.Type type = StepRunRepresentation.Type.CHECK_AWAITING;
            String comment = "";
            if (run.stepRunMap.get(head.id) != null){
                //alredy got details
                StepRun runDetails = run.stepRunMap.get(head.id);
                type = StepRunRepresentation.Type.fromNative(runDetails.status);
                comment = runDetails.comment;
            }
            stepRunRepresentation = new StepRunRepresentation(head.id, head.caption, comment, type);
        }
        return stepRunRepresentation;
    }
}
