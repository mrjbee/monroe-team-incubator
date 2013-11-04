package org.monroe.team.libdroid.commons;

import java.util.ArrayList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 11/5/13 Time: 12:09 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ListenerSupport <ListenerType> {

    private final List<ListenerType> mList = new ArrayList<ListenerType>(3);

    public void add(ListenerType listener){
        mList.add(listener);
    }

    public boolean remove(ListenerType listener){
        return mList.remove(listener);
    }

    public void notify(Closure<Void, ListenerType> notifyCommand){
        for (int i = mList.size() - 1; i > -1; i--){
            notifyCommand.call(mList.get(i));
        }
    }
}
