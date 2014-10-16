package org.monroe.team.chekit.uc;

import com.sun.corba.se.impl.orbutil.ObjectWriter;
import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.uc.entity.run.CheckSuiteRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SaveCheckRun extends UseCaseSupport<SaveCheckRun.SaveStatus,SaveCheckRun.SaveRequest>{


    @Override
    public SaveStatus perform(SaveRequest saveRequest) {
        CheckSuiteRun run = using(SuitesCache.class).getRun(saveRequest.id);
        String filePath = run.saveToFilePath;

        if (saveRequest.filePath != null){
            filePath = saveRequest.filePath;
        }

        if (filePath == null){
            return new SaveStatus(ResponseCode.NO_FILE, "");
        }

        if (new File(filePath).isDirectory()){
            return new SaveStatus(ResponseCode.NO_FILE, "");
        }

        if (!filePath.endsWith(".runit")){
            filePath+=".runit";
        }

        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(filePath));
            objectOutputStream.writeObject(run);
            objectOutputStream.flush();
            objectOutputStream.close();
            run.saveToFilePath = filePath;
            return new SaveStatus(ResponseCode.DONE, filePath);
        } catch (IOException e) {
            e.printStackTrace();
            if(objectOutputStream != null){
                try {
                    objectOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return new SaveStatus(ResponseCode.ERROR,filePath);
    }

    public static class SaveRequest{
        private final String id;
        private final String filePath;

        public SaveRequest(String id, String filePath) {
            this.id = id;
            this.filePath = filePath;
        }
    }

    public static class SaveStatus{

        public final ResponseCode responseCode;
        public final String filePath;

        public SaveStatus(ResponseCode responseCode, String filePath) {
            this.responseCode = responseCode;
            this.filePath = filePath;
        }
    }

    public static enum ResponseCode{
        DONE, NO_FILE, ERROR
    }
}
