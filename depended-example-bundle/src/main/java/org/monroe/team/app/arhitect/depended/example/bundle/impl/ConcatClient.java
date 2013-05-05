package org.monroe.team.app.arhitect.depended.example.bundle.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.monroe.team.app.arhitect.depended.example.bundle.api.ExampleService;
import org.osgi.service.component.ComponentContext;

/**
 * User: MisterJBee
 * Date: 5/6/13 Time: 12:29 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Component
public class ConcatClient {

    @Reference
    private ExampleService concatService;

    protected void bindConcatService(ExampleService concatService){
        System.out.println("Goes to binding");
        this.concatService = concatService;
    }

    protected void unbindConcatService(ExampleService concatService){
        System.out.println("Goes to unbinding");
        this.concatService = null;
    }

    protected void activate(ComponentContext context){
        System.out.println("Concat service client activated");
        System.out.println("And it going to use this f*cking service....");
        System.out.println(concatService.concatStrings("Hello ", "OSGI"));
    }

    protected void deactivate(ComponentContext context){
        System.out.println("Concat service client deactivated");
    }
}
