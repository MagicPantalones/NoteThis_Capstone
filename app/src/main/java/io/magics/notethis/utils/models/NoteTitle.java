package io.magics.notethis.utils.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;

public class NoteTitle {

    @PrimaryKey
    public int id;

    @ColumnInfo(name = "title")
    public String title;
}
