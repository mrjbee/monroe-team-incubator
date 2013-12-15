package org.monroe.team.notification.bridge.android.connectivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import org.monroe.team.libdroid.logging.Debug;

import java.io.IOException;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 8:51 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
class BluetoothServer {

    private BluetoothServerThread mExecutionThread;
    private final BluetoothAdapter mDeviceAdapter;

    BluetoothServer(BluetoothAdapter deviceAdapter) {
        mDeviceAdapter = deviceAdapter;
    }

    public synchronized void openServerConnection(){
        mExecutionThread = new BluetoothServerThread();
        mExecutionThread.start();
    }

    public synchronized void closeServerConnection(){
        if(mExecutionThread != null){
            mExecutionThread.closeConnection();
        }
    }

    private final class BluetoothServerThread extends Thread {

        private BluetoothServerSocket serverSocket;

        private BluetoothServerThread() {
            super("BLT_server");
        }

        @Override
        public void run() {
            serverSocket = null;
            while (!isInterrupted()){
                if(serverSocket == null){
                    try {
                        serverSocket = mDeviceAdapter.listenUsingRfcommWithServiceRecord(BluetoothGateway.SERVICE_NAME,
                                BluetoothGateway.SERVICE_UUID);
                    } catch (IOException e) {
                        Debug.e(e,"Something bad with bluetooth");
                    }
                }
                if (serverSocket != null){
                    try {
                        BluetoothSocket clientSocket = serverSocket.accept();
                    } catch (IOException e) {
                        Debug.e(e, "Something bad with bluetooth during awaiting");
                    }
                }
            }
        }

        private void safeServerSocketClosing() {
            if(serverSocket != null){
                try {
                    serverSocket.close();
                 } catch (IOException e) {
                    Debug.e(e, "Something bad due closing bluetooth server");
                }
                serverSocket = null;
            }
        }

        public void closeConnection() {
            this.interrupt();
            safeServerSocketClosing();
        }
    }

}
