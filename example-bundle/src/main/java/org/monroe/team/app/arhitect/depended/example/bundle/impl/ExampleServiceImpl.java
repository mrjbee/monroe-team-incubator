package org.monroe.team.app.arhitect.depended.example.bundle.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.monroe.team.app.arhitect.depended.example.bundle.api.ExampleService;
import org.osgi.service.component.ComponentContext;

/**
 * User: MisterJBee
 * Date: 5/5/13 Time: 6:57 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Component(immediate = true)
@Service(value = ExampleService.class)
public class ExampleServiceImpl implements ExampleService {

    @Override
    public String concatStrings(String first, String second) {
        System.out.println("String to concat: "+first+", "+second);
        return first+second;
    }

    protected void activate(ComponentContext context){
        System.out.print("Concat service activated");
    }

    protected void deactivate(ComponentContext context){
        System.out.print("Concat service deactivated");
    }
}
