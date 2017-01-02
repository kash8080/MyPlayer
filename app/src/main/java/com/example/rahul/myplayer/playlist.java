package com.example.rahul.myplayer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class playlist extends Fragment {

    final static String tag="tstng1";
    ArrayList<songs> playlist_list;
    RecyclerView rec_view;
    RecyclerView.LayoutManager mlayoutmanager;
    recycler_adapter adapter;

     ContentResolver resolver=null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.activity_playlist,container,false);
        resolver = getActivity().getContentResolver();
        playlist_list=new ArrayList<>();
        rec_view=(RecyclerView)v.findViewById(R.id.recview);
        mlayoutmanager=new LinearLayoutManager(getActivity());

        doasync inback=new doasync();
        inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        adapter=new recycler_adapter(getActivity(),playlist_list,"playlist");
        rec_view.setLayoutManager(mlayoutmanager);
        rec_view.setAdapter(adapter);

        Log.i("cccc","oncreate playlist");
        return v;

    }

   public ArrayList<songs> get_playlist(){
        playlist_list=new ArrayList<>();
       final ContentResolver resolver = getActivity().getContentResolver();
       final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
       final String idKey = MediaStore.Audio.Playlists._ID;
       final String nameKey = MediaStore.Audio.Playlists.NAME;
      final String songs= MediaStore.Audio.Playlists._COUNT;


       final String[] columns = { idKey, nameKey };
       final Cursor playLists = resolver.query(uri, columns, null, null, null);
       if (playLists == null) {
           Log.e(tag, "Found no playlists.");
       }else {

           // Log a list of the playlists.
           Log.i(tag, "Playlists:");
           String playListName = null;
           String playlist_id = null;
           String playlist_songs;
           for (boolean hasItem = playLists.moveToFirst(); hasItem; hasItem = playLists.moveToNext()) {
               playListName = playLists.getString(playLists.getColumnIndex(nameKey));
               playlist_id = playLists.getString(playLists.getColumnIndex(idKey));
               playlist_songs=getplaylistsize(playlist_id);
              songs playlist =new songs(Long.parseLong(playlist_id),playListName,playlist_songs+" songs","",0);
               playlist_list.add(playlist);

               Log.i(tag, playListName);
           }
       }
       // Close the cursor.
       if (playLists != null) {
          try{ playLists.close();}catch (Exception e){e.printStackTrace();}
       }
         return playlist_list;

   }
   public String getplaylistsize(String id){
       Long ids=Long.parseLong(id);
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
       return String.valueOf(i);
   }


    public class doasync extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            get_playlist();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.songs_list=playlist_list;
            adapter.notifyDataSetChanged();
        }
    }
}
