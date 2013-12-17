package org.monroe.team.notification.bridge.android.delivery.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import org.monroe.team.libdroid.logging.Debug;
import org.monroe.team.notification.bridge.common.IdAwareData;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 7:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothGateway implements BluetoothServer.OnClientListener{

    static final String SERVICE_NAME = "NotificationBridgeService";
    static final String SERVICE_UUID_PLAIN = "1e3e867b-aa65-4dc0-a400-6bc4762ef15e";

    static final UUID SERVICE_UUID = UUID.fromString(SERVICE_UUID_PLAIN);

    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServer mBluetoothServer;
    private final Map<String, BluetoothRemoteClient> mRemoteClientCacheMap = new HashMap<String, BluetoothRemoteClient>();
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private BluetoothExchangePipe mBluetoothExchangePipe = new BluetoothExchangePipe();

    public BluetoothGateway(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothServer = new BluetoothServer(mBluetoothAdapter);
        mBluetoothServer.setOnClientListener(this);
    }

    public boolean isSupported() {
        return mBluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter!= null && mBluetoothAdapter.isEnabled();
    }

    public void startServer() {
        mBluetoothServer.openServerConnection();
    }

    public void stopServer() {
        mBluetoothServer.closeServerConnection();
    }

    

    public List<BluetoothRemoteClient> getKnownDevices() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Set<String> clientCachedIdRemoveSet = new HashSet<String>(mRemoteClientCacheMap.keySet());
        for (BluetoothDevice device : devices) {
            String address = device.getAddress();
            clientCachedIdRemoveSet.remove(address);
            mRemoteClientCacheMap.put(address, new BluetoothRemoteClient(device));
        }
        for (String clientToRemove : clientCachedIdRemoveSet) {
            mRemoteClientCacheMap.remove(clientToRemove);
        }
        return new ArrayList<BluetoothRemoteClient>(mRemoteClientCacheMap.values());
    }

    
    //As server
    @Override
    public void onClient(final BluetoothSocket clientSocket) {
        try {
            clientSocket.close();
        } catch (IOException e) {
            Debug.e(e," Test closign");
        }
        /*     mBluetoothExchangePipe.setup(clientSocket, new BluetoothExchangePipe.BluetoothClientListener() {
                   @Override
                   public void onReadError(BluetoothExchangePipe client, Exception e) {
                       Debug.e(e,"Error during reading");
                   }

                   @Override
                   public void onWriteError(BluetoothExchangePipe bluetoothExchangePipe, BluetoothExchange exchange, Exception e) {
                       Debug.e(e,"Error during writing = %s",exchange);
                   }

                   @Override
                   public void onExchange(BluetoothExchangePipe client, BluetoothExchange exchange) {
                       Debug.i("new exchange = %s", exchange);
                   }

                   @Override
                   public void onSessionEnd(BluetoothExchangePipe client) {
                       Debug.i("Release pipe");
                       mBluetoothExchangePipe.free();
                   }
               });
       stopServer(); */
    }

    //As client
    public void send(BluetoothRemoteClient client, final IdAwareData[] notification, BluetoothDeliveryCallback deliveryCallback) {
        final BluetoothSocket socket = client.openConnection();
        if (socket == null){
            deliveryCallback.onFailBeforeSend(notification, client);
            return;
        }
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mBluetoothExchangePipe.setup(socket, new BluetoothExchangePipe.BluetoothClientListener() {
                    @Override
                    public void onReadError(BluetoothExchangePipe client, Exception e) {
                        Debug.e(e,"Error during reading");
                    }

                    @Override
                    public void onWriteError(BluetoothExchangePipe bluetoothExchangePipe, BluetoothExchange exchange, Exception e) {
                        Debug.e(e,"Error during writing = %s",exchange);
                    }

                    @Override
                    public void onExchange(BluetoothExchangePipe client, BluetoothExchange exchange) {
                        Debug.i("new exchange = %s", exchange);
                    }
                    @Override
                    public void onSessionEnd(BluetoothExchangePipe client) {
                        Debug.i("Release pipe");
                        mBluetoothExchangePipe.free();
                    }
                });
                for (IdAwareData idAwareData : notification) {
                    mBluetoothExchangePipe.write(new BluetoothExchange(idAwareData));
                }
                mBluetoothExchangePipe.endWrite();
            }
        });
    }



    public static interface BluetoothDeliveryCallback {
        public void onFailBeforeSend(IdAwareData[] dataset, BluetoothRemoteClient client);
        public void onSuccessDeliver(IdAwareData data, BluetoothRemoteClient client);
        public void onFailDuringSend(IdAwareData data, BluetoothRemoteClient client);
    }
}
