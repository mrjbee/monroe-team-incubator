package org.monroe.team.chekit.uc;

import org.monroe.team.chekit.services.CheckSuitParser;
import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.uc.presentations.Actions;
import org.monroe.team.chekit.uc.entity.check.ActionStep;
import org.monroe.team.chekit.uc.entity.check.Check;
import org.monroe.team.chekit.uc.entity.check.CheckSuite;
import org.monroe.team.chekit.uc.entity.check.Step;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LoadCheckSuite extends UseCaseSupport<LoadCheckSuite.LoadStatus,File> {

    @Override
    public LoadStatus perform(File file) {
        if (!file.exists() || file.isDirectory())  new LoadStatus("",new Actions.Toast("Bad file"));

        CheckSuitParser parser = new CheckSuitParser();
        CheckSuite suite = null;
        try {
            suite = parser.parse(file);
        } catch (IOException e) {
            e.printStackTrace();
            return new LoadStatus("",new Actions.Toast("Bad file"));
        } catch (CheckSuitParser.FormatException e) {
            e.printStackTrace();
            return new LoadStatus("",new Actions.Toast(e.getMessage()));
        }
        using(SuitesCache.class).manageSuit(suite);
        return new LoadStatus(suite.id,null);
    }

    private ActionStep action(Step step){
        return (ActionStep) step;
    }

    public static class LoadStatus{

        public final String checkSuiteId;
        public final Actions.Toast toast;

        public LoadStatus(String checkSuiteId, Actions.Toast toast) {
            this.checkSuiteId = checkSuiteId;
            this.toast = toast;
        }
    }

}
