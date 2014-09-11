package org.monroe.team.smooker.app.common;

public interface Closure <In,Out> {
    public Out execute(In arg);
}
