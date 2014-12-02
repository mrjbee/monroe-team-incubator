package org.monroe.team.socks.example;

import org.monroe.team.socks.SocksClient;
import org.monroe.team.socks.SocksConnection;
import org.monroe.team.socks.SocksServer;
import org.monroe.team.socks.SocksTransport;
import org.monroe.team.socks.exception.ConnectionException;
import org.monroe.team.socks.exception.InvalidProtocolException;
import org.monroe.team.socks.protocol.StringExchangeProtocol;

import java.io.Console;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class StringClient {

    private static boolean active =true;
    private static SocksClient client;
    private static SocksConnection<String> socksConnection;

    public static void main(String[] args) throws UnknownHostException, ConnectionException, InvalidProtocolException {

        if (args == null || args.length != 1){
            System.out.println("Please specify host:port, aka 127.0.0.1");
            return;
        }
        System.out.println("Socks Client [text] "+Version.get());
        Console console = System.console();
        if (console == null) {
            System.out.println("Unable to fetch console");
            return;
        }

        int port = 0;
        String host = null;
        try{
           String[] hostPort = args[0].split(":");
           port = Integer.parseInt(hostPort[1]);
           host = hostPort[0];
        }catch (Exception e){
           System.out.println("Client: something bad with address = "+args[0]);
           throw new RuntimeException(e);
        }

        client = new SocksClient(port, InetAddress.getByName(host));
        socksConnection = client.getConnection(StringExchangeProtocol.class, new SocksTransport.ConnectionObserver<String>() {

            int errorCount = 0;

            @Override
            public void onData(String data) {
                System.out.println("[Server response] "+data);
                errorCount = 0;
            }

            @Override
            public void onReadError(Exception e) {
                System.out.println("Client: [error] "+e.getMessage());
                e.printStackTrace();
                errorCount++;
                if(errorCount > 5){
                    shutdown();
                }
            }
        });


        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                shutdown();
            }
        });

        System.out.println("Type message and hit enter:");
        while (active){
            String line = console.readLine();
            if (active) {
                socksConnection.send(line);
            }
        }
    }

    private static void shutdown() {
        if (client!=null && socksConnection!=null) {
            System.out.println("Client: shutdown ");
            active = false;
            client.closeConnection(socksConnection);
            client = null;
        }
    }
}
