package org.monroe.team.notification.bridge.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: MisterJBee
 * Date: 12/16/13 Time: 1:02 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Notification implements Serializable {

    public final String id;
    public final String owner;
    public final Date mDate;
    public final Map<String,String> body = new HashMap<String, String>();

    public Notification(String id, String owner, Date date) {
        this.id = id;
        this.owner = owner;
        mDate = date;
    }
}
