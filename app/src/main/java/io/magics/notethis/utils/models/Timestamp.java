package io.magics.notethis.utils.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Timestamp {

    @PrimaryKey
    public String timeStamp;

}
