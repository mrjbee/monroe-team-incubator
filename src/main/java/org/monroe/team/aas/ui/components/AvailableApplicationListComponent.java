package org.monroe.team.aas.ui.components;

/**
 * User: MisterJBee
 * Date: 8/24/13 Time: 6:43 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface AvailableApplicationListComponent {
    public interface AvailableApplicationListComponentDelegate {
        public void onApplicationSelected(String appId);
    }

}
