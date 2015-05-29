package org.monroe.team.smooker.promo.common.constant;

import java.io.Serializable;

public enum SmokeCancelReason implements Serializable{
    POSTPONE(1), SKIP(0);

    public final int id;

    SmokeCancelReason(int id) {
        this.id = id;
    }

    public static SmokeCancelReason byId(int id) {
        switch (id){
            case 0: return SKIP;
            case 1: return POSTPONE;
        }
        throw new IllegalStateException();
    }
}
