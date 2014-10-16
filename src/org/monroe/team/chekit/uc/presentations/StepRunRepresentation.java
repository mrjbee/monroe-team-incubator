package org.monroe.team.chekit.uc.presentations;

import org.monroe.team.chekit.uc.entity.run.StepRun;

import java.util.ArrayList;
import java.util.List;

public class StepRunRepresentation {

    public final static Type[] CHECK_TYPES = {Type.CHECK_PASSED,Type.CHECK_SKIPPED,Type.CHECK_AWAITING,Type.CHECK_FAILED};

    public final String id;
    public final String title;
    public String comment;
    public Type type;
    public final List<StepRunRepresentation> subStepList = new ArrayList<>();

    public StepRunRepresentation(String id, String title, String comment, Type type) {
        this.id = id;
        this.title = title;
        this.comment = comment;
        this.type = type;
    }

    @Override
    public String toString() {
        return "StepRunRepresentation{" +
                "title='" + title + '\'' +
                ", type=" + type +
                '}';
    }

    public static enum Type {
        ACTION("action"), CHECK_AWAITING("Awaiting"), CHECK_PASSED("Passed"), CHECK_FAILED("Failed"), CHECK_SKIPPED("Skipped");

        private final String humanString;

        Type(String humanString) {
            this.humanString = humanString;
        }

        public static Type fromNative(StepRun.StepStatus status) {
            switch (status){
                case PASSED: return CHECK_PASSED;
                case SKIPPED: return CHECK_SKIPPED;
                case FAILED: return CHECK_FAILED;
                case AWAITING: return CHECK_AWAITING;
            }
            throw new RuntimeException();
        }

        public String toHumanString() {
            return humanString;
        }

        public static StepRun.StepStatus toNative(Type status) {

            switch (status){
                case CHECK_PASSED:return StepRun.StepStatus.PASSED;
                case CHECK_SKIPPED: return StepRun.StepStatus.SKIPPED;
                case CHECK_FAILED: return StepRun.StepStatus.FAILED;
                case CHECK_AWAITING: return StepRun.StepStatus.AWAITING;
            }

            throw new RuntimeException();
        }
    }
}
