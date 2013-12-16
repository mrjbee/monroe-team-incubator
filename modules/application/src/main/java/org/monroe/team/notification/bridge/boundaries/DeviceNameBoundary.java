package org.monroe.team.notification.bridge.boundaries;

public interface DeviceNameBoundary {
    public String getDeviceName(String deviceName);
    public void OnDeviceNameChanged(String deviceName, String oldDeviceName);
}
