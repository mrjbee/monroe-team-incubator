package org.monroe.team.chekit.services;

import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class BackgroundTaskManager {

    private final ExecutorService service = Executors.newFixedThreadPool(4);
    private final Map<Integer, BackgroundTask> runningTasks = new HashMap<>();
    private int autoIncrement = 100;


    public synchronized void execute(BackgroundTask task){
        execute(generateId(),task);
    }

    private int generateId() {

        return autoIncrement++;
    }

    public synchronized void execute(int id, BackgroundTask task){
        cancelTask(id);
        runningTasks.put(id,task);
        //TODO: think how to deliver exception
        Future future =  service.submit(task);
    }

    public synchronized void cancelTask(int id) {
        if (runningTasks.get(id) != null){
            runningTasks.get(id).cancel(true);
            runningTasks.remove(id);
        }
    }

    public void destroy() {
        service.shutdownNow();
    }

    public static abstract class BackgroundTask<Result> extends FutureTask<Result> {

        private final Callable<Result> callable = new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return BackgroundTask.this.call();
            }
        };

        protected BackgroundTask() {
            this(new TaskCallable<Result>());
        }

        private BackgroundTask(TaskCallable<Result> call){
            super(call);
            call.owner = this;
        }

        private final Result call() throws Exception {
            Result answer = null;
            try {
                answer = doInBackground();
                if (isCancelled()){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            onCancel();
                            onFinish();
                        }
                    });
                }
            } catch (final InterruptedException ie){
                if (isCancelled()){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            onCancel();
                            onFinish();
                        }
                    });
                }
                return null;
            }catch (final Exception e){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        onException(e);
                        onFinish();
                    }
                });
                return null;
            }

            final Result finalAnswer = answer;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    onResult(finalAnswer);
                    onFinish();
                }
            });
            return finalAnswer;
        }

        protected abstract Result doInBackground() throws InterruptedException;
        protected void onException(Exception e){e.printStackTrace();}
        protected void onCancel(){}
        protected void onResult(Result result){}
        protected void onFinish(){}
    }


    private final static class TaskCallable<Result> implements Callable<Result>{
        private BackgroundTask<Result> owner;

        @Override
        public Result call() throws Exception {
            return owner.call();
        }
    }
}
