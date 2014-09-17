package org.monroe.team.smooker.app.common;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class QuitSmokeDataManager {

    private QuitSmokeScheduleData schedule;
    private final Context context;

    public QuitSmokeDataManager(Context context) {
        this.context = context;
    }

    private synchronized void persistSchedule(){

        FileOutputStream fos = null;
        ObjectOutputStream os = null;

        try {
            fos = context.openFileOutput("quitschedule.bin", Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(schedule);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public synchronized void restoreSchedule(){
        FileInputStream fis = null;
        ObjectInputStream is = null;
        try {
            fis = context.openFileInput("quitschedule.bin");
            is = new ObjectInputStream(fis);
            schedule = (QuitSmokeScheduleData) is.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public synchronized QuitSmokeScheduleData getSchedule(){
        if (schedule == null){
            restoreSchedule();
        }
        return schedule;
    }

    public synchronized void updateSchedule(Closure<QuitSmokeScheduleData,Void> update){
        try {
            update.execute(getSchedule());
        }finally {
            persistSchedule();
        }
    }

    public static class QuitSmokeScheduleData implements Serializable {

    }
}
