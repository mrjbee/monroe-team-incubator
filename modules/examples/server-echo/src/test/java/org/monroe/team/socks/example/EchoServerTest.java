package org.monroe.team.socks.example;

import org.junit.*;
import org.monroe.team.socks.SocksClient;
import org.monroe.team.socks.SocksConnection;
import org.monroe.team.socks.SocksServer;
import org.monroe.team.socks.SocksTransport;
import org.monroe.team.socks.exception.ConnectionException;
import org.monroe.team.socks.exception.InvalidProtocolException;
import org.monroe.team.socks.protocol.StringExchangeProtocol;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EchoServerTest {

    private static SocksServer testInstance;
    private List<SocksConnection> autoDisconnectList = new ArrayList<SocksConnection>();
    private SocksClient client;

   // @BeforeClass
    public static void startServer(){
        testInstance = EchoServer.createServer(7777);
    }

    @Before
    public void prepare() throws UnknownHostException {
        autoDisconnectList.clear();
        client = new SocksClient(7777, InetAddress.getLocalHost());
    }


    @After
    public void clearResources(){
        for (SocksConnection connection : autoDisconnectList) {
            client.closeConnection(connection);
        }
    }

    @Test
    public void shouldResponseOnSingleMessages() throws ConnectionException, InvalidProtocolException, InterruptedException {
        final List<Object> answer = new ArrayList<Object>(3);

        SocksConnection<String> connection = client.getConnection(StringExchangeProtocol.class, new SocksTransport.ConnectionObserver<String>() {
            @Override
            public void onData(String data) {
                answer.add(data);
            }

            @Override
            public void onReadError(Exception e) {
                answer.add(e);
            }
        });

        autoDisconnectList.add(connection);

        connection.send("Hello, I`m client");

        waitUnless(answer, 1);

        Assert.assertEquals(1, answer.size());
        Assert.assertEquals(EchoServer.prepareEchoString("Hello, I`m client"),answer.get(0));
    }

    @Test
    public void shouldResponseOnFewMessagesWithSameClient() throws ConnectionException, InvalidProtocolException, InterruptedException {
        final List<Object> answer = new ArrayList<Object>(3);

        SocksConnection<String> connection = client.getConnection(StringExchangeProtocol.class, new SocksTransport.ConnectionObserver<String>() {
            @Override
            public void onData(String data) {
                System.out.println(data);
                answer.add(data);
            }

            @Override
            public void onReadError(Exception e) {
                answer.add(e);
            }
        });

        autoDisconnectList.add(connection);

        connection.send("Hello, I`m client");
        connection.send("How do you do?");

        waitUnless(answer, 2);

        Assert.assertEquals(2, answer.size());
        Assert.assertEquals(EchoServer.prepareEchoString("Hello, I`m client"),answer.get(0));
        Assert.assertEquals(EchoServer.prepareEchoString("How do you do?"),answer.get(1));
    }


    @Test
    public void shouldResponseOnFewMessagesWithDifferentClients() throws ConnectionException, InvalidProtocolException, InterruptedException {
        final List<Object> answer = new ArrayList<Object>(3);
        final List<Object> answer2 = new ArrayList<Object>(3);

        SocksConnection<String> connection = client.getConnection(StringExchangeProtocol.class, new SocksTransport.ConnectionObserver<String>() {
            @Override
            public void onData(String data) {
                answer.add(data);
            }

            @Override
            public void onReadError(Exception e) {
                answer.add(e);
            }
        });
        SocksConnection<String> connection2 = client.getConnection(StringExchangeProtocol.class, new SocksTransport.ConnectionObserver<String>() {
            @Override
            public void onData(String data) {
                answer2.add(data);
            }

            @Override
            public void onReadError(Exception e) {
                answer2.add(e);
            }
        });
        autoDisconnectList.add(connection);
        autoDisconnectList.add(connection2);

        connection.send("Hello, I`m client");
        connection2.send("How do you do?");

        waitUnless(answer, 1);
        waitUnless(answer2, 1);

        Assert.assertEquals(1, answer.size());
        Assert.assertEquals(1, answer2.size());
        Assert.assertEquals(EchoServer.prepareEchoString("Hello, I`m client"),answer.get(0));
        Assert.assertEquals(EchoServer.prepareEchoString("How do you do?"),answer2.get(0));
    }


    private void waitUnless(Collection<?> awaitList,int waitForCount) throws InterruptedException {
        waitUnless(awaitList,waitForCount,10,500);
    }

    private void waitUnless(Collection<?> awaitList,int waitForCount, int tryCount, int tryTimeout) throws InterruptedException {
        int tries = 0;
        while (awaitList.size() < waitForCount && tries<tryCount){
            Thread.sleep(tryTimeout);
            tries++;
        }
        if (awaitList.size() != waitForCount){
            throw new AssertionError("Expected answer count "+waitForCount+" , but get "+awaitList);
        }
    }

   // @AfterClass
    public static void stopServer(){
        testInstance.shutdown();
    }

}