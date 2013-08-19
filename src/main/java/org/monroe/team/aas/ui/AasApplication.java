package org.monroe.team.aas.ui;
import android.app.Application;
import android.os.Environment;
import org.monroe.team.aas.model.PublicGatewayService;
import org.monroe.team.aas.model.PublicModelModelService;
import org.monroe.team.aas.ui.common.Logs;
import org.monroe.team.aas.ui.common.logging.Debug;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class AasApplication extends Application{

    private static final Object[] APPLICATION_CONSTRAINS = {
            new Object[] {DashboardActivity.class, 4},
            new Object[] {PublicModelModelService.class, 4},
            new Object[] {PublicGatewayService.class, 4},
    };

    @Override
    public void onCreate() {
        Logs.UI.i("Start application");
        super.onCreate();
        try {
            Class strictModeClass = Class.forName("android.os.StrictMode");
            installStrictMode(strictModeClass);
            /*StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectLeakedSqlLiteObjects()
                    .setClassInstanceLimit()
                    .detectAll().penaltyLog().penaltyDropBox().penaltyDeath().build()); */
            System.setErr(new HProfDumpingStderrPrintStream(this, System.err));
            Logs.UI.i("Strict mode enable.");
        } catch (ClassNotFoundException e) {
            Logs.UI.i("Strict mode disabled.");
        } catch (RuntimeException e){
            Logs.UI.w(e, "Exception during init strict mode");
        }
    }

    private void installStrictMode(Class strictModeClass) {
        try {
            Class vmPolicyBuilderClass = Class.forName("android.os.StrictMode$VmPolicy$Builder");
            Object vmPolicyBuilder = vmPolicyBuilderClass.newInstance();
            String[] builderMethods = {
                    "detectLeakedClosableObjects",
                    "detectLeakedRegistrationObjects",
                    "detectLeakedSqlLiteObjects"};
            vmPolicyBuilder = executeBuilderMethods(vmPolicyBuilderClass, vmPolicyBuilder, builderMethods);

            vmPolicyBuilder = setClassInstanceLimits(vmPolicyBuilderClass, vmPolicyBuilder, APPLICATION_CONSTRAINS);

            String[] builderMethods2 = {
                    "penaltyLog",
                    "penaltyDeath",
                    "build"};
            vmPolicyBuilder = executeBuilderMethods(vmPolicyBuilderClass, vmPolicyBuilder, builderMethods2);
            Class vmPolicyClass = Class.forName("android.os.StrictMode$VmPolicy");
            Method setVmPolicyMethod = strictModeClass.getMethod("setVmPolicy", vmPolicyClass);
            setVmPolicyMethod.invoke(null, vmPolicyBuilder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object setClassInstanceLimits(Class vmPolicyBuilderClass, Object vmPolicyBuilder, Object... classIntegerPair) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = vmPolicyBuilderClass.getMethod("setClassInstanceLimit",Class.class,int.class);
        for (Object integerPair : classIntegerPair) {
            vmPolicyBuilder = method.invoke(vmPolicyBuilder, ((Object[])integerPair)[0], ((Object[])integerPair)[1]);
        }
        return vmPolicyBuilder;
    }

    private Object executeBuilderMethods(Class vmPolicyBuilderClass, Object vmPolicyBuilder, String[] builderMethods) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object result = null;
        for (String methodName: builderMethods){
            Method method = vmPolicyBuilderClass.getMethod(methodName);
            result = method.invoke(vmPolicyBuilder);
        }
        return result;
    }

    private static class HProfDumpingStderrPrintStream extends PrintStream{

        private final Application application;

        public HProfDumpingStderrPrintStream (Application app, OutputStream destination){
            super (destination);
            application = app;
        }

        @Override
        public synchronized void println (String str)
        {
            super.println(str);
            if (str.contains("POLICY_DEATH"))
            {
                // StrictMode is about to terminate us... don't let it!
                super.println ("Trapped StrictMode shutdown notice: logging heap data");
                try {
                    File dumpHome = Environment.getExternalStorageDirectory();
                    File dumpFile = new File(dumpHome,"strictmode-death-penalty.hprof");
                    android.os.Debug.dumpHprofData(dumpFile+"");
                    Debug.i("Write hprof before stop to " + dumpFile.getAbsolutePath());
                } catch (Exception e) {
                    Debug.e(e,"couldnt write hprof");
                }
            }
        }
    }
}
