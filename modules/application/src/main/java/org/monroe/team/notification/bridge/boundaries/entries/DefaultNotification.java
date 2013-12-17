package org.monroe.team.notification.bridge.boundaries.entries;

import org.monroe.team.notification.bridge.boundaries.NotificationBoundary;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class DefaultNotification implements Serializable, NotificationBoundary.Notification {

    private String mOwnerId;
    private String mId;
    private Map<String,String> mBody;
    private Date mCreationDate;

    public DefaultNotification(String ownerId, String id, Map<String, String> body, Date creationDate) {
        mOwnerId = ownerId;
        mId = id;
        mBody = body;
        mCreationDate = creationDate;
    }

    @Override
    public String getMessageId() {
        return mId;
    }

    @Override
    public String getOwner() {
        return mOwnerId;
    }

    @Override
    public Map<String, String> getBody() {
        return mBody;
    }
}
