package org.monroe.team.libdroid.mservice;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 8:31 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class ServiceUtils {
    private ServiceUtils() {}

    public static boolean isServiceForeground(Class serviceClass, Context context){
        ActivityManager.RunningServiceInfo runningServiceInfo = getServiceInfo(serviceClass, context);
        if (runningServiceInfo != null){
            return runningServiceInfo.foreground;
        }
        return false;
    }

    public static boolean isServiceRunning(Class serviceClass, Context context){
        ActivityManager.RunningServiceInfo runningServiceInfo = getServiceInfo(serviceClass, context);
        if (runningServiceInfo != null){
            return runningServiceInfo.started;
        }
        return false;
    }

    public static ActivityManager.RunningServiceInfo getServiceInfo(Class serviceClass, Context context){
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                return runningServiceInfo;
            }
        }
        return null;
    }
}
