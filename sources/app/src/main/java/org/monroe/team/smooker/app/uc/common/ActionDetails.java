package org.monroe.team.smooker.app.uc.common;

import java.util.Date;

public class ActionDetails {

    public final long id;
    public final Date date;
    public final Type type;

    public ActionDetails(long id, Date date, Type type) {
        this.id = id;
        this.date = date;
        this.type = type;
    }

    public static enum Type{
        SMOKE_BREAK, SMOKE_CANCEL_SKIP, SMOKE_CANCEL_POSTPONED
    }
}
