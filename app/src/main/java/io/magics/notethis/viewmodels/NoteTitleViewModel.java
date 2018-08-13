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
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;

public class NoteTitleViewModel extends AndroidViewModel {

    private LiveData<List<NoteTitle>> noteTitles;
    private MutableLiveData<List<NoteTitle>> recentlyDeletedTitles = new MutableLiveData<>();
    private AppDatabase appDatabase;

    public NoteTitleViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        appDatabase = AppDatabase.getInMemoryDatabase(getApplication());
        noteTitles = appDatabase.userNoteModel().getNoteTitles();
    }


    public void observeNoteTitles(LifecycleOwner owner, Observer<List<NoteTitle>> observer) {
        noteTitles.observe(owner, observer);
    }

    public void unObserveNoteTitle(Observer<List<NoteTitle>> observer) {
        noteTitles.removeObserver(observer);
    }

    public void addTitleToRecentlyDeleted(NoteTitle noteTitle) {
        List<NoteTitle> tempData = recentlyDeletedTitles.getValue() != null ?
                recentlyDeletedTitles.getValue() : new ArrayList<>();
        tempData.add(noteTitle);
        recentlyDeletedTitles.setValue(tempData);
    }

    public void deleteRecentTitle() {
        if (recentlyDeletedTitles.getValue() == null || recentlyDeletedTitles.getValue().isEmpty()){
            return;
        }
        AppDbUtils.deleteNotes(appDatabase, recentlyDeletedTitles.getValue());
    }

    public void restoreTitle(NoteTitle noteTitle) {
        if (recentlyDeletedTitles.getValue() == null || recentlyDeletedTitles.getValue().isEmpty()) {
            return;
        }
        recentlyDeletedTitles.getValue().remove(noteTitle);
    }

}
