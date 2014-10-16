package org.monroe.team.chekit.uc.entity.check;

import java.io.Serializable;

public abstract class Step implements Serializable {

    public final String id;
    public final String caption;

    public Step(String id, String caption) {
        this.id = id;
        this.caption = caption;
    }
}
