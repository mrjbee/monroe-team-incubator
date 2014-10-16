package org.monroe.team.chekit.uc;

public interface UseCase <Response,Request> {
    public Response perform(Request request);
}
