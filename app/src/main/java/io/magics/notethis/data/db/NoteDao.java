package io.magics.notethis.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;
import io.reactivex.Flowable;

@Dao
public interface NoteDao {

    @Query("SELECT Note.id, Note.title, Note.preview FROM Note")
    LiveData<List<NoteTitle>> getNoteTitles();
    
    @Query("SELECT * FROM Note WHERE Note.id = :id")
    Note getNote(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(Note... notes);

    @Update
    void updateNote(Note... notes);

    @Query("SELECT count(*) FROM Note")
    int checkHasData();

    @Query("DELETE FROM note")
    void deleteAll();

    @Query("DELETE FROM note where id == :id")
    void deleteNotes(int id);

}
