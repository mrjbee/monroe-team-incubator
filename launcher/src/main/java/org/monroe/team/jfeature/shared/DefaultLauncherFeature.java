package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.launcher.Main;
import org.monroe.team.jfeature.shared.api.LauncherFeature;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 10:44 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DefaultLauncherFeature implements LauncherFeature{

    @Override
    public void shutdown() {
        shutdown(0);
    }

    @Override
    public void shutdown(int statusCode) {
        Main.continueMain(statusCode);
    }
}
