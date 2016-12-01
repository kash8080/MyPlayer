package com.example.rahul.myplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Rahul on 14-07-2016.
 */
public class songs {
    private String path="--";
    private String name="--";
    private String artist="--";
    private String image="bb";
    private Long id;
    private String imagepath ;
    private int no_of_songs ;
    Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Long album_id;

    public Long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(Long album_id) {
        this.album_id = album_id;
    }

    public songs(Long id, String name, String artist, String imagepath, int no_of_songs) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.imagepath = imagepath;
        this.no_of_songs = no_of_songs;
    }
    public songs(Long id, String name, String artist, String imagepath,Long album_id) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.imagepath = imagepath;
        this.album_id = album_id;
    }
    public songs(Long id, String name, String artist) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.album_id=album_id;
    }
    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public int getNo_of_songs() {
        return no_of_songs;
    }

    public void setNo_of_songs(int no_of_songs) {
        this.no_of_songs = no_of_songs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
