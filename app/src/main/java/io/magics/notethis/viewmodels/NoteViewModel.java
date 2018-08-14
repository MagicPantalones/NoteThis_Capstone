package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Observable;

import io.magics.notethis.data.db.AppDatabase;
import io.magics.notethis.ui.dialogs.SaveDialog;
import io.magics.notethis.utils.AppDbUtils;
import io.magics.notethis.utils.RoomNoteCallback;
import io.magics.notethis.utils.models.Note;

public class NoteViewModel extends AndroidViewModel {

    private static final String TAG = "NoteViewModel";
    public static final String NEW_NOTE_TITLE = "New Note";

    private MutableLiveData<Note> note = new MutableLiveData<>();
    private MutableLiveData<Note> savedNote = new MutableLiveData<>();

    private AppDatabase appDatabase;


    public NoteViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        appDatabase = AppDatabase.getInMemoryDatabase(getApplication());
    }

    public void newNote() {
        note.setValue(new Note(NEW_NOTE_TITLE, "", ""));
    }

    public void editNote(int id) {
        AppDbUtils.fetchNote(appDatabase, id, new RoomNoteCallback<Note>() {
            @Override
            public void onComplete(Note data) {
                note.setValue(data);
            }

            @Override
            public void onFail(Throwable e) {
                AppDbUtils.handleDbErrors(TAG, e);
            }
        });
    }

    public void saveChanges(String title) {
        if (note.getValue() == null) {
            Log.d(TAG, "Trying to save nonexistent note");
            return;
        }

        Note saveNote = note.getValue();
        saveNote.setTitle(title);

        if (TextUtils.isEmpty(saveNote.getPreview())) {
            saveNote.setBodyPreview();
        }

        note.setValue(saveNote);
        AppDbUtils.updateNote(appDatabase, saveNote);
    }

    public boolean hasUnsavedChanges(String text) {
        if (TextUtils.isEmpty(text)) return false;
        if (note.getValue() == null) {
            Log.w(TAG, "Tried to check if Null LiveData note had changes. " +
                    "This should never happen");
            note.setValue(new Note(NEW_NOTE_TITLE, "", ""));
        }

        boolean hasChanged = !text.equals(note.getValue().getBody());

        if (hasChanged) {
            Note tempNote = note.getValue();
            note.setValue(tempNote);
        }

        return hasChanged;
    }
}
