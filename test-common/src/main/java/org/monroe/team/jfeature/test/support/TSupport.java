package org.monroe.team.jfeature.test.support;

import junit.framework.Assert;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 10:06 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class TSupport {

    public  void shouldFail() {
        Assert.fail();
    }

    public void should(boolean b) {
        should("be true", b);
    }

    public void should(String msg, boolean shouldBeTrue){
       if (!shouldBeTrue){
           Assert.assertTrue("Should "+msg,shouldBeTrue);
       }
    }

    public void should(String msg, Object object,  Object object1) {
        should(msg, object.equals(object1));
    }

    public void should(Object object,  Object object1) {
        should("be equals "+object+" and "+ object1, object, object1);
    }

}
