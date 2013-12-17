package org.monroe.team.notification.bridge.boundaries;

public interface DeviceNameBoundary {

    public interface Required {
        public String getDeviceName(String deviceName);
    }

    public interface Declare {
        public void OnDeviceNameChanged(String deviceName);
    }
}
