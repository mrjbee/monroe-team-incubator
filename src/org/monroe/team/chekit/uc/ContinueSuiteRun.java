package org.monroe.team.chekit.uc;

import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.uc.entity.check.ActionStep;
import org.monroe.team.chekit.uc.entity.check.Check;
import org.monroe.team.chekit.uc.entity.check.CheckSuite;
import org.monroe.team.chekit.uc.entity.check.Step;
import org.monroe.team.chekit.uc.entity.run.CheckSuiteRun;
import org.monroe.team.chekit.uc.presentations.Actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.util.ArrayList;

public class ContinueSuiteRun extends UseCaseSupport<ContinueSuiteRun.LoadStatus,File> {

    @Override
    public LoadStatus perform(File file) {
        if (!file.exists() || file.isDirectory())  new LoadStatus(null,new Actions.Toast("Bad file"));
        ObjectInputStream stream =null;
        try {
            stream = new ObjectInputStream(new FileInputStream(file));
            CheckSuiteRun run = (CheckSuiteRun) stream.readObject();
            run.saveToFilePath = file.getAbsolutePath();
            run.updateID();
            using(SuitesCache.class).manageRun(run);
            return new LoadStatus(new CreateCheckRun.CheckRunDetails(
                    run.id,
                    run.suite.caption,
                    run.details,
                    DateFormat.getDateTimeInstance().format(run.startDate)),null);
        } catch (Exception e) {
            if (stream != null){
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
            return new LoadStatus(null,new Actions.Toast("Error! Couldn`t open it."));
        }
    }

    public static class LoadStatus{

        public final CreateCheckRun.CheckRunDetails details;
        public final Actions.Toast toast;

        public LoadStatus(CreateCheckRun.CheckRunDetails details, Actions.Toast toast) {
            this.details = details;
            this.toast = toast;
        }
    }

}
