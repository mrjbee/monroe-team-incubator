package org.monroe.team.smooker.promo.common.quitsmoke;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class QuitSmokeDataManager {

    private final Context context;

    public QuitSmokeDataManager(Context context) {
        this.context = context;
    }

    public synchronized void persist(QuitSmokeData schedule){
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

    public synchronized boolean delete() {
        return context.deleteFile("quitschedule.bin");
    }


    public synchronized boolean exists() {
        return context.getFileStreamPath("quitschedule.bin").exists();
    }

    public synchronized QuitSmokeData restore(){
        FileInputStream fis = null;
        ObjectInputStream is = null;
        try {
            fis = context.openFileInput("quitschedule.bin");
            is = new ObjectInputStream(fis);
            return  (QuitSmokeData) is.readObject();
        }
        catch (Exception e) {
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

}
