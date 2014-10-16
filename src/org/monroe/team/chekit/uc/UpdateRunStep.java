package org.monroe.team.chekit.uc;

import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.uc.entity.check.ActionStep;
import org.monroe.team.chekit.uc.entity.check.Step;
import org.monroe.team.chekit.uc.entity.run.CheckSuiteRun;
import org.monroe.team.chekit.uc.entity.run.StepRun;
import org.monroe.team.chekit.uc.presentations.StepRunRepresentation;

public class UpdateRunStep extends UseCaseSupport <UpdateRunStep.UpdateResult, UpdateRunStep.UpdateRequest> {

    @Override
    public UpdateResult perform(UpdateRequest updateRequest) {

        CheckSuiteRun run = using(SuitesCache.class).getRun(updateRequest.runSuitId);
        Step step = run.suite.findById(updateRequest.runId);
        if (step instanceof ActionStep) return new UpdateResult(Result.ACTION_COULD_BE_UPDATED,updateRequest,null);
        if (updateRequest.status != StepRunRepresentation.Type.CHECK_PASSED
                && (updateRequest.comment == null || updateRequest.comment.trim().isEmpty())){
            //request a comment
            return new UpdateResult(Result.COMMENT_REQUIRED, updateRequest, null);
        }
        StepRun stepRun = run.stepRunMap.get(updateRequest.runId);
        if (stepRun == null){
            stepRun = new StepRun(step);
            run.stepRunMap.put(stepRun.step.id,stepRun);
        }
        stepRun.comment = updateRequest.comment;
        stepRun.updateState(StepRunRepresentation.Type.toNative(updateRequest.status));

        return new UpdateResult(Result.DONE,updateRequest,
                new StepRunRepresentation(
                    stepRun.step.id,
                    stepRun.step.caption,
                    stepRun.comment,
                    updateRequest.status));
    }

    public  static class UpdateRequest {

        public final String runSuitId;
        public final String runId;
        public final StepRunRepresentation.Type status;
        public final String comment;

        public UpdateRequest(String runSuitId, String runId, StepRunRepresentation.Type status, String comment) {
            this.runSuitId = runSuitId;
            this.runId = runId;
            this.status = status;
            this.comment = comment;
        }
    }

    public static class UpdateResult {
        public final Result result;
        public final UpdateRequest request;
        public final StepRunRepresentation representation;

        public UpdateResult(Result result, UpdateRequest request, StepRunRepresentation representation) {
            this.result = result;
            this.request = request;
            this.representation = representation;
        }
    }

    public static enum Result {
        DONE, COMMENT_REQUIRED, ACTION_COULD_BE_UPDATED
    }

}
