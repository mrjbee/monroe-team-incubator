package org.monroe.team.jfeature.launcher;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 11:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ConsoleLog {

    public void i(String msg) {
       if (consoleOutEnabled()){
           System.out.println(msg);
       }
    }

    private boolean consoleOutEnabled() {
        return true;
    }
}
