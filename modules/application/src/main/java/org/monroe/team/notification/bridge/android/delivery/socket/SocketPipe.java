package org.monroe.team.notification.bridge.android.delivery.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * User: MisterJBee
 * Date: 12/28/13 Time: 12:04 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SocketPipe {

    SocketClient mSocketClient;
    ObjectInputStream mInputStream;
    ObjectOutputStream mOutputStream;
    SocketReadThread mReadThread;
    Set<String> mSentMessageIdSet = new HashSet<String>();
    SocketPipeCallback mSocketPipeCallback;

    public synchronized void start(SocketClient socketClient, SocketPipeCallback callback){
        mSocketClient = socketClient;
        mSocketClient.init();
        mSentMessageIdSet.clear();
        try {
            if(socketClient.isConnectionOwner()){
                mInputStream = mSocketClient.getInputStream();
                mOutputStream = mSocketClient.getOutputStream();
            } else {
                mOutputStream = mSocketClient.getOutputStream();
                mInputStream = mSocketClient.getInputStream();
            }
        } catch (IOException e) {
            failsWith(e);
            return;
        }
        mReadThread = new SocketReadThread(mInputStream);
        mReadThread.start();
    }

    public synchronized void writeEnd(){
        write(null);
    }

    public synchronized void write(SocketMessage<?> message){
        try {
            mSentMessageIdSet.add(message.getId());
            mOutputStream.writeObject(message);
        } catch (IOException e) {
            failsWith(e);
        }
    }

    private synchronized void processMessage(String id, Object body) {
        if (body instanceof DeliveryApprove){
            if(mSentMessageIdSet.remove(id)){
                mSocketPipeCallback.onMessageDelivered(this, mSocketClient, id);
            }
        } else {
            write(new IdAware.IdAwareSocketMessage(new DeliveryApprove(id)));
            mSocketPipeCallback.onNewMessage(this, mSocketClient, id, body);
        }
    }

    private synchronized void failsWith(Exception e) {
        SocketPipeCallback callback = mSocketPipeCallback;
        SocketClient client = mSocketClient;
        freeResources();
        callback.onStopBecauseOfError(this, client, e);
    }

    private synchronized void endOfData() {
        writeEnd();
        SocketPipeCallback callback = mSocketPipeCallback;
        SocketClient client = mSocketClient;
        freeResources();
        callback.onStop(this, client);
    }

    private void freeResources() {
        if (mReadThread != null){
            mReadThread.interrupt();
            mReadThread = null;
        }
        mOutputStream = null;
        mInputStream = null;
        mSocketClient.deInit();
        mSocketClient = null;
    }


    private class SocketReadThread extends Thread{

        private final ObjectInputStream mInputStream;

        private SocketReadThread(ObjectInputStream inputStream) {
            super("client_read_socket");
            mInputStream = inputStream;
        }

        @Override
        public void run() {
           while (!isInterrupted()){
               try {
                   Object readObject = mInputStream.readObject();
                   if(readObject == null){
                       endOfData();
                       return;
                   }
                   if(readObject instanceof SocketMessage){
                       String id = ((SocketMessage) readObject).getId();
                       Object body = ((SocketMessage) readObject).getBody();
                       processMessage(id, body);
                   }else {
                       throw new IllegalStateException("Unknown type of message was read = "+readObject);
                   }
               } catch (Exception e) {
                   failsWith(e);
                   return;
               }
           }
        }
    }

    public static interface SocketPipeCallback{
        void onMessageDelivered(SocketPipe socketPipe, SocketClient socketClient, String id);
        void onNewMessage(SocketPipe socketPipe, SocketClient socketClient, String id, Object body);
        void onStopBecauseOfError(SocketPipe socketPipe, SocketClient socketClient, Exception e);
        void onStop(SocketPipe socketPipe, SocketClient client);
    }

    private final class DeliveryApprove implements IdAware {

        private final String mMessageId;

        private DeliveryApprove(String messageId) {
            mMessageId = messageId;
        }

        @Override
        public String getId() {
            return mMessageId;
        }
    }
}
