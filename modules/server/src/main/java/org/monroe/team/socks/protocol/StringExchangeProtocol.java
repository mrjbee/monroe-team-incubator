package org.monroe.team.socks.protocol;

import org.monroe.team.socks.ReaderTask;
import org.monroe.team.socks.SocksConnection;

import java.io.*;

public class StringExchangeProtocol implements SocksConnection.Protocol{

    private static final String KILL_SIGNAL = "KILL_SIGNAL_asdasbnqwkejbwqe_FROM_CLIENT";
    private DataInputStream reader;
    private DataOutputStream writer;

    @Override
    public void create(DataInputStream inputStream, DataOutputStream outputStream) throws Exception {
        reader = inputStream;
        writer = outputStream;
    }

    @Override
    public void send(Object message) throws Exception {
        writer.writeUTF((String) message);
        writer.flush();
    }

    @Override
    public void sendShutdownSignal() throws Exception {
        send(KILL_SIGNAL);
    }

    @Override
    public ReaderTask<Object> createReaderTask(ReaderTask.DataObserver observer) {
        return new ReaderTask<Object>(observer) {
            @Override
            protected Object readForResult() throws IOException, ShutdownSignalException {
                String text =  reader.readUTF();
                if (KILL_SIGNAL.equals(text)) throw new ShutdownSignalException();
                return text;
            }
        };
    }

    @Override
    public void clearResources() {

    }

}
