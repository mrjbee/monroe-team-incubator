package org.monroe.team.smooker.app.common;

import org.monroe.team.smooker.app.event.GenericEvent;

public final class Events {
    private Events() {}
    public static final GenericEvent<Integer> SMOKE_COUNT_CHANGED = new GenericEvent<Integer>("SMOKE_COUNT_CHANGED");
    public static final GenericEvent<Boolean> QUIT_SCHEDULE_UPDATED = new GenericEvent<Boolean>("QUIT_SCHEDULE_UPDATED");
    public static final GenericEvent<Boolean> QUIT_SCHEDULE_REFRESH = new GenericEvent<Boolean>("QUIT_SCHEDULE_REFRESH");
}
