package org.monroe.team.socks;

public interface SocksConnection<DataType> {
    public void send(DataType type);
}
