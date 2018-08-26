package io.magics.notethis.utils.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Image {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "serverId")
    private String serverId;

    @ColumnInfo(name = "link")
    private String link;

    @ColumnInfo(name = "title")
    private String title;

    @Ignore
    @Exclude
    private int status;

    public Image(){}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Ignore
    @Exclude
    public int getStatus() { return status; }
    @Ignore
    @Exclude
    public void setStatus(int status) { this.status = status; }

    @Ignore
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("serverId", serverId);
        result.put("link", link);
        return result;
    }
    
}
