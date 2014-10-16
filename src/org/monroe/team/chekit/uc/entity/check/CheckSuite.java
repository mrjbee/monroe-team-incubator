package org.monroe.team.chekit.uc.entity.check;

import java.io.File;
import java.io.Serializable;

public class CheckSuite implements Serializable {

    public final String id;
    public final String caption;
    public ActionStep rootActionStep;
    public final String filePath;

    public int idGenerator = 0;

    public CheckSuite(String id, String caption, String file) {
        this.id = id;
        this.caption = caption;
        this.filePath = file;
    }

    public String generateId() {
        idGenerator++;
        return id+":"+idGenerator;
    }

    public Step findById(String id) {
       return findByIdStartingWith(id, rootActionStep);
    }

    public Step findByIdStartingWith(String id, Step step) {
        if (step.id.equals(id)){
            return step;
        } 
        if (step instanceof ActionStep){
            for (Step sonStep : ((ActionStep) step).stepList) {
                Step answer = findByIdStartingWith(id, sonStep);
                if (answer != null) return answer;
            }
        }
        return null;
    }
}
