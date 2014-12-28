package org.monroe.team.socks.example;

import junit.framework.Assert;
import org.junit.*;
import org.monroe.team.socks.broadcast.BroadcastReceiver;
import org.monroe.team.socks.broadcast.MapBroadcastMessageTransport;
import org.monroe.team.socks.exception.ConnectionException;

import java.util.Map;

public class BroadcastTest {

    private static BroadcastReceiver<Map<String,String>,
            MapBroadcastMessageTransport,
            BroadcastReceiver.BroadcastMessageObserver<Map<String,String>>> receiver;

    @BeforeClass
    public static void init() throws ConnectionException {
        receiver = new BroadcastReceiver<Map<String, String>, MapBroadcastMessageTransport, BroadcastReceiver.BroadcastMessageObserver<Map<String, String>>>(
                new MapBroadcastMessageTransport(),
                createObserver()
        );
        receiver.start(0);
    }

    @Before
    public void prepare(){
        Assert.assertEquals(receiver.isAlive(), true);
    }

    @AfterClass
    public static void destroy(){
        receiver.shutdown();
    }

    @Test
    public void shouldReceive(){
        //nothing here yet
        System.out.println(receiver.getPort());
    }


    private static BroadcastReceiver.BroadcastMessageObserver<Map<String, String>> createObserver() {
        return new BroadcastReceiver.BroadcastMessageObserver<Map<String, String>>() {
            @Override
            public void onMessage(Map<String, String> stringStringMap) {

            }
        };
    }
}
