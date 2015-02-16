package org.monroe.team.smooker.app.common;

import android.app.Activity;
import android.view.View;

import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.smooker.app.android.SmookerApplication;
import org.monroe.team.android.box.event.Event;

public class SupportActivity extends Activity {

    final protected Model model(){
       return ((SmookerApplication)getApplication()).getModel();
    }

    final protected <ViewType extends View> ViewType view(Class<ViewType> viewClass, int resourceId){
        return (ViewType) findViewById(resourceId);
    }

    public <DataT> void subscribeOnEvent(final Event<DataT> event, final Closure<DataT, Void> onEvent){
        Event.subscribeOnEvent(this,this,event,onEvent);
    }

    public <DataT> void unSubscribeFromEvents(){
        Event.unSubscribeFromEvents(this, this);
    }

    final protected View view(int resourceId){
        return findViewById(resourceId);
    }
    final protected SmookerApplication application(){
        return (SmookerApplication) getApplication();
    }
}
