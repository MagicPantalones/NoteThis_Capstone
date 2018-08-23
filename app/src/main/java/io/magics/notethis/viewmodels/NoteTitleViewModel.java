package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.util.DbUtils;

import java.util.ArrayList;
import java.util.List;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.utils.AppDbUtils;
import io.magics.notethis.utils.RoomNoteCallback;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public class NoteTitleViewModel extends AndroidViewModel {

    private LiveData<List<NoteTitle>> noteTitles;
    private Note deletedTitle;
    private AppDatabase appDatabase;
    private MutableLiveData<Note> deletedNote = new MutableLiveData<>();

    public NoteTitleViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        appDatabase = AppDatabase.getInMemoryDatabase(getApplication());
        noteTitles = appDatabase.userNoteModel().getNoteTitles();
    }

    public LiveData<List<NoteTitle>> getNoteTitles() {
        return noteTitles;
    }
    public LiveData<Note> getDeletedNote() {
        return deletedNote;
    }

    public void deleteTitle(NoteTitle noteTitle) {
        if (deletedTitle != null) deletedNote.setValue(deletedTitle);
        deletedTitle = null;
        AppDbUtils.fetchNote(appDatabase, noteTitle.getId(), new RoomNoteCallback<Note>() {
            @Override
            public void onComplete(Note data) {
                deletedTitle = data;
                AppDbUtils.deleteNote(appDatabase, noteTitle);
            }

            @Override
            public void onFail(Throwable e) {
                //No action
            }
        });
    }

    public void restoreTitle() {
        AppDbUtils.insertNote(appDatabase, deletedTitle, null);
    }

    public void deletePermanent() {
        deletedNote.setValue(deletedTitle);
        deletedTitle = null;
    }

}
