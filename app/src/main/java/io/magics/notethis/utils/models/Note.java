package io.magics.notethis.utils.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.util.StringUtil;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "body")
    private String body;
    @ColumnInfo(name = "preview")
    private String preview;

    public Note() {}

    @Ignore
    public Note(String title, String body, String preview) {
        this.title = title;
        this.body = body;
        this.preview = preview;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }

    @Ignore
    @Exclude
    public void setBodyPreview(){
        if (TextUtils.isEmpty(this.getBody())) return;
        if (body.length() <= 150) {
            this.setPreview(this.getBody());
        } else {
            this.setPreview(this.getBody().substring(0, 149));
        }
    }

    @Ignore
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
