package io.magics.notethis.utils.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "timestamp")
public class Timestamp {

    @PrimaryKey
    @NonNull
    public String timeStamp = "init";

}
