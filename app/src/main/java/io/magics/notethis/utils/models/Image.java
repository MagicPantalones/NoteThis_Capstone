package io.magics.notethis.utils.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Image {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "link")
    private String link;

    @ColumnInfo(name = "title")
    private String title;

    public Image(){}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
}
