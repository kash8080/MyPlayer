package com.androidplay.one.myplayer.helper_classes;

import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplay.one.myplayer.R;

/**
* Created by Rahul on 31-12-2016.
*/

public class Toolbar_ActionMode_Callback implements ActionMode.Callback {

    private artist_interface artist;
    private home_interface home;
    private album_interface album;
    private open_playlist_interface open_playlist;
    String method;

    public interface artist_interface{
         void setNullToActionMode();
         void removeSelections();
         void context_delete();
         void context_addtoPlaylist();
         void canremoveSelection(boolean f);
    }
    public interface album_interface{
        void setNullToActionMode();
        void removeSelection();
        void context_delete();
        void context_addtoqueue();
        void context_addtoPlaylist();
        void canremoveSelection(boolean f);

    }
    public interface home_interface{
        void setNullToActionMode();
        void removeSelection();
        void share_contextual();
        void delete_contextual();
        void addtoqueue_contextual();
        void addtoplaylist_contextual();
        void canremoveSelection(boolean f);

    }
    public interface open_playlist_interface{
        void setNullToActionMode();
        void removeSelection();
        void share_contextual();
        void delete_contextual();
        void addtoqueue_contextual();
        void addtoplaylist_contextual();
        void remove_from_playlist();
        void canremoveSelection(boolean f);

    }

    public Toolbar_ActionMode_Callback(artist_interface artist,String method) {
        this.artist = artist;
        this.method=method;
    }
    public Toolbar_ActionMode_Callback(home_interface home,String method) {
        this.home = home;
        this.method=method;
    }
    public Toolbar_ActionMode_Callback(open_playlist_interface open_playlist,String method) {
        this.open_playlist = open_playlist;
        this.method=method;
    }
    public Toolbar_ActionMode_Callback(album_interface album,String method) {
        this.album = album;
        this.method=method;
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if(method.equals("home") ){
            mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);//Inflate the menu over action mode
        }else if(method.equals("album")){
            mode.getMenuInflater().inflate(R.menu.contextmenu_album, menu);//Inflate the menu over action mode
        }else if(method.equals("artist")){
            mode.getMenuInflater().inflate(R.menu.contextmenu_artist, menu);//Inflate the menu over action mode
        }else if(method.equals("open_playlist")){
            mode.getMenuInflater().inflate(R.menu.contextual_menu_open_playlist, menu);//Inflate the menu over action mode
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Fragment f=null;
        if(method.equals("home")){
            home.canremoveSelection(false);
        }else if(method.equals("album")){
            album.canremoveSelection(false);
        }else if(method.equals("artist")){
            artist.canremoveSelection(false);
        }else if(method.equals("open_playlist")){
            open_playlist.canremoveSelection(false);
        }

        switch (item.getItemId()) {
            case R.id.contextual_addtoqueue:
                home.addtoqueue_contextual();
               mode.finish();//Finish action mode
               break;
            case R.id.contextual_delete:
                home.delete_contextual();
               //mode.finish();//Finish action mode
                break;
            case R.id.contextual_addtoplaylist:
                home.addtoplaylist_contextual();
                mode.finish();//Finish action mode
                break;
            case R.id.contextual_share:
                home.share_contextual();
                mode.finish();//Finish action mode
                break;
            case R.id.context_album_delete:
                album.context_delete();
                //mode.finish();//Finish action mode
                break;
            case R.id.context_album_addtoPlaylist:
                album.context_addtoPlaylist();
                mode.finish();//Finish action mode
                break;
            case R.id.context_album_AddtoQueue:
                album.context_addtoqueue();
                mode.finish();//Finish action mode
                break;
            case R.id.context_artist_delete:
                artist.context_delete();
                //mode.finish();//Finish action mode
                break;
            case R.id.context_artist_addToPlaylist:
                artist.context_addtoPlaylist();
                mode.finish();//Finish action mode
                break;
            case R.id.open_contextual_addtoqueue:
                open_playlist.addtoqueue_contextual();
                mode.finish();//Finish action mode
                break;
            case R.id.open_contextual_delete:
                open_playlist.delete_contextual();
                mode.finish();//Finish action mode
                break;
            case R.id.open_contextual_addtoplaylist:
                open_playlist.addtoplaylist_contextual();
                mode.finish();//Finish action mode
                break;
            case R.id.open_contextual_share:
                open_playlist.share_contextual();
                mode.finish();//Finish action mode
                break;
            case R.id.open_contextual_remove_from_playlist:
                open_playlist.remove_from_playlist();
                mode.finish();//Finish action mode
                break;


        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        //When action mode destroyed remove selected selections and set action mode to null
        //First check current fragment action mode
        //recyclerView_adapter.removeSelection();// remove selection
        if(method.equals("home")){
            home.canremoveSelection(true);
        }else if(method.equals("album")){
            album.canremoveSelection(true);
        }else if(method.equals("artist")){
            artist.canremoveSelection(true);
        }else if(method.equals("open_playlist")){
            open_playlist.canremoveSelection(true);
        }

        if (method.equals("home") ) {
           home.setNullToActionMode();//Set action mode null

        } else if (method.equals("album")) {
            album.setNullToActionMode();//Set action mode null
        }
        else if (method.equals("artist")) {
           artist.setNullToActionMode();//Set action mode null
        }
        else if (method.equals("open_playlist")) {
            open_playlist.setNullToActionMode();//Set action mode null
        }
    }


}