package io.magics.notethis.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;


import java.util.List;

import io.magics.notethis.utils.models.Image;

@Dao
public interface ImageDao {

    @Query("SELECT * FROM Image")
    LiveData<List<Image>> getImages();

    @Query("SELECT count(*) FROM Image")
    int checkHasData();

    @Insert
    List<Long> insertImages(Image... images);

    @Delete
    void deleteImage(Image image);

    @Query("DELETE FROM Image")
    void deleteAll();

}
