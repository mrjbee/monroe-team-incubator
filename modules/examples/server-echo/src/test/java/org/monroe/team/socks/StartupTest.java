package org.monroe.team.socks;

import org.junit.*;
import org.monroe.team.socks.exception.ConnectionException;
import org.monroe.team.socks.exception.InvalidProtocolException;
import org.monroe.team.socks.protocol.StringExchangeProtocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class StartupTest {

    private static SocksServer server;

    @BeforeClass
    public static void init() throws IOException {
  //      server = new SocksServer();
  //      server.start(SocksServer.PORT_ANY, InetAddress.getByName("0.0.0.0"));
    }


    @Test
    public void startServer() throws IOException, ConnectionException, InvalidProtocolException, InterruptedException {
        server = new SocksServer();
        server.start(7777, InetAddress.getByName("0.0.0.0"));
        System.out.println("Server started....");
        server.awaitShutdown();

    }

    @Test
    public void clientConnect() throws UnknownHostException, ConnectionException, InvalidProtocolException, InterruptedException {
        Thread.sleep(20);
        SocksClient client = new SocksClient(7777, InetAddress.getLocalHost());
        SocksConnection connection = pingServerWithNewConnection(client);
        client.closeConnection(connection);
        Thread.sleep(20);
        System.out.println("Run second");
        connection = pingServerWithNewConnection(client);
        client.closeConnection(connection);
    }

    private SocksConnection pingServerWithNewConnection(SocksClient client) throws ConnectionException, InvalidProtocolException, InterruptedException {
        SocksConnection connection = client.getConnection(StringExchangeProtocol.class, new SocksConnection.ConnectionObserver() {
            @Override
            public void onData(Object data) {
                System.out.println("Readed:"+data);
            }

            @Override
            public void onReadError(Exception e) {
                e.printStackTrace();
            }
        });
        for(int i=0;i<10;i++) {
            connection.send("Hello World "+i);
            Thread.sleep(10);
        }
        return connection;
    }


    @AfterClass
    public static void destroy() throws InterruptedException {
    //    server.shutdown();
    //    server.awaitShutdown();
        System.out.println("Server stopped");
    }

}
