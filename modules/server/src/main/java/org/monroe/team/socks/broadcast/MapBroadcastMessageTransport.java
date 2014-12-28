package org.monroe.team.socks.broadcast;

import java.util.Map;

public class MapBroadcastMessageTransport implements BroadcastMessageTransport<Map<String,String>> {

    @Override
    public Map<String, String> fromString(String msg) {
        return null;
    }

    @Override
    public String toString(Map<String, String> msg) {
        return null;
    }
}
