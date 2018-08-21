package io.magics.notethis.data.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import io.magics.notethis.utils.models.Image;
import io.magics.notethis.utils.models.Note;

@Database(entities = {Note.class, Image.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase{


    private static AppDatabase instance;

    public static final String DB_NAME = "note_this";

    public abstract NoteDao userNoteModel();
    public abstract ImageDao userImageModel();

    public static AppDatabase getInMemoryDatabase(Context context) {
        if (instance == null) {

            instance = Room.databaseBuilder(
                    context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
        }

        return instance;
    }

    public static void disposeInstance(){
        instance = null;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS notes");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE image ADD COLUMN serverId TEXT");
        }
    };

}