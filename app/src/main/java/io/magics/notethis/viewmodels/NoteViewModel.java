package io.magics.notethis.viewmodels;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public class NoteViewModel extends ViewModel {

    private MutableLiveData<List<NoteTitle>> noteTitles = new MutableLiveData<>();
    private MutableLiveData<Note> note = new MutableLiveData<>();

    public void setNoteTitles(List<NoteTitle> noteTitles) {
        this.noteTitles.setValue(noteTitles);
    }

    public void setNote(Note note) {
        this.note.setValue(note);
    }


    public void observeNoteTitles(LifecycleOwner owner, Observer<List<NoteTitle>> observer) {
        noteTitles.observe(owner, observer);
    }

    public void unObserveNoteTitle(Observer<List<NoteTitle>> observer) {
        noteTitles.removeObserver(observer);
    }


    public void observeNote(LifecycleOwner owner, Observer<Note> observer) {
        note.observe(owner, observer);
    }

    public void unObserveNote(Observer<Note> observer) {
        note.removeObserver(observer);
    }
}
