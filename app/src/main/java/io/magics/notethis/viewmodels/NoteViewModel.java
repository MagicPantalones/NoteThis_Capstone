package io.magics.notethis.viewmodels;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public class NoteViewModel extends ViewModel {

    private MutableLiveData<List<NoteTitle>> noteTitles = new MutableLiveData<>();
    private List<NoteTitle> recentlyDeletedTitles = new ArrayList<>();
    private static final String TAG = "NoteViewModel";

    public void setNoteTitles(List<NoteTitle> titles) {
        noteTitles.setValue(titles);
    }

    public void observeNoteTitles(LifecycleOwner owner, Observer<List<NoteTitle>> observer) {
        noteTitles.observe(owner, observer);
    }

    public void unObserveNoteTitle(Observer<List<NoteTitle>> observer) {
        noteTitles.removeObserver(observer);
    }

    public int getDbNoteCount() {
        if (noteTitles.getValue() == null) return -1;
        return noteTitles.getValue().size();
    }

    public void addTitleToRecentlyDeleted(NoteTitle noteTitle) {
        recentlyDeletedTitles.add(noteTitle);
        if (noteTitles.getValue() != null) {
            noteTitles.getValue().remove(noteTitle);
        }
    }

    public List<NoteTitle> getRecentlyDeletedTitles() {
        return recentlyDeletedTitles;
    }

    public NoteTitle getRecentlyDeleted(NoteTitle noteTitle) {
        for (NoteTitle title : recentlyDeletedTitles) {
            if (noteTitle.getId() == title.getId()) {
                return title;
            }
        }
        IllegalArgumentException e = new IllegalArgumentException(
                "Recently deleted list does not contain NoteTitle with ID: " + noteTitle.getId()
                        + ". This should not happen. Ever!");
        Log.e(TAG, "getRecentlyDeleted: ", e);
        return null;
    }
}
