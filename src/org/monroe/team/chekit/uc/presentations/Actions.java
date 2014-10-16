package org.monroe.team.chekit.uc.presentations;

import org.monroe.team.chekit.uc.CreateCheckRun;

import java.io.File;

public class Actions {

    public static interface Action {}

    public static interface Application extends Action{
        public static class ChangeScreen implements Application {

            public final Screen screen;

            public ChangeScreen(Screen screen) {
                this.screen = screen;
            }

            @Override
            public String toString() {
                return "action ChangeScreen {" +
                        "screen=" + screen +
                        '}';
            }
        }
        public static class QuitApplication implements Application{
            @Override
            public String toString() {
                return "action QuitApplication";
            }
        }
    }

    public static class Toast implements Action{

        public final String msg;

        public Toast(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "ErrorMessage{" +
                    "msg='" + msg + '\'' +
                    '}';
        }
    }

    public static interface Suite extends Action {

        public static class OpenCheckSuite implements Suite {
            public final File file;

            public OpenCheckSuite(File file) {
                this.file = file;
            }

            @Override
            public String toString() {
                return "OpenCheckSuite{" +
                        "file=" + file +
                        '}';
            }
        }
        public static class RunCheckSuite implements Suite{

            public final String suiteId;

            public RunCheckSuite(String suiteId) {
                this.suiteId = suiteId;
            }
        }
        public static class UpdateRunStatusRequest implements Suite {

            public final StepRunRepresentation.Type status;
            public final String comment;

            public UpdateRunStatusRequest(StepRunRepresentation.Type status, String comment) {
                this.status = status;
                this.comment = comment;
            }
        }
        public class UpdateRunDetails implements Suite {}
        public class UpdateStepComment implements Suite {}

        public class SaveRun implements Suite {

            public final String filePath;

            public SaveRun(String pathToSave) {
                filePath = pathToSave;
            }
        }

        public class OpenRun implements Suite {
            public final File file;
            public OpenRun(File file) {
                this.file = file;
            }
        }

        public class ContinueCheckSuiteRun implements Suite {
            public CreateCheckRun.CheckRunDetails details;
            public ContinueCheckSuiteRun(CreateCheckRun.CheckRunDetails runId) {
                this.details= runId;
            }
        }
    }
}
