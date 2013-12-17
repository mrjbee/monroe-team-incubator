package org.monroe.team.notification.bridge.android.delivery.bluetooth;

import android.bluetooth.BluetoothSocket;
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
            mInThread.restoreReading();
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

    public void requestSessionEnd(){
        mInThread.stopReading();
    }


    public void forceSessionEnd() {
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

        private Object awaitingObject = new Object();
        private boolean requestSessionEnd = false;

        private InThread() {
            super("in_thread_client");
        }

        public void restoreReading(){
            synchronized (awaitingObject){
                awaitingObject.notify();
            }
        }

        public void stopReading() {
            synchronized (awaitingObject){
                requestSessionEnd = true;
            }
        }

        @Override
        public void run() {
            while (isInterrupted()){
                try {

                    synchronized (awaitingObject){
                        Object object = null;
                        if (!requestSessionEnd && BluetoothExchangePipe.this.mInputStream != null) {
                            object = BluetoothExchangePipe.this.mInputStream.readObject();
                        }
                        if (object!=null){
                            onExchange((BluetoothExchange) object);
                        } else {
                            try {
                                requestSessionEnd = false;
                                onEndOfSession();
                                awaitingObject.wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }

                } catch (ClassNotFoundException e) {
                    Debug.e(e,"Error during fetch object");
                    onError(e);
                } catch (IOException e) {
                    Debug.e(e, "Error during fetch object");
                    onError(e);
                }
            }
        }

        protected abstract void onEndOfSession();
        protected abstract void onExchange(BluetoothExchange exchange);
        protected abstract void onError(Exception e);


    }

}
