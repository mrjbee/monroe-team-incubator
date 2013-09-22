package org.monroe.team.libdroid.testing;

import org.robolectric.annotation.Config;
import org.robolectric.bytecode.ClassInfo;

/**
 * User: MisterJBee
 * Date: 9/22/13 Time: 2:29 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DefaultLibdroidTestRunner implements LibdroidTestRunner.LibdroidTestRunnerConfigure{

    @Override
    public Class[] getShadows() {
        return new Class[]{LibdroidTestLog.class};
    }

    @Override
    public String getManifest() {
        return Config.NONE;
    }

    @Override
    public boolean allowToShadow(ClassInfo classInfo) {
        return false;
    }
}

