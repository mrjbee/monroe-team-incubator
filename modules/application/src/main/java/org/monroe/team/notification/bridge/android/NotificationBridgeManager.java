package org.monroe.team.notification.bridge.android;

/**
 * User: MisterJBee
 * Date: 12/14/13 Time: 1:36 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface NotificationBridgeManager {
    void activate();
    void disable();
    void onSettingChange(SettingAccessor<?> accessor);
}
