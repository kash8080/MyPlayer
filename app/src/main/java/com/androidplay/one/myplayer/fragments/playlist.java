package com.androidplay.one.myplayer.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.activities.MainActivity;
import com.androidplay.one.myplayer.recycler_adapter;
import com.androidplay.one.myplayer.songs;

import java.util.ArrayList;

public class playlist extends Fragment {

    private final static String tag="tstng1";
    private ArrayList<songs> playlist_list=new ArrayList<>();
    private RecyclerView rec_view;
    private RecyclerView.LayoutManager mlayoutmanager;
    private recycler_adapter adapter;
    public Context context;
    private ContentResolver resolver=null;
    private boolean cancelled=false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("rtgh", "createview:");

        View v=inflater.inflate(R.layout.activity_playlist,container,false);
        resolver = context.getContentResolver();
        playlist_list=new ArrayList<>();
        rec_view=(RecyclerView)v.findViewById(R.id.recview);
        mlayoutmanager=new LinearLayoutManager(getActivity());
        adapter=new recycler_adapter(getActivity(),playlist_list,"playlist");
        rec_view.setLayoutManager(mlayoutmanager);
        rec_view.setAdapter(adapter);

        Log.i("cccc","oncreate playlist");

        return v;

    }

    @Override
    public void onAttach(Context context) {
        Log.i("rtgh", "onattach:");
        this.context=context;
        ((MainActivity)context).setPlaylistfragment(this);
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        Log.i("rtgh", "onresume:");
        super.onResume();
        get_playlist();
        adapter.songs_list=playlist_list;
        adapter.notifyDataSetChanged();
       // refreshview();
    }

    public void refreshview(){
        Log.i("rtgh","refresh view");

        if(getActivity()!=null) {
            doasync inback = new doasync();
            inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onDestroyView() {
        Log.i("rtgh","destroy");
        super.onDestroyView();
        //cancelled=true;
    }

    private ArrayList<songs> get_playlist(){
        Log.i("wesd", "getplaylist:");

        ArrayList<songs> playlist_list=new ArrayList<>();
       final ContentResolver resolver = getActivity().getContentResolver();
       final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
       final String idKey = MediaStore.Audio.Playlists._ID;
       final String nameKey = MediaStore.Audio.Playlists.NAME;

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
               if(cancelled){
                   return playlist_list;
               }
               playListName = playLists.getString(playLists.getColumnIndex(nameKey));
               playlist_id = playLists.getString(playLists.getColumnIndex(idKey));
               playlist_songs=getplaylistsize(playlist_id);
              songs playlist =new songs(Long.parseLong(playlist_id),playListName,playlist_songs+" tracks","",0);
               playlist_list.add(playlist);

               Log.i(tag, playListName);
           }
       }
       // Close the cursor.
       if (playLists != null) {
          try{ playLists.close();}catch (Exception e){e.printStackTrace();}
       }
       this.playlist_list=playlist_list;
       return playlist_list;

   }
   private String getplaylistsize(String id){
       ContentResolver resolver = context.getContentResolver();
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


    private class doasync extends AsyncTask<Void,Void,Void>{
        ArrayList<songs> list;
        @Override
        protected Void doInBackground(Void... voids) {
            list=get_playlist();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!cancelled) {
                adapter.songs_list=playlist_list;
                adapter.notifyDataSetChanged();
            }
        }
    }
}
