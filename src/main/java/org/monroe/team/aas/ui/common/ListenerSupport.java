package org.monroe.team.aas.ui.common;

import org.monroe.team.aas.ui.common.command.ArgumentLessCommand;
import org.monroe.team.aas.ui.common.command.ResultLessCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 8/15/13 Time: 11:45 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ListenerSupport <ListenerType> {

    private List<ListenerType> mListenerList = new ArrayList<ListenerType>(3);

    public void fire(ResultLessCommand<ListenerType> fireCommand){
         for (int i = mListenerList.size()-1; i>=0; i--){
             fireCommand.callAndResult(mListenerList.get(i));
         }
    }

    public void fireIf(ArgumentLessCommand<Boolean> condition, ResultLessCommand<ListenerType> fireCommand){
        if (condition.callAndResult(null))
            fire(fireCommand);
    }

    public void add(ListenerType listener){
        mListenerList.add(listener);
    }

    public void remove(ListenerType listener){
        mListenerList.remove(listener);
    }

    public void removeAll() {
        mListenerList.clear();
    }
}
