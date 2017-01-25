package com.androidplay.rahul.myplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by Rahul on 23-01-2017.
 */

public class Artist extends Fragment {
    ArrayList<songs> list ;
    ApplicationController con;
    ContentResolver res;
    boolean hassavedlist=false;
    RecyclerView rec_view;
    recycler_adapter rec_adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    boolean cancelled=false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        con=new ApplicationController(getActivity().getApplicationContext(),getActivity());
        View v= inflater.inflate(R.layout.activity_home,container,false);
        res=getActivity().getContentResolver();
        list=new ArrayList<>();
        Log.i("artist","on create");

       /* if(savedInstanceState!=null) {
            String s = (String) savedInstanceState.get("instancesaved");
            String can=con.currentactivityname;
            if (s != null && s.equals("true") && can.equals("artist")) {
                Log.i("dsad", "restored instance state");
                hassavedlist = true;
                list = con.currentactivitySavedList;
                Log.i("activitystate", "list.size()=" + String.valueOf(list.size()));
            }
        }
*/
        cancelled=false;
        doasync inback=new doasync();
        inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        rec_view=(RecyclerView)v.findViewById(R.id.rec_view);

        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) v.findViewById(R.id.fast_scroller);
        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(rec_view);
        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        rec_view.addOnScrollListener(fastScroller.getOnScrollListener());

        mLayoutManager=new LinearLayoutManager(getActivity());
        rec_adapter=new recycler_adapter(getActivity(),list,"artist");
        rec_view.setLayoutManager(mLayoutManager);
        rec_view.setAdapter(rec_adapter);

        return v;
    }


    public void setlist(){
        Log.i("artist","setting list artist");
        final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        final String _id = MediaStore.Audio.Artists._ID;
        final String name = MediaStore.Audio.Artists.ARTIST;
        final String artistKey = MediaStore.Audio.Artists.ARTIST_KEY;
        final String numberOfAlbums = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS;
        final String numberOfTracks = MediaStore.Audio.Artists.NUMBER_OF_TRACKS;

        Cursor cursor=null;

        final String[] columns = { _id, name, numberOfAlbums, numberOfTracks,artistKey};
        try {
            cursor = res.query(uri, columns, null,null,name);
        }catch (java.lang.SecurityException e){
            e.printStackTrace();
        }
        if(cursor!=null){
            Log.i("artist","plus");
            int idColumn = cursor.getColumnIndex(_id);
            int titleColumn = cursor.getColumnIndex(name);
            int numberOfAlbumsColumn = cursor.getColumnIndex(numberOfAlbums);
            int numberOfTracksColumn = cursor.getColumnIndex(numberOfTracks);
            int artistkeyColumn = cursor.getColumnIndex(artistKey);

            while(cursor.moveToNext() && !cancelled){
                Long id=Long.parseLong(cursor.getString(idColumn));
                String artistname=cursor.getString(titleColumn);
                String numberalbums=cursor.getString(numberOfAlbumsColumn);
                String numbertracks=cursor.getString(numberOfTracksColumn);
                String artisttkey=cursor.getString(artistkeyColumn);

                songs song =new songs(id,artistname,numberalbums,numbertracks,artisttkey);
                list.add(song);
            }
            try {
                cursor.close();
            }catch (Exception e ){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("activitystate","onsaveinstance");
        /*if(list!=null && list.size()>0) {
            outState.putString("instancesaved", "true");
            con.currentactivityname="artist";
            con.currentactivitySavedList=list;

        }*/
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("artist","on destroy");

        cancelled=true;
    }

    public class doasync extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            setlist();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!cancelled){
                Log.i("artist","adapternotified");
                rec_adapter.notifyDataSetChanged();
            }
        }
    }

}
