package com.androidplay.one.myplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Rahul on 11-02-2017.
 */

public class fileUtil {

    public static String getnumOfSongsForFolder(Context context,String foldername){
        String result="";
        ContentResolver resolver=context.getContentResolver();
        String _id= MediaStore.Audio.Media._ID;

        String[] proj={
               _id
        };
        String Selection=MediaStore.Audio.Media.DATA+" LIKE ? ";
        String[] args=new String[]{foldername+"%"};
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor=null;
        try {
            Log.i("hghh","try");
            musicCursor = resolver.query(musicUri, proj, Selection, args,null);
        }catch(Exception ee){
            ee.printStackTrace();
        }
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            Log.i("hghh","musicCursor.getCount()+"+musicCursor.getCount());

            result=String.valueOf(musicCursor.getCount());
        }
        try{
            musicCursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<songs> getSongsInFoldersAsync(Context context,String foldername){
        ArrayList<songs> allsonglist=new ArrayList<>();
        Log.i("llllp","fetch list");
        String[] proj={android.provider.MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                android.provider.MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ALBUM
        };


        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String where=MediaStore.Audio.Media.DATA+" LIKE ? ";
        String[] args=new String[]{foldername+"%"};
        Cursor musicCursor=null;
        try {
            Log.i("llllp","try");

            musicCursor = context.getContentResolver().query(musicUri, proj, where, args, MediaStore.Audio.Media.TITLE);
        }catch(java.lang.SecurityException e){
            e.printStackTrace();
        }
        Log.i("llllp","done");

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int albumidcolumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int datacolumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int yearcolumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.YEAR);
            int albumcolumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);


            //add songs to list
            do {
                Long albumid=musicCursor.getLong(albumidcolumn);
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisdata = musicCursor.getString(datacolumn);
                String year=musicCursor.getString(yearcolumn);
                String albumname=musicCursor.getString(albumcolumn);
                songs s=new songs(thisId, thisTitle, thisArtist,"",albumid,thisdata);
                s.setYear(year);
                s.setAlbumName(albumname);
                Log.i("foll","----------------"+thisdata);

                //----fetching album art
                Cursor cursor;
                String path;
                String _ids = MediaStore.Audio.Albums._ID;
                String albumart = MediaStore.Audio.Albums.ALBUM_ART;

                String[] projection2={ _ids,albumart};

                cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection2, "_ID=" + albumid,
                        null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                        s.setImagepath(path);
                    }
                    cursor.close();
                }
                // fetching album art end

                allsonglist.add(s);

            }
            while (musicCursor.moveToNext());
        }
        try{
            musicCursor.close();
        }catch (Exception e){e.printStackTrace();}

        return allsonglist;
    }

    public static ArrayList<Long> getSongIdsInFoldersAsync(Context context,String foldername){
        ArrayList<Long> allsonglist=new ArrayList<>();
        Log.i("llllp","fetch list");
        String[] proj={android.provider.MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media._ID,
        };

        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String where=MediaStore.Audio.Media.DATA+" LIKE ? ";
        String[] args=new String[]{foldername+"%"};
        Cursor musicCursor=null;
        try {
            Log.i("llllp","try");

            musicCursor = context.getContentResolver().query(musicUri, proj, where, args, MediaStore.Audio.Media.TITLE);
        }catch(java.lang.SecurityException e){
            e.printStackTrace();
        }
        Log.i("llllp","done");

        if(musicCursor!=null && musicCursor.moveToFirst()){
            Log.i("hjui","musicCursor!=null && musicCursor.moveToFirst()");

            //get columns
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            //add songs to list
            do {
                Long thisId = musicCursor.getLong(idColumn);
                allsonglist.add(thisId);
            }
            while (musicCursor.moveToNext());
        }
        try{
            musicCursor.close();
        }catch (Exception e){e.printStackTrace();}

        return allsonglist;
    }

    public static void deletefile(Context context,File fileOrDirectory){
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles())
                deletefile(context,child);
            fileOrDirectory.delete();
        }else if(fileOrDirectory.isFile() && fileOrDirectory.getName().endsWith("mp3")){
            removeSongFromMediaStoreWithPath(context,fileOrDirectory.getAbsolutePath());
            fileOrDirectory.delete();
        }

    }
    public static void removeSongFromMediaStoreWithPath(Context context,String path){
        ContentResolver res=context.getContentResolver();
        int i=res.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,MediaStore.Audio.Media.DATA+" = ? ",
                new String[]{String.valueOf(path)});
        Log.i("dell",i+" songs deleted from mediastre");
    }


}
