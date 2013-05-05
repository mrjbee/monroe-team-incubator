package org.monroe.team.app.arhitect.depended.example.bundle.impl;

import org.monroe.team.app.arhitect.depended.example.bundle.api.ExampleService;

/**
 * User: MisterJBee
 * Date: 5/5/13 Time: 6:57 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ExampleServiceImpl implements ExampleService {

    @Override
    public String concatStrings(String first, String second) {
        System.out.println("String to concat: "+first+", "+second);
        return first+second;
    }
}
