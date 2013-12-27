package org.monroe.team.notification.bridge.android.delivery.socket;

import org.monroe.team.libdroid.commons.Should;
import org.monroe.team.libdroid.testing.TSupport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * User: MisterJBee
 * Date: 12/27/13 Time: 11:35 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SocketGatewayAccTest extends TSupport{

    private final SocketGateway mTestInstance = new SocketGateway(new TcpIpSocketServerDelegate());

    public static class TcpIpSocketServerDelegate implements SocketServer.SocketServerDelegate{

        ServerSocket mServerSocket;

        @Override
        public void start() throws Exception {
            mServerSocket = new ServerSocket(4040);
        }

        @Override
        public SocketClient accept() throws IOException {
            Socket socket = mServerSocket.accept();
            return new ServerSocketClient(socket);
        }

        @Override
        public void stop() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Should.reThrow("Error during closing socket", e);
            }
        }

        public static class ServerSocketClient implements SocketClient{

            final Socket mSocket;

            public ServerSocketClient(Socket socket) {
                mSocket = socket;
            }
        }


    }
}
