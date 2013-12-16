package org.monroe.team.notification.bridge.android.connectivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import org.monroe.team.libdroid.commons.VoidClosure;

import java.util.UUID;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 7:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothGateway implements BluetoothServer.OnClientListener, BluetoothClient.BluetoothClientListener {

    static final String SERVICE_NAME = "NotificationBridgeService";
    static final String SERVICE_UUID_PLAIN = "1e3e867b-aa65-4dc0-a400-6bc4762ef15e";

    static final UUID SERVICE_UUID = UUID.fromString(SERVICE_UUID_PLAIN);

    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServer mBluetoothServer;
    private final BluetoothClientPool mBluetoothClientPool;

    private boolean outActivated = false;

    public BluetoothGateway(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothServer = new BluetoothServer(mBluetoothAdapter);
        mBluetoothServer.setOnClientListener(this);
        mBluetoothClientPool = new BluetoothClientPool();
    }

    public <Type> void sendMessage(BluetoothExchange.IdAware message){
    }


    public boolean isSupported() {
        return mBluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter!= null && mBluetoothAdapter.isEnabled();
    }

    public void activateIncoming() {
        mBluetoothServer.openServerConnection();
    }

    public void deactivateIncoming() {
        mBluetoothServer.closeServerConnection();
    }

    public void activateOutgoing() {
        outActivated = true;
    }

    public void deactivateOutgoing() {
        outActivated = false;
    }

    @Override
    public void onClient(BluetoothSocket clientSocket) {
        BluetoothClient client = mBluetoothClientPool.getForIncomingClient(clientSocket);
        client.setBluetoothClientListener(this);
    }

    @Override
    public void onWriteError(BluetoothClient client, Exception e) {
    }

    @Override
    public void onReadError(BluetoothClient client, Exception e) {
    }

    @Override
    public void onReadObject(BluetoothClient client, Object object) {
    }

    @Override
    public void onEndReadSession(BluetoothClient client) {
        //greatest ever be happens
        mBluetoothClientPool.releaseClient(client);
    }

    public static interface BluetoothDelivery {
        public void onSuccessDeliver();
    }
}
