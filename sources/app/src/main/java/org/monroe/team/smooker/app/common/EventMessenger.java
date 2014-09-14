package org.monroe.team.smooker.app.common;

import android.content.Context;

import org.monroe.team.smooker.app.event.Event;

public class EventMessenger {

    private final Context context;

    public EventMessenger(Context context) {
        this.context = context;
    }

    public <Data> void send(Event<Data> event, Data data){
        Event.send(context, event, data);
    }
}
