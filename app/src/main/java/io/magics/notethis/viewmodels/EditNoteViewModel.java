package io.magics.notethis.viewmodels;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.Observable;

import io.magics.notethis.ui.dialogs.SaveDialog;
import io.magics.notethis.utils.models.Note;

public class EditNoteViewModel extends ViewModel {
    public static final String NEW_NOTE_TITLE = "New Note";
    private String tempText;
    private Note note = new Note(NEW_NOTE_TITLE, "", "");
    private MutableLiveData<Note> savedNote = new MutableLiveData<>();
    private MutableLiveData<Note> readNote = new MutableLiveData<>();

    public void saveChanges(String title) {
        note.setTitle(title);
        note.setBody(tempText);
        note.setBodyPreview();
        savedNote.setValue(note);
    }

    public void setNoteId(int id) {
        if (note != null) note.setId(id);
    }

    public Note getNote() {
        return note;
    }

    public void newNote() {
        tempText = "";
        note = new Note(NEW_NOTE_TITLE, "", "");
    }

    public void setNote(Note note) {
        this.note = note;
        readNote.setValue(note);
    }

    public void observeOnSave(LifecycleOwner owner, Observer<Note> observer) {
        savedNote.observe(owner, observer);
    }

    public void removeObserver(LifecycleOwner owner) {
        savedNote.removeObservers(owner);
    }

    public void observeReadNote(LifecycleOwner owner, Observer<Note> observer) {
        readNote.observe(owner, observer);
    }

    public void removeReadObserver(Observer<Note> observer) {
        readNote.removeObserver(observer);
    }

    public boolean hasUnsavedChanges(String text) {
        if (TextUtils.isEmpty(text)) return false;
        boolean hasChanged = !note.getBody().equals(text);
        if (hasChanged) {
            tempText = text;
        }
        return hasChanged;
    }
}
