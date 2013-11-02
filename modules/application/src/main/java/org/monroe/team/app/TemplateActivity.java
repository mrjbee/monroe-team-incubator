package org.monroe.team.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import org.monroe.team.app.db.DaoMaster;
import org.monroe.team.app.db.DaoSession;
import org.monroe.team.app.db.Note;
import org.monroe.team.app.db.NoteDao;

import java.util.Date;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 9/21/13 Time: 8:08 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class TemplateActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        DaoMaster.DevOpenHelper db = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        DaoMaster daoMaster = new DaoMaster(db.getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        NoteDao noteDao = daoSession.getNoteDao();


        Note note = new Note(null, "Some text", "Some comment", new Date());
        noteDao.insert(note);
        Log.d("DaoExample", "Inserted new note, ID: " + note.getId());

        List<Note> notes = noteDao.loadAll();
        Log.d("DaoExample", "Notes count = "+notes.size() + " notes:"+notes);

    }
}
