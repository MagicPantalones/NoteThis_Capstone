package io.magics.notethis.utils.models;

import java.io.File;

public class ImgurUpload {

    private File image;
    private String title;
    private String description;
    private String albumId;

    public ImgurUpload(File image, String title){
        this.image = image;
        this.title = title;
    }

    public String getTitle() { return title; }
    public File getImage() { return image; }
    public String getDescription() { return description; }
    public String getAlbumId() { return albumId; }
}
