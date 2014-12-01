package org.monroe.team.socks;

import org.monroe.team.socks.exception.ConnectionException;
import org.monroe.team.socks.exception.HandshakeException;
import org.monroe.team.socks.exception.InvalidProtocolException;
import org.monroe.team.socks.exception.ProtocolInitializationException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class SocksClient {

    private final InetAddress serverAddress;
    private final int port;

    public SocksClient(int port, InetAddress serverAddress) {
        this.port = port;
        this.serverAddress = serverAddress;
    }

    public SocksConnection getConnection(Class<? extends SocksConnection.Protocol> protocol, SocksConnection.ConnectionObserver observer)
            throws ConnectionException, InvalidProtocolException {
        Socket socket = null;
        try {
            socket = new Socket(serverAddress,port);
        }catch (IOException e) {
            throw new ConnectionException("Couldnt open socket",e);
        }

        SocksConnection connection = new SocksConnection(socket);

        try {
            connection.init();
        } catch (IOException e) {
            connection.destroy();
            throw new ConnectionException("Fail to init connection",e);
        }
        connection.setObserver(observer);

        try {
            connection.open(protocol, true);
        } catch (HandshakeException e) {
            connection.destroy();
            throw new ConnectionException("Handshake fails", e);
        } catch (InvalidProtocolException e){
            //everithing good with a sockets so could be reused in future
            connection.destroy();
            throw e;
        } catch (ProtocolInitializationException e) {
            connection.destroy();
            throw new ConnectionException("Protocol initialization fails", e);
        }
        return connection;
    }

    public void closeConnection(SocksConnection connection) {
        connection.destroy();
    }
}
