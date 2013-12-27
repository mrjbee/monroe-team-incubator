package org.monroe.team.notification.bridge.android.delivery.socket;
import java.io.IOException;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 8:51 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SocketServer {

    SocketServerThread mExecutionThread;
    SocketServerCallback mSocketServerCallback;
    boolean mStarted = false;
    final SocketServerDelegate mSocketServerDelegate;

    public SocketServer(SocketServerDelegate socketServerDelegate) {
        mSocketServerDelegate = socketServerDelegate;
    }

    public synchronized void start(SocketServerCallback socketServerCallback){
        mExecutionThread = new SocketServerThread();
        mSocketServerCallback = socketServerCallback;
        mStarted = true;
        mExecutionThread.start();
    }

    public synchronized void stop(){
        mSocketServerCallback = null;
        if(mExecutionThread != null){
            mExecutionThread.release();
            mExecutionThread = null;
        }
        mStarted = false;
    }

    public synchronized boolean isStarted() {
        return mStarted;
    }

    private synchronized void fails(Exception e) {
       if (mExecutionThread == null) return;
       SocketServerCallback callback = mSocketServerCallback;
       stop();
       callback.onStopWithError(e);
    }

    private final class SocketServerThread extends Thread {

        private SocketServerThread() {
            super("socket_server");
        }

        @Override
        public void run() {
            try {
                mSocketServerDelegate.start();
            } catch (Exception e) {
                fails(e);
                return;
            }
            while (!isInterrupted()){
                try {
                    SocketClient client = mSocketServerDelegate.accept();
                    mSocketServerCallback.onClient(client);
                } catch (IOException e) {
                    fails(e);
                    break;
                }
            }
        }

        public void release() {
            interrupt();
            mSocketServerDelegate.stop();
        }
    }

    public static interface SocketServerCallback {
        void onClient(SocketClient client);
        void onStopWithError(Exception e);
    }

    public static interface SocketServerDelegate {
        void start() throws Exception;
        SocketClient accept() throws IOException;
        void stop();
    }

}
