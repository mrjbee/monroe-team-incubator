package org.monroe.team.bonee.core.utils;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 12:52 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class Should {

    private Should() {}

    public static void shouldBeTrue(String msg, boolean... value){
        for (boolean bool:value){
            if (!bool) throw new RuntimeException(msg);
        }
    }
}
