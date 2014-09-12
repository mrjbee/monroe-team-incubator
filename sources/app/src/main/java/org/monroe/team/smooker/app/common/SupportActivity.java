package org.monroe.team.smooker.app.common;

import android.app.Activity;
import android.view.View;

import org.monroe.team.smooker.app.SmookerApplication;

public class SupportActivity extends Activity {

    final protected Model model(){
       return ((SmookerApplication)getApplication()).getModel();
    }

    final protected <ViewType extends View> ViewType view(Class<ViewType> viewClass, int resourceId){
        return (ViewType) findViewById(resourceId);
    }

    final protected View view(int resourceId){
        return findViewById(resourceId);
    }
}
