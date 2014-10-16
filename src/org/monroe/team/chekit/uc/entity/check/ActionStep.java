package org.monroe.team.chekit.uc.entity.check;

import java.util.List;

public class ActionStep extends Step{

    public final List<Step> stepList;

    public ActionStep(String id, String caption, List<Step> stepList) {
        super(id, caption);
        this.stepList = stepList;
    }
}
