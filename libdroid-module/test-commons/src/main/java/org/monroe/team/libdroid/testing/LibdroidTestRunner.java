package org.monroe.team.libdroid.testing;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.bytecode.ClassInfo;
import org.robolectric.bytecode.Setup;

import java.util.Properties;

/**
 * User: MisterJBee
 * Date: 9/22/13 Time: 1:45 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class LibdroidTestRunner extends RobolectricTestRunner {

    private LibdroidTestRunnerConfigure mTestRunnerConfigure = null;

    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory by default. Use the {@link org.robolectric.annotation.Config} annotation to configure.
     *
     * @param testClass the test class to be run
     * @throws org.junit.runners.model.InitializationError
     *          if junit says so
     */
    public LibdroidTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public Setup createSetup() {
        return new SetupExt();
    }

    @Override
    protected Properties getConfigProperties() {
        Properties properties = new Properties();
        Class[] shadows = getTestRunnerConfigure().getShadows();
        if (shadows != null && shadows.length > 0){
            StringBuilder shadowStringBuilder = new StringBuilder();
            for (Class shadow : shadows) {
                shadowStringBuilder.append(shadow.getName()).append(",");
            }
            shadowStringBuilder.deleteCharAt(shadowStringBuilder.length()-1);
            properties.setProperty("shadows",shadowStringBuilder.toString());
        }
        properties.setProperty("manifest", getTestRunnerConfigure().getManifest());
        return properties;
    }

    private LibdroidTestRunnerConfigure getTestRunnerConfigure() {
        if (mTestRunnerConfigure == null){
            Class aClass = null;
            try {
                aClass = Class.forName("libdroid.conf.LibdroidTestRunnerConfigureImpl");
            } catch (ClassNotFoundException e) {
                aClass = DefaultLibdroidTestRunner.class;
            }
            try {
                mTestRunnerConfigure = (LibdroidTestRunnerConfigure) aClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("LibdroidTestRunnerConfigure implementation could not be used", e);
            }
        }
        return mTestRunnerConfigure;
    }

    public static interface LibdroidTestRunnerConfigure {
        Class[] getShadows();
        String getManifest();
        boolean allowToShadow(ClassInfo classInfo);
    }

    private class SetupExt extends Setup{
        @Override
        public boolean isFromAndroidSdk(ClassInfo classInfo) {
            return super.isFromAndroidSdk(classInfo) || getTestRunnerConfigure().allowToShadow(classInfo);
        }
    }
}
