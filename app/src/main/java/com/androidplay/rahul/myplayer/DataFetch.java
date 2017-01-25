package com.androidplay.rahul.myplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Rahul on 23-01-2017.
 */

public class DataFetch {
    private static ArrayList<songs> list;
    static ContentResolver resolver;

    public static ArrayList<songs> getPlaylist(Context context, Long playlist_id){
        list=new ArrayList<>();
        resolver=context.getContentResolver();

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);
        String[] projection = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.ALBUM_ID,
                MediaStore.Audio.Playlists.Members.DATA,

        };
        Cursor tracks = resolver.query(uri,projection, null, null, null);
        if(tracks!=null){
            while(tracks.moveToNext()) {
                String name =tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                Long id=Long.parseLong(tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
                String artist=tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                Long albumid=tracks.getLong(tracks.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String thisdata=tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA));

                songs song =new songs(id,name,artist,"",albumid,thisdata);
                //----fetching album art
                    Cursor cursor;
                    String path;
                    String _id = MediaStore.Audio.Albums._ID;
                    String albumart = MediaStore.Audio.Albums.ALBUM_ART;

                    String[] projection2={ _id,albumart};

                    cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection2, "_ID=" + albumid,
                            null, null);

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                            song.setImagepath(path);
                        }
                        cursor.close();
                    }
                // fetching album art end
                list.add(song);
            }
            try{
                tracks.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return list;

    }

    public static ArrayList<songs> getSongsOfArtist(Context context, String Selection,String[] args){
        list=new ArrayList<>();
        resolver=context.getContentResolver();

        String name=android.provider.MediaStore.Audio.Media.TITLE;
        String album_id=MediaStore.Audio.Media.ALBUM_ID;
        String _id= MediaStore.Audio.Media._ID;
        String artist=android.provider.MediaStore.Audio.Media.ARTIST;
        String data=MediaStore.Audio.Media.DATA;

        Log.i("llllp","fetch list");
        String[] proj={
                name,album_id,_id,artist,data
        };
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor=null;
        try {
            Log.i("llllp","try");

            musicCursor = resolver.query(musicUri, proj, Selection, args, name);
        }catch(Exception ee){
            ee.printStackTrace();
        }
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns

            int titleColumn = musicCursor.getColumnIndex(name);
            int idColumn = musicCursor.getColumnIndex(_id);
            int artistColumn = musicCursor.getColumnIndex(artist);
            int datacolumn = musicCursor.getColumnIndex(data);


            //add songs to list
            do {
                Long albumid=musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisdata = musicCursor.getString(datacolumn);

                list.add(new songs(thisId, thisTitle, thisArtist,"",albumid,thisdata));

            }
            while (musicCursor.moveToNext());
        }
        try{
            musicCursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

       return list;

    }

}
