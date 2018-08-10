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
    @ColumnInfo(name = "preview")
    private String preview;

    public Note() {}

    public Note(String id, String title, String body, String preview) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.preview = preview;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("body", body);
        result.put("preview", preview);

        return result;
    }
}
