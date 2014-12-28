package org.monroe.team.socks.example;

import org.monroe.team.socks.SocksServer;
import org.monroe.team.socks.SocksTransport;
import org.monroe.team.socks.exception.SendFailException;
import org.monroe.team.socks.protocol.StringExchangeProtocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class EchoServer {



    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        System.out.println("Socks Echo Server "+Version.get());
        if (args == null || args.length == 0){
            System.out.println("Specify port as first argument");
            return;
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        }catch (NumberFormatException e){
            System.out.println("Invalid port = "+args[0]);
            return;
        }

        final SocksServer server = createServer(port);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                server.shutdown();
            }
        });
        server.awaitShutdown();
    }

    public static SocksServer createServer(int port) {
        SocksServer instance = new SocksServer();
        instance.setTransportServlet(new SocksServer.Servlet() {
            @Override
            public void onData(Object data, SocksTransport transport) {
                if (!transport.getProtocol().getClass().equals(StringExchangeProtocol.class)){
                    System.out.println("Server get <unexpected protocol>: "+data +" "+transport);
                    transport.destroy();
                } else {
                    System.out.println("Server get: " + data + " " + transport);
                    try {
                        transport.send(prepareEchoString((String) data));
                    } catch (SendFailException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Exception e, SocksTransport transport) {
                System.out.println("Server error: "+e +" "+transport);
                e.printStackTrace();
            }
        });
        try {
            instance.start(port, InetAddress.getByName("0.0.0.0"));
            System.out.println("Server started at port:"+instance.getListenPort());
        } catch (IOException e) {
            throw new RuntimeException("Couldn`t start server", e);
        }
        return instance;
    }

    public static String prepareEchoString(String originString) {
        StringBuffer buffer = new StringBuffer(originString);
        return buffer.reverse().toString();
    }
}
