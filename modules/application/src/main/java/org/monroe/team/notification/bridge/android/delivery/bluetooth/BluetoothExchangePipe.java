package org.monroe.team.notification.bridge.android.delivery.bluetooth;

import android.bluetooth.BluetoothSocket;
import org.monroe.team.libdroid.commons.Should;
import org.monroe.team.libdroid.logging.Debug;

import java.io.*;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 10:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
class BluetoothExchangePipe {

    private ObjectInputStream mInputStream;
    private ObjectOutputStream mOutputStream;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothClientListener mBluetoothClientListener;

    private InThread mInThread;

    final public void setup(BluetoothSocket clientSocket, BluetoothClientListener bluetoothClientListener) {
        mBluetoothClientListener = bluetoothClientListener;
        if(mInThread == null){
            mInThread = new InThread() {
                @Override
                protected void onEndOfSession() {
                    if(mBluetoothClientListener != null){
                        mBluetoothClientListener.onSessionEnd(BluetoothExchangePipe.this);
                    }
                }

                @Override
                protected void onExchange(BluetoothExchange exchange) {
                    if(mBluetoothClientListener != null){
                       mBluetoothClientListener.onExchange(BluetoothExchangePipe.this, exchange);
                    }
                }

                @Override
                protected void onError(Exception e) {
                    if(mBluetoothClientListener != null){
                        mBluetoothClientListener.onReadError(BluetoothExchangePipe.this, e);
                    }
                }
            };
        } else {
            mInThread.startReading();
        }

        mBluetoothSocket = clientSocket;

        try {
            mInputStream = new ObjectInputStream(mBluetoothSocket.getInputStream());
            mOutputStream = new ObjectOutputStream(mBluetoothSocket.getOutputStream());
        } catch (IOException e) {
            Debug.e(e,"During opening client stream");
            closeConnections();
        }
    }

    public void write(BluetoothExchange exchange){
        try {
            mOutputStream.writeObject(exchange);
        } catch (IOException e) {
            Debug.e(e, "Exception during writing object");
            mBluetoothClientListener.onWriteError(this, exchange);
        }
    }


    public void forceSessionEnd() {
        mInThread.stopReading();
        closeConnections();
        mBluetoothClientListener = null;
    }

    private void closeConnections() {

        if (mInputStream != null){
            try {
                mInputStream.close();
            } catch (IOException e) {
                Debug.e(e,"During closing client input stream");
            }
            mInputStream = null;
        }

        if (mOutputStream != null){
            try {
                mOutputStream.close();
            } catch (IOException e) {
                Debug.e(e,"During closing client out stream");
            }
            mOutputStream = null;
        }

        if (mBluetoothSocket != null){
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                Debug.e(e,"During closing client");
            }
            mBluetoothSocket = null;
        }
    }


    public static interface BluetoothClientListener{
        void onReadError(BluetoothExchangePipe client, Exception e);
        void onWriteError(BluetoothExchangePipe bluetoothExchangePipe, BluetoothExchange exchange);
        void onExchange(BluetoothExchangePipe client, BluetoothExchange exchange);
        void onSessionEnd(BluetoothExchangePipe client);
    }

    private abstract class InThread extends Thread {

        private KillSignal killSignal = new KillSignal();

        private InThread() {
            super("in_thread_client");
        }

        void startReading(){

            synchronized (killSignal){
                if(killSignal.killRequest){
                    try {
                        killSignal.wait();
                    } catch (InterruptedException e) {
                        Should.failsHere("Shouldn`t wait too long to be interrupted.", e);
                    }
                }
            }

            synchronized (killSignal){
                killSignal.notify();
            }
        }

        void stopReading() {
            synchronized (killSignal){
                if (killSignal.killRequest){
                    return;
                }
                killSignal.killRequest = true;
                try {
                    killSignal.wait();
                } catch (InterruptedException e) {
                    Should.failsHere("Shouldn`t wait too long to be interrupted.", e);
                }
            }
        }

        @Override
        public void run() {
            while (isInterrupted()) {
                synchronized (killSignal) {
                    try {
                        Object object = null;
                        if (!killSignal.killRequest) {
                            object = BluetoothExchangePipe.this.mInputStream.readObject();
                        }
                        if (object != null) {
                            onExchange((BluetoothExchange) object);
                        } else {
                            onEndOfSession();
                            killSignal.notifyAll();
                            try {
                                killSignal.wait();
                                killSignal.killRequest = false;
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        Debug.e(e, "Error during fetch object");
                        onError(e);
                    } catch (IOException e) {
                        Debug.e(e, "Error during fetch object");
                        onError(e);
                    }
                }
            }
        }

        protected abstract void onEndOfSession();
        protected abstract void onExchange(BluetoothExchange exchange);
        protected abstract void onError(Exception e);

        private class KillSignal{
             private boolean killRequest = false;
        }

    }

}
