package io.magics.notethis.data.db;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import java.util.List;

import io.magics.notethis.utils.RoomInsertException;
import io.magics.notethis.utils.RoomNoteCallback;
import io.magics.notethis.utils.models.Image;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.NoteTitle;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Database(entities = {Note.class, Image.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase{


    private static AppDatabase instance;

    public static final String DB_NAME = "note_this";

    public abstract NoteDao userNoteModel();
    public abstract ImageDao userImageModel();

    public static AppDatabase getInMemoryDatabase(Context context) {
        if (instance == null) {

            instance = Room.databaseBuilder(
                    context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .build();
        }

        return instance;
    }

    public static void disposeInstance(){
        instance = null;
    }
    

}