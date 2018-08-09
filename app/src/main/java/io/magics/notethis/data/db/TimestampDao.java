package io.magics.notethis.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import io.magics.notethis.utils.models.Timestamp;

@Dao
public interface TimestampDao {

    @Query("SELECT * FROM timestamp")
    Timestamp getTimestamp();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTimestamp();
}
