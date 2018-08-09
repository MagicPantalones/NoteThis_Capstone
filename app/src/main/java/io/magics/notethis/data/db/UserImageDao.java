package io.magics.notethis.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.magics.notethis.utils.models.Image;

@Dao
public interface UserImageDao {

    @Query("SELECT * FROM Image")
    List<Image> getImages();

    @Insert
    void insertImages(Image... images);

    @Delete
    void deleteImage(Image image);

}
