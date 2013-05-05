package org.monroe.team.app.arhitect;

import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;
import org.osgi.framework.launch.Framework;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * User: MisterJBee
 * Date: 5/5/13 Time: 6:10 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Launcher {
    public static void main(String[] argv) throws Exception{
        org.apache.felix.main.Main.main(argv);
    }
}
