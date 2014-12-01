package org.monroe.team.socks;

import com.sun.xml.internal.ws.client.SenderException;
import org.monroe.team.socks.exception.HandshakeException;
import org.monroe.team.socks.exception.InvalidProtocolException;
import org.monroe.team.socks.exception.ProtocolInitializationException;

import java.io.*;
import java.net.Socket;

public class SocksConnection implements ReaderTask.DataObserver{

    private final Socket socket;
    private Protocol protocol;
    private Thread readThread;
    private ReaderTask task;
    private InputStream in;
    private OutputStream out;
    private ConnectionObserver observer;

    public SocksConnection(Socket socket) {
        this.socket = socket;
    }

    void init() throws IOException {
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    public void send(Object msg){
        try {
            protocol.send(msg);
        } catch (Exception e) {
            throw new SenderException("Couldnt send", e);
        }
    }

    public void setObserver(ConnectionObserver observer) {
        this.observer = observer;
    }

    void open(Class<? extends Protocol> protocolType, boolean doHandshake) throws InvalidProtocolException, ProtocolInitializationException {
        if (doHandshake){
            handshake(protocolType);
        }
        try {
            protocol = protocolType.newInstance();
        } catch (Exception e) {
            throw new InvalidProtocolException("Couldn`t instantiate protocol",e);
        }
        try {
            protocol.create(in,out);
        } catch (Exception e) {
            throw new ProtocolInitializationException("Could create protocol",e);
        }

        task = protocol.createReaderTask(this);
        readThread = new Thread(task);
        readThread.start();
    }

    void accept() throws InvalidProtocolException, ProtocolInitializationException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String protocolName = null;
        try {
            protocolName = reader.readLine();
        } catch (IOException e) {
            throw new HandshakeException("Couldn`t read class",e);
        }
        Class<? extends Protocol> protocolClass = null;
        try {
           protocolClass = (Class<? extends Protocol>) Class.forName(protocolName);
        } catch (ClassNotFoundException e) {
            sendHandshakeRespond(writer,"INVALID_PROTOCOL");
            throw new InvalidProtocolException("Protocol class not found",e);
        }

        sendHandshakeRespond(writer, "OK");
        open(protocolClass, false);
    }

    private void sendHandshakeRespond(BufferedWriter writer, String handShakeRespond) {
        try {
            writer.write(handShakeRespond);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new HandshakeException("Could`n send "+handShakeRespond+" ",e);
        }
    }

    private boolean handshake(Class<? extends Protocol> protocolType) throws InvalidProtocolException {
        String response;
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            writer.write(protocolType.getName());
            writer.newLine();
            writer.flush();
            //TODO: implement timeout here
            response = reader.readLine();
        } catch (IOException ex){
            throw new HandshakeException("Handshake fail",ex);
        }
        if ("OK".equals(response)) {
            return false;
        } else if ("INVALID_PROTOCOL".equals(response)){
            throw new InvalidProtocolException("Server protcol invalid",null);
        }
        throw new HandshakeException("Invalid response "+response,null);
    }


    void destroy() {

        if (task != null){
            task.kill();
            readThread.interrupt();
        }

        if(protocol != null){
            try {
                protocol.sendShutdownSignal();
                protocol.clearResources();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (in != null){
            try {
                in.close();
            } catch (IOException e) {e.printStackTrace();}
        }
        if (out != null){
            try {
                out.close();
            } catch (IOException e) {e.printStackTrace();}
        }
        if (socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDataChunk(Object data) {
        observer.onData(data);
    }

    @Override
    public void onError(Exception e) {
        observer.onReadError(e);
    }

    @Override
    public void onShutdownRequested() {
        if(protocol != null){
            protocol.clearResources();
            protocol = null;
        }
        destroy();
    }


    public static interface Protocol{
        void create(InputStream inputStream, OutputStream outputStream) throws Exception;
        void send(Object message) throws Exception;
        void sendShutdownSignal() throws Exception;
        ReaderTask<Object> createReaderTask(ReaderTask.DataObserver observer);

        void clearResources();
    }

    public static interface ConnectionObserver{
        public void onData(Object data);
        public void onReadError(Exception e);
    }

}