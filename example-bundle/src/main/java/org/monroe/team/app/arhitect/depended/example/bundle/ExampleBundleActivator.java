package org.monroe.team.app.arhitect.depended.example.bundle;

import org.monroe.team.app.arhitect.depended.example.bundle.api.ExampleService;
import org.monroe.team.app.arhitect.depended.example.bundle.impl.ExampleServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * User: MisterJBee
 * Date: 5/5/13 Time: 6:59 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ExampleBundleActivator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
       System.out.println("Start example bundle");
       Hashtable<String, String> props = new Hashtable<String, String>();
       props.put("Language", "English");
       bundleContext.registerService(
                ExampleService.class.getName(), new ExampleServiceImpl(), props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("Stop example bundle");
    }
}
