package org.monroe.team.smooker.promo.common.constant;

import org.monroe.team.android.box.event.GenericEvent;
import org.monroe.team.smooker.promo.uc.underreview.CalculateTodaySmokeSchedule;

import java.io.Serializable;
import java.util.ArrayList;

public final class Events {

    private Events() {}

    public static final GenericEvent<ArrayList<CalculateTodaySmokeSchedule.SmokeSuggestion>> SMOKE_SCHEDULE_CHANGED =
            new GenericEvent<ArrayList<CalculateTodaySmokeSchedule.SmokeSuggestion>>("SMOKE_SCHEDULE_CHANGED");

    public static final GenericEvent<SmokeCancelReason> SMOKE_CANCELED = new GenericEvent<SmokeCancelReason>("SMOKE_CANCELED");
    public static final GenericEvent<Integer> SMOKE_COUNT_CHANGED = new GenericEvent<Integer>("SMOKE_COUNT_CHANGED");
    public static final GenericEvent<Boolean> QUIT_SCHEDULE_UPDATED = new GenericEvent<Boolean>("QUIT_SCHEDULE_UPDATED");
    public static final GenericEvent<Boolean> QUIT_SCHEDULE_REFRESH = new GenericEvent<Boolean>("QUIT_SCHEDULE_REFRESH");
    public static final GenericEvent<Serializable> WARNING = new GenericEvent<>("WARNING");

}
