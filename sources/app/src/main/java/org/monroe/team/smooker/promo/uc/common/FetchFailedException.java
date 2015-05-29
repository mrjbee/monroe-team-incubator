package org.monroe.team.smooker.promo.uc.common;

public class FetchFailedException extends RuntimeException {

    public final String error_code;

    public FetchFailedException(String error_code, Throwable throwable) {
        super("Fetch failed with "+error_code, throwable);
        this.error_code = error_code;
    }
}
