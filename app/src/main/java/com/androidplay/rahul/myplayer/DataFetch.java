package com.androidplay.rahul.myplayer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Rahul on 23-01-2017.
 */

public class DataFetch {
    private static ArrayList<songs> list;
    private static ArrayList<Long> idlist;
    private AlertDialog.Builder builder;
    private static ContentResolver resolver;
    private Context context;
    private Long playlistIdForMultipleAdd;

    public DataFetch(Context contextt) {
        this.context = contextt;
        resolver=contextt.getContentResolver();
    }

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

    public static ArrayList<Long> getSongIdsOfAlbum(Context context, Long album_id){
        idlist=new ArrayList<>();
        resolver=context.getContentResolver();
        String selection = "is_music != 0";

        if (album_id > 0) {
            selection = selection + " and album_id = " + album_id;
        }

        String[] projection = {
                MediaStore.Audio.Media._ID,

        };

        Cursor cursor = null;
        try {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = resolver.query(uri, projection, selection, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    idlist.add(id);
                }
            }

        } catch (Exception e) {
            Log.e("Media", e.toString());
        } finally {
            if (cursor != null) {
                try{cursor.close();}catch (Exception e){e.printStackTrace();}
            }
        }
        return idlist;
    }

    public static ArrayList<Long> getSongIdsOfArtist(Context context, Long artist_id){
        idlist=new ArrayList<>();
        resolver=context.getContentResolver();
        String selection = "is_music != 0";
        String Artist_id=MediaStore.Audio.Media.ARTIST_ID;
        if (artist_id > 0) {
            selection = selection + " and "+Artist_id+" = " + artist_id;
        }

        String[] projection = {
                MediaStore.Audio.Media._ID,

        };

        Cursor cursor = null;
        try {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = resolver.query(uri, projection, selection, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    idlist.add(id);
                }
            }

        } catch (Exception e) {
            Log.e("Media", e.toString());
        } finally {
            if (cursor != null) {
                try{cursor.close();}catch (Exception e){e.printStackTrace();}
            }
        }
        return idlist;
    }

    public static ArrayList<songs> getSongsOfAlbum(Context context, Long id){
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
        String Selection=MediaStore.Audio.Media.ALBUM_ID+" = ? ";
        String[] args=new String[]{String.valueOf(id)};
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
                songs song=new songs(thisId, thisTitle, thisArtist,"",albumid,thisdata);

                //----fetching album art
                Cursor cursor;
                String path;
                String _ids = MediaStore.Audio.Albums._ID;
                String albumart = MediaStore.Audio.Albums.ALBUM_ART;

                String[] projection2={ _ids,albumart};

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
            while (musicCursor.moveToNext());
        }
        try{
            musicCursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return list;

    }

    //all functions to add a list of song to playlist ..
    public void AddtoPlaylist(final ArrayList<songs> gotlist){
        resolver=context.getContentResolver();
        final ArrayList<songs> playlist_list=get_playlist(context);
        builder=new AlertDialog.Builder(context);
        //show a dialog

        CharSequence playlistts[]=new String[playlist_list.size()];
        for(int i=0;i<playlist_list.size();i++){
            String s=playlist_list.get(i).getName();
            playlistts[i]=(CharSequence) s;
        }
        builder = new AlertDialog.Builder(context);
        if(playlist_list.size()==0){
            builder.setTitle("No Playlists found");
        }else{
            builder.setTitle("Choose a Playlist");
        }

        builder.setItems(playlistts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on playlistts[which]
                playlistIdForMultipleAdd=playlist_list.get(which).getId();

                if (playlistIdForMultipleAdd.equals(0L)) {
                    // add new playlist and add songs to tht playlist
                    dialog.dismiss();
                    addnewPlaylistwithSongsAsync(gotlist);
                } else {
                    addToPaylistMultiple addtoPlaylist=new addToPaylistMultiple(gotlist.size());
                    addtoPlaylist.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,gotlist);

                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog =builder.create();
        dialog.show();

    }
    public ArrayList<songs> get_playlist(Context context){
        ArrayList<songs> playlist_list=new ArrayList<>();
        playlist_list.add(new songs(0L,"Add New Playlist",""));
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String idKey = MediaStore.Audio.Playlists._ID;
        final String nameKey = MediaStore.Audio.Playlists.NAME;
        final String songs= MediaStore.Audio.Playlists._COUNT;


        final String[] columns = { idKey, nameKey };
        final Cursor playLists = resolver.query(uri, columns, null, null, null);
        if (playLists == null) {

        }else {
            // Log a list of the playlists.
            String playListName = null;
            String playlist_id = null;

            for (boolean hasItem = playLists.moveToFirst(); hasItem; hasItem = playLists.moveToNext()) {
                playListName = playLists.getString(playLists.getColumnIndex(nameKey));
                playlist_id = playLists.getString(playLists.getColumnIndex(idKey));
                songs playlist =new songs(Long.parseLong(playlist_id),playListName,"");
                playlist_list.add(playlist);
            }
        }
        // Close the cursor.
        if (playLists != null) {
            try{ playLists.close();}catch (Exception e){e.printStackTrace();}
        }
        return playlist_list;

    }
    public void createnewplaylist(String playlistname) {
        ContentValues mInserts = new ContentValues();
        mInserts.put(MediaStore.Audio.Playlists.NAME, playlistname);
        mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        context.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
    }
    private Long findPlaylistIdByName(String name){

        Long id;
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String idKey = MediaStore.Audio.Playlists._ID;
        final String nameKey = MediaStore.Audio.Playlists.NAME;

        final String[] columns = { idKey, nameKey };
        final Cursor playLists = resolver.query(uri, columns,nameKey +" = ?", new String[]{name}, null);
        if (playLists == null) {
            return null;
        }else {
            String playlist_id = null;

            for (boolean hasItem = playLists.moveToFirst(); hasItem; hasItem = playLists.moveToNext()) {
                playlist_id = playLists.getString(playLists.getColumnIndex(idKey));
                return Long.valueOf(playlist_id);
            }
        }
        return null;
    }
    private void addnewPlaylistwithSongsAsync(final ArrayList<songs> gotSonglist){
        builder=new AlertDialog.Builder(context);
        builder.setTitle("Playlist name");
        builder.setCancelable(true);
        final EditText input = new EditText(context);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);
        builder.setPositiveButton(
                "Create",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String s=input.getText().toString();
                        createnewplaylist(s);
                        playlistIdForMultipleAdd=findPlaylistIdByName(s);
                        addToPaylistMultiple addtoPlaylist=new addToPaylistMultiple(gotSonglist.size());
                        addtoPlaylist.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,gotSonglist);

                    }
                });

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private class addToPaylistMultiple extends AsyncTask<ArrayList<songs>,Void,Void>{
        Long playlistid=new Long(playlistIdForMultipleAdd);
        //songs current_song;
        ProgressDialog progress=new ProgressDialog(context);
        int typeBar=1;        // Determines type progress bar: 0 = spinner, 1 = horizontal
        int success=0;
        int listsize;
        public addToPaylistMultiple(int listsize){
            this.listsize=listsize;
        }
        @Override
        protected void onPreExecute() {
            //current_song=con.getsong();

            progress.setMessage("PLease wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(typeBar);
            progress.setIndeterminate(false);
            progress.setMax(listsize);
            progress.setProgress(success);
            progress.show();
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(ArrayList<songs>... params) {
            ArrayList<songs> list=params[0];
            Long l;
            for(songs song:list){
                l=song.getId();
                addTracksToPlaylist(playlistid,l);
                success++;
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progress.setProgress(success);

        }
        public  String addTracksToPlaylist(final long id,Long track) {
            int count = getplaylistsize(id);
            ContentValues values ;

            values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, count + 1);
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, track);

            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);

            resolver.insert(uri, values);
            resolver.notifyChange(Uri.parse("content://media"), null);
            return "";
        }

        public int getplaylistsize(Long ids){

            int i=0;
            final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", ids);
            final String idd=MediaStore.Audio.Playlists.Members._ID;
            Cursor tracks = resolver.query(uri,new String[]{idd}, null, null, null);
            if (tracks != null) {

                while(tracks.moveToNext()){
                    i++;
                }
            }
            try{
                tracks.close();
            }catch (Exception e){e.printStackTrace();}
            return i;
        }

        @Override
        protected void onPostExecute(Void songses) {
            super.onPostExecute(songses);
            progress.dismiss();
            try{
                ((MainActivity)context).refreshFragment(2);
            }catch (Exception e){
            }
        }
    }


    //efficiently showing bitmap to imageview

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,int reqWidth, int reqHeight) {

         // First decode with inJustDecodeBounds=true to check dimensions
         final BitmapFactory.Options options = new BitmapFactory.Options();
         options.inJustDecodeBounds = true;
         BitmapFactory.decodeResource(res, resId, options);

         // Calculate inSampleSize
         options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
         options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            Log.i("bitmmp","insamplesize="+inSampleSize+" halfwidth="+halfWidth+" halfheight="+halfHeight+" reqwidth="+reqWidth+" reqheight="+reqHeight);
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (((halfHeight / inSampleSize) >= reqHeight) && ((halfWidth / inSampleSize) >= reqWidth)) {
                Log.i("bitmmp","insamplesize="+inSampleSize+" halfwidth="+halfWidth+" halfheight="+halfHeight+" reqwidth="+reqWidth+" reqheight="+reqHeight);

                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}
