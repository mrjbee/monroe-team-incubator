package org.monroe.team.smooker.app.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.monroe.team.smooker.app.common.Closure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Event<DataType>{

    private static final Map<Object, List<BroadcastReceiver>> registerMap = new HashMap<Object, List<BroadcastReceiver>>();

    public static <DataT> void  send(Context context, Event<DataT> event, DataT data){
        Intent intent = new Intent(event.getAction());
        event.putValue(intent, data);
        context.sendBroadcast(intent);
    }

    public static <DataT> void subscribeOnEvent(Context context, Object owner, final Event<DataT> event, final Closure<DataT, Void> onEvent){
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onEvent.execute(event.extractValue(intent));
            }
        };

        context.registerReceiver(receiver,new IntentFilter(event.getAction()));
        register(owner, receiver);
    }

    public static <DataT> void unSubscribeFromEvents(Context context, Object owner){
        if (registerMap.get(owner) != null){
            for (BroadcastReceiver receiver : registerMap.get(owner)) {
                context.unregisterReceiver(receiver);
            }
        }
    }

    private static void register(Object owner, BroadcastReceiver receiver) {
        if (registerMap.get(owner) == null){
            registerMap.put(owner, new ArrayList<BroadcastReceiver>(2));
        }
        registerMap.get(owner).add(receiver);
    }

    protected abstract DataType extractValue(Intent intent);
    protected abstract void putValue(Intent intent, DataType data);
    public abstract String getAction();

}