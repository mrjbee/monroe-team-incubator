package org.monroe.team.smooker.app.uc.common;

public class FetchFailedException extends RuntimeException {

    public final String error_code;

    public FetchFailedException(String error_code, Throwable throwable) {
        super(throwable);
        this.error_code = error_code;
    }
}
