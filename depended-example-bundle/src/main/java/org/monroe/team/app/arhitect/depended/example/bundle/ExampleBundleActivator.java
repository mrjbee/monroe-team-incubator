package org.monroe.team.app.arhitect.depended.example.bundle;

import org.monroe.team.app.arhitect.depended.example.bundle.api.ExampleService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * User: MisterJBee
 * Date: 5/5/13 Time: 6:59 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ExampleBundleActivator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
       System.out.println("Start example bundle [depended]");
       // Query for all service references matching any language.
       ServiceReference refs = bundleContext.getServiceReference(ExampleService.class.getName());
       System.out.println("Founded service references = " + refs);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("Stop example bundle [depended]");
    }
}
