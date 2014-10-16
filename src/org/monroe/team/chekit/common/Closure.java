package org.monroe.team.chekit.common;

public interface Closure <Out,In> {
    Out call(In in);
}
