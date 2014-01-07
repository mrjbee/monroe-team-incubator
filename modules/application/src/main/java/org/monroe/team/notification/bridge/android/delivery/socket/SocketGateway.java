package org.monroe.team.notification.bridge.android.delivery.socket;

import org.monroe.team.libdroid.commons.Closure;
import org.monroe.team.libdroid.commons.Should;
import org.monroe.team.libdroid.commons.VoidClosure;
import org.monroe.team.libdroid.logging.Debug;

/**
 * User: MisterJBee
 * Date: 12/27/13 Time: 11:44 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SocketGateway<DestinationDescriptionType> {

    private final SocketServer mSocketServer;
    private final SocketClientFactory<DestinationDescriptionType> mSocketClientFactory;
    private SocketServer.SocketServerCallback mSocketServerCallback;

    public SocketGateway(SocketServer.SocketServerDelegate socketServer, SocketClientFactory<DestinationDescriptionType> socketClientFactory) {
        mSocketServer = new SocketServer(socketServer);
        mSocketClientFactory = socketClientFactory;
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
                public void onClient(final SocketServer server, SocketClient client) {
                    server.stop();
                    SocketPipe pipe = getFreeSocket();
                    pipe.start(client, new SocketPipe.SocketPipeCallback() {
                        @Override
                        public void onMessageDelivered(SocketPipe socketPipe, SocketClient socketClient, String id) {
                            Should.fails("No message to send expected");
                        }

                        @Override
                        public void onNewMessage(SocketPipe socketPipe, SocketClient socketClient, String id, Object body) {
                            //To change body of implemented methods use File | Settings | File Templates.
                        }

                        @Override
                        public void onStopBecauseOfError(SocketPipe socketPipe, SocketClient socketClient, Exception e) {
                            Debug.e(e, "Server connection killed");
                            onStop(socketPipe, socketClient);
                        }

                        @Override
                        public void onStop(SocketPipe socketPipe, SocketClient client) {
                            server.start(getServerCallback());
                            freeSocketPipe(socketPipe);
                        }
                    });
                }

                @Override
                public void onStopWithError(SocketServer server, Exception e) {
                    Should.beImplemented();
                }
            };
        }
        return mSocketServerCallback;
    }

    public synchronized void sendTo(DestinationDescriptionType description, IdAware... messages){
        SocketClient client = mSocketClientFactory.getBy(description);
        SocketPipe socketPipe = getFreeSocket();
        socketPipe.start(client, new SocketPipe.SocketPipeCallback() {
            @Override
            public void onMessageDelivered(SocketPipe socketPipe, SocketClient socketClient, String id) {

            }

            @Override
            public void onNewMessage(SocketPipe socketPipe, SocketClient socketClient, String id, Object body) {

            }

            @Override
            public void onStopBecauseOfError(SocketPipe socketPipe, SocketClient socketClient, Exception e) {
                freeSocketPipe(socketPipe);
            }

            @Override
            public void onStop(SocketPipe socketPipe, SocketClient client) {
                freeSocketPipe(socketPipe);
            }
        });
    }


    private SocketPipe getFreeSocket() {
        return new SocketPipe();
    }
    private void freeSocketPipe(SocketPipe socketPipe) {
    }
}
