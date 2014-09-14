package org.monroe.team.smooker.app.common;

import org.monroe.team.smooker.app.event.GenericEvent;

public final class Events {
    private Events() {}
    public static final GenericEvent<Integer> ADD_SMOKE = new GenericEvent<Integer>("ADD_SMOKE");
}
