package org.monroe.team.notification.bridge.android.delivery.socket;

/**
 * User: MisterJBee
 * Date: 12/27/13 Time: 11:44 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SocketGateway {

    private final SocketServer mSocketServer;
    private SocketServer.SocketServerCallback mSocketServerCallback;

    public SocketGateway(SocketServer.SocketServerDelegate serverDelegate) {
        mSocketServer = new SocketServer(serverDelegate);
    }

    public synchronized void acceptIncoming(){
        mSocketServer.start(getServerCallback());
    }

    public synchronized void declaimIncoming(){
        if(mSocketServer.isStarted()){
            mSocketServer.stop();
        }
    }

    private SocketServer.SocketServerCallback getServerCallback() {
        if (mSocketServerCallback == null){
            mSocketServerCallback = new SocketServer.SocketServerCallback() {
                @Override
                public void onClient(SocketClient client) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void onStopWithError(Exception e) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            };
        }
        return mSocketServerCallback;
    }


}
