package org.monroe.team.smooker.app.uc.common;


public interface UserCase <RequestType,ResponseType> {
    ResponseType execute(RequestType request);
}
