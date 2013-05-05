package org.monroe.team.app.arhitect.example.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * User: MisterJBee
 * Date: 5/5/13 Time: 6:59 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ExampleBundleActivator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
       System.out.print("Start example bundle");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.print("Stop example bundle");
    }
}
