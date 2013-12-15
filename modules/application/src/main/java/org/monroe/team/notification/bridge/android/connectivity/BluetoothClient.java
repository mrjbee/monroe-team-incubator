package org.monroe.team.notification.bridge.android.connectivity;

import android.bluetooth.BluetoothSocket;
import org.monroe.team.libdroid.logging.Debug;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 10:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
class BluetoothClient {

    private ObjectInputStream mInputStream;
    private ObjectOutputStream mOutputStream;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothClientListener mBluetoothClientListener;

    private OutThread mOutThread;
    private InThread mInThread;

    public void init(BluetoothSocket clientSocket) {
        if (mOutThread == null){
            mOutThread = new OutThread() {
                @Override
                protected void onOutGoingError(IOException e) {
                    if(mBluetoothClientListener!=null){
                        mBluetoothClientListener.onWriteError(BluetoothClient.this, e);
                    }
                }
            };
            mOutThread.start();
        } else {
            mOutThread.pullAll();
        }

        if(mInThread == null){
            mInThread = new InThread() {
                @Override
                protected void onObject(Object object) {
                    if(mBluetoothClientListener != null){
                       mBluetoothClientListener.onReadObject(BluetoothClient.this, object);
                    }
                }

                @Override
                protected void onError(Exception e) {
                    if(mBluetoothClientListener != null){
                        mBluetoothClientListener.onReadError(BluetoothClient.this, e);
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

    public BluetoothClientListener getBluetoothClientListener() {
        return mBluetoothClientListener;
    }

    public void setBluetoothClientListener(BluetoothClientListener bluetoothClientListener) {
        mBluetoothClientListener = bluetoothClientListener;
    }

    public static interface BluetoothClientListener{
        void onWriteError(BluetoothClient client, Exception e);
        void onReadError(BluetoothClient client, Exception e);
        void onReadObject(BluetoothClient client, Object object);
    }

    private abstract class InThread extends Thread {

        private Object awaitingObject = new Object();

        private InThread() {
            super("in_thread_client");
        }

        public void restoreReading(){
            synchronized (awaitingObject){
                awaitingObject.notify();
            }
        }

        @Override
        public void run() {
            while (isInterrupted()){
                try {
                    synchronized (awaitingObject){
                        Object object = BluetoothClient.this.mInputStream.readObject();
                        if (object!=null){
                            onObject(object);
                        } else {
                                try {
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

        protected abstract void onObject(Object object);
        protected abstract void onError(Exception e);
    }

    private abstract class OutThread extends Thread {

        private List<Object> objectsToWriteList = new LinkedList<Object>();
        private Object awaitFlag = new Object();

        private OutThread() {
            super("out_thread_client");
        }

        public void push(Object obj){
            synchronized (awaitFlag){
                objectsToWriteList.add(obj);
                if (objectsToWriteList.size() == 1){
                    awaitFlag.notify();
                }
            }
        }

        private Object top(){
            if (objectsToWriteList.isEmpty()) return null;
            return objectsToWriteList.get(0);
        }

        private Object pull(){
            return objectsToWriteList.remove(0);
        }

        @Override
        public void run() {
            while (isInterrupted()){
                Object objToWrite = null;
                synchronized (awaitFlag){
                    objToWrite = top();
                    if (objToWrite == null){
                        try {
                            awaitFlag.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
                try {
                    BluetoothClient.this.mOutputStream.writeObject(objToWrite);
                    synchronized (awaitFlag){
                        pull();
                    }
                } catch (IOException e) {
                    Debug.e(e,"Error during sending object = %s", objToWrite);
                    onOutGoingError(e);
                }
            }
        }

        protected abstract void onOutGoingError(IOException e);


        public void pullAll() {
            synchronized (awaitFlag){
                objectsToWriteList.clear();
            }
        }
    }

}
