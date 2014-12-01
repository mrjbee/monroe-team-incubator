package org.monroe.team.socks;


import java.io.IOException;

public abstract class ReaderTask<Data> implements Runnable {

    private final DataObserver<Data> observer;

    private boolean isActive = true;

    protected ReaderTask(DataObserver<Data> observer) {
        this.observer = observer;
    }


    @Override
    public void run() {
        while (isActive && !Thread.currentThread().isInterrupted()){
            try {
                Data result = readForResult();
                notifyOwner(result);
            } catch (ShutdownSignalException e){
                notifyOwnerShutdown();
                isActive = false;
            } catch (Exception e) {
                notifyOwner(e);
            }
        }
    }

    private void notifyOwnerShutdown() {
        if (!isActive) return;
        observer.onShutdownRequested();
    }

    private synchronized void notifyOwner(Exception e){
        if (!isActive) return;
        observer.onError(e);
    }

    private synchronized void notifyOwner(Data result) {
        if (!isActive) return;
        observer.onDataChunk(result);
    }

    public synchronized void kill(){
        isActive = false;
    }

    protected abstract Data readForResult() throws IOException, ShutdownSignalException;

    public static interface DataObserver<DataType>{
        public void onDataChunk(DataType data);
        void onError(Exception e);
        void onShutdownRequested();
    }

    public static class ShutdownSignalException extends Exception{}
}
