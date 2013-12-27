package org.monroe.team.notification.bridge.android.delivery.socket;

import java.io.Serializable;

/**
 * User: MisterJBee
 * Date: 12/28/13 Time: 12:09 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface IdAware extends Serializable {

    String getId();

    public static class IdAwareSocketMessage implements SocketMessage<IdAware> {

        private final IdAware mIdAware;

        public IdAwareSocketMessage(IdAware idAware) {
            mIdAware = idAware;
        }

        @Override
        public String getId() {
            return mIdAware.getId();
        }

        @Override
        public IdAware getBody() {
            return mIdAware;
        }
    }
}
