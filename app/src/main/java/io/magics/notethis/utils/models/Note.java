package io.magics.notethis.utils.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Note {

    @PrimaryKey(autoGenerate = true)
    private String id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "body")
    private String body;

    public Note() {}

    public Note(String id, String title, String body) {
        this.id = id;
        this.title = title;
        this.body = body;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("body", body);

        return result;
    }
}
