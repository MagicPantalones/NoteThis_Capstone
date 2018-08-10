package io.magics.notethis.data.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import io.magics.notethis.utils.models.Image;
import io.magics.notethis.utils.models.Note;
import io.magics.notethis.utils.models.Timestamp;

@Database(entities = {Note.class, Image.class, Timestamp.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase instance;

    public static final String DB_NAME = "note_this";

    public abstract NoteDao userNoteModel();
    public abstract ImageDao userImageModel();
    public abstract TimestampDao timestampModel();

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