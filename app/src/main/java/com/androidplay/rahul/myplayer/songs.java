package com.androidplay.rahul.myplayer;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Rahul on 14-07-2016.
 */
public class songs implements Serializable {
    private String path="";
    private String name="--";
    private String artist="--";
    private String image="bb";
    private Long id;
    private String imagepath ;
    private int no_of_songs ;
    boolean clicked=false;
    private String Data="";
    private int sortorder;
    //for artist
    private String numberOfAlbums;
    private String numberOfTracks;
    private String artistkey;
    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
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
    public songs(Long id, String name, String artist, String imagepath,Long album_id,String Data) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.imagepath = imagepath;
        this.album_id = album_id;
        this.Data=Data;
    }
    public songs(Long id, String name, String artist) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.album_id=album_id;
    }
    //for artist
    public songs(Long id, String name, String numberofalbums,String numberOfTracks,String artistkey){
        this.id = id;
        this.name = name;
        this.numberOfAlbums = numberofalbums;
        this.numberOfTracks=numberOfTracks;
        this.artistkey=artistkey;

        String str="";
        str=str.concat(numberofalbums);
        if(numberofalbums.equals("1")){
            str = str.concat(" Album  ");
        }else {
            str = str.concat(" Albums  ");
        }
        str=str.concat(numberOfTracks);
        if(numberOfTracks.equals("1")){
            str=str.concat(" Track");
        }else {
            str=str.concat(" Tracks");
        }
        this.artist=str;

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

    public String getNumberOfAlbums() {
        return numberOfAlbums;
    }

    public void setNumberOfAlbums(String numberOfAlbums) {
        this.numberOfAlbums = numberOfAlbums;
    }

    public String getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setNumberOfTracks(String numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }

    public String getArtistkey() {
        return artistkey;
    }

    public void setArtistkey(String artistkey) {
        this.artistkey = artistkey;
    }

    public int getSortorder() {
        return sortorder;
    }

    public void setSortorder(int sortorder) {
        this.sortorder = sortorder;
    }
}
