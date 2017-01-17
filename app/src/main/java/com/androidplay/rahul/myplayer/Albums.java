package com.androidplay.rahul.myplayer;

import android.content.ContentResolver;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class Albums extends Fragment {

    String tag="albumss";
    RecyclerView rec_view;
    recycler_adapter rec_adapter;
    ArrayList<songs> list ;
    private RecyclerView.LayoutManager mLayoutManager;
    ContentResolver res;
    int columncount=2;
    ApplicationController con;
    boolean hassavedlist=false;
    boolean cancelled=false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        con=new ApplicationController(getActivity().getApplicationContext(),getActivity());
        View v= inflater.inflate(R.layout.activity_albums,container,false);
        res=getActivity().getContentResolver();
        list=new ArrayList<>();

        if(savedInstanceState!=null) {
            String s = (String) savedInstanceState.get("instancesaved");
            if (s != null && s.equals("true")) {
                Log.i("dsad", "restored instance state");
                hassavedlist = true;
                list = con.currentactivitySavedList;
                Log.i("activitystate", "list.size()=" + String.valueOf(list.size()));
            }
        }


        rec_view=(RecyclerView)v.findViewById(R.id.recview_albums);

        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) v.findViewById(R.id.fast_scroller1);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(rec_view);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        rec_view.addOnScrollListener(fastScroller.getOnScrollListener());


        columncount=(int)this.getResources().getInteger(R.integer.columncount);
        mLayoutManager=new GridLayoutManager(getActivity(),columncount);
        rec_adapter=new recycler_adapter(getActivity(),list,"album");

        rec_view.setLayoutManager(mLayoutManager);
        rec_view.setAdapter(rec_adapter);
        if(!hassavedlist){
            //setlist();
            doasync inback=new doasync();
            inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        Log.i("cccc","on create album");
        return v;
    }

    @Override
    public void onResume() {
        Log.i("dsad","on resume album");

        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelled=true;
    }

    //need to do this in background
    public void setlist(){
            Log.i("dsad","setting list");
            final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            final String _id = MediaStore.Audio.Albums._ID;
            final String album_name = MediaStore.Audio.Albums.ALBUM;
            final String artist = MediaStore.Audio.Albums.ARTIST;
            final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
            final String tracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS;

            final String[] columns = { _id, album_name, artist, albumart, tracks };
            Cursor cursor=null;
           try {
               cursor = res.query(uri, columns, null,
                       null, MediaStore.Audio.Albums.ALBUM);
           }catch (java.lang.SecurityException e){
               e.printStackTrace();
           }
            Log.i(tag,"cursor loaded");
            if(cursor!=null){
                Log.i(tag,"cursor!=null");
                while(cursor.moveToNext() && !cancelled){
                Log.i(tag,"--");
                Long id=Long.parseLong(cursor.getString(cursor.getColumnIndex(_id))); Log.i(tag,"--");
                String name=cursor.getString(cursor.getColumnIndex(album_name));; Log.i(tag,"--");
                String artistt=cursor.getString(cursor.getColumnIndex(artist));; Log.i(tag,"--");
                String pic=cursor.getString(cursor.getColumnIndex(albumart));; Log.i(tag,"--");
                int total=Integer.parseInt(cursor.getString(cursor.getColumnIndex(tracks)));; Log.i(tag,"--");

                songs song =new songs(id,name,artistt,pic,total); Log.i(tag,"--");
                list.add(song); Log.i(tag,"--");
            }
            try {
                cursor.close();
            }catch (Exception e ){
                e.printStackTrace();
            }

        }

            Log.i(tag,"setlist done");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("activitystate","onsaveinstance");
        if(list!=null && list.size()>0) {
            outState.putString("instancesaved", "true");
            con.currentactivitySavedList=list;

        }
        super.onSaveInstanceState(outState);
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
                Log.i("dsad","adapternotified");
                //rec_adapter.notifyDataSetChanged();
            }
        }
    }
}
