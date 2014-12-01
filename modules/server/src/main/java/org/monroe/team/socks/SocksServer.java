package org.monroe.team.socks;


import org.monroe.team.socks.exception.InvalidProtocolException;
import org.monroe.team.socks.exception.ProtocolInitializationException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocksServer {

    public static final int PORT_ANY = 0;
    private final Object controlMonitor = new Object();
    private ServerThread serverThread;
    private ErrorHandler errorHandler;

    private int readBuffer = 3000;

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void start(int port, InetAddress address) throws IOException {
        if (serverThread != null){
            shutdown();
        }
        synchronized (controlMonitor){
            ServerSocket serverSocket = new ServerSocket(port,0, address);
            serverThread = new ServerThread(serverSocket);
            serverThread.start();
        }
    }

    public int getListenPort(){
       synchronized (controlMonitor) {
           if (serverThread == null) throw new IllegalStateException("Seems not running");
           return serverThread.socket.getLocalPort();
       }
    }

    public void shutdown() {
        synchronized (controlMonitor){
            if (serverThread == null ) return;
            serverThread.kill();
            serverThread = null;
            controlMonitor.notifyAll();
        }
    }

    public void awaitShutdown() throws InterruptedException {
        synchronized (controlMonitor) {
            if (serverThread == null || !serverThread.isActive) return;
            controlMonitor.wait();
        }
    }

    private void onClient(Socket clientSocket){

        //TODO: more here
        SocksConnection connection = new SocksConnection(clientSocket);

        try {
            connection.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            connection.accept();
        } catch (InvalidProtocolException e) {
            e.printStackTrace();
        } catch (ProtocolInitializationException e) {
            e.printStackTrace();
        }
        connection.setObserver(new SocksConnection.ConnectionObserver() {
            @Override
            public void onData(Object data) {
                System.out.println("Server reads: "+data);
            }

            @Override
            public void onReadError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onCriticalError(Exception e) {
        if (errorHandler != null && !errorHandler.processCriticalError(e)) {
            shutdown();
        }
    }

    private void onError(IOException e) {
        if (errorHandler != null){
            errorHandler.onException(e);
        }
    }

    private final class ServerThread extends Thread {

        private final ServerSocket socket;
        private boolean isActive = true;

        private ServerThread(ServerSocket serverSocket) {
            this.socket = serverSocket;
        }

        @Override
        public void run() {
            try {
                startListing();
            }catch (Exception e){
                SocksServer.this.onCriticalError(e);
            }
        }

        private void startListing() throws IOException {
            int errorChecker = 0;
            while (isActive && !isInterrupted()){
                Socket clientSocket = null;
                try {
                    clientSocket = socket.accept();
                    errorChecker = 0;
                } catch (IOException e) {
                    //DO nothing if closed by request
                    if (!isActive) return;
                    errorChecker ++;
                    if (errorChecker > 20) {
                     throw new IllegalStateException("Too much errors without success connections.");
                    }else {
                        SocksServer.this.onError(e);
                    }
                }

                if (clientSocket != null){
                    SocksServer.this.onClient(clientSocket);
                }

            }
            if (isInterrupted()){
                throw new IllegalStateException("Thread was interrupted");
            }
        }

        public void kill() {
            isActive = false;
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public interface ErrorHandler {
        boolean processCriticalError(Exception e);
        void onException(IOException e);
    }


}
