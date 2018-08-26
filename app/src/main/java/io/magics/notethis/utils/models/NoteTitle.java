package io.magics.notethis.utils.models;

import android.arch.persistence.room.ColumnInfo;

public class NoteTitle {

    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "preview")
    private String preview;

    public NoteTitle() {
        //Required public constructor
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }
}
