package org.monroe.team.app;

import junit.framework.Assert;
import org.junit.Test;

/**
 * User: MisterJBee
 * Date: 1/7/14 Time: 3:54 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class AClassTest {

    AClass testInstance = new AClass();

    @Test
    public void shouldSayHello(){
        Assert.assertEquals("Hello man", testInstance.hello("man"));
    }
}
