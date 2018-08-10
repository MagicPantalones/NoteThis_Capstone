package io.magics.notethis.utils.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;

public class NoteTitle {

    @PrimaryKey
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "preview")
    private String preview;

    public NoteTitle() {}

    public NoteTitle(int id, String title, String preview) {
        this.id = id;
        this.title = title;
        this.preview = preview;
    }
}
