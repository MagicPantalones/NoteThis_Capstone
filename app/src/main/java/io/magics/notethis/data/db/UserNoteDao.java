package io.magics.notethis.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;
import io.reactivex.Flowable;

@Dao
public interface UserNoteDao {

    @Query("SELECT id, title FROM Note")
    Flowable<NoteTitle> getNoteTitles();

    @Query("SELECT * FROM Note WHERE id = :id")
    Note getNote(int id);

    @Insert
    void insertAll(Note... notes);

    @Query("DELETE FROM Note")
    void deleteAll();

    @Delete
    void delete(Note note);

}
