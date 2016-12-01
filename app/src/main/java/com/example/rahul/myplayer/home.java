package com.example.rahul.myplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


//import com.example.rahul.myplayer.MyService.MusicBinder;

public class home extends Fragment
        {
    Cursor cursor;
    String tag="tstnn";
    //for service
    //private MyService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    ApplicationController con;
    private boolean servicelistset =false;
    //

    //
    RecyclerView rec_view;
    recycler_adapter rec_adapter;
    ArrayList<songs> list ;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean paused=true, playbackPaused=true;
    ContentResolver musicResolver;

    public home() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(tag,"oncreateview");
        View v=inflater.inflate(R.layout.activity_home,container,false);
        list =new ArrayList<>();
        rec_view=(RecyclerView)v.findViewById(R.id.rec_view);
        musicResolver= getActivity().getContentResolver();

        con=new ApplicationController(getActivity().getApplicationContext(),getActivity());

        //-----------------getsonglist();
        list=con.getAllsonglist();



        mLayoutManager = new LinearLayoutManager(getActivity());
        rec_view.setLayoutManager(mLayoutManager);
        rec_adapter=new recycler_adapter(getActivity(),list,"allsongs");
        rec_view.setAdapter(rec_adapter);



        //if no list set then set all songs list to the queue
        if(con.getlist()==null){
           con.setMylist(list,"song",false);

        }else if(con.getlist().size()<=0){
            con.setMylist(list,"song",false);
        }

        Log.i(tag,"-----------------");
        //con.getService();

        Log.i(tag,"got service");




        Log.i(tag,"musicSrv isnull "+con.isnull());
        Log.i(tag,"oncreateview done");
                if(con.getsong()!=null) {
                    Log.i("llll", "not null");
                    Log.i("llll", con.getsong().getName());
                    Log.i("llll", con.getsong().getArtist());
                }
        return v;


    }

    @Override
    public void onStart() {

        super.onStart();
        Log.i(tag,"onstart");
    }
    public void getsonglist() {
        Log.i(tag,"getsonglist");
        String[] proj={android.provider.MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media._ID,android.provider.MediaStore.Audio.Media.ARTIST};
            //using mediaplayer



        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,proj, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns

            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);


            //add songs to list
            do {
                Long albumid=musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);

                list.add(new songs(thisId, thisTitle, thisArtist,"",albumid));

            }
            while (musicCursor.moveToNext());
        }
        try{

        musicCursor.close();}catch (Exception e){e.printStackTrace();}
            Collections.sort(list, new Comparator<songs>(){
                public int compare(songs a, songs b){
                    return a.getName().compareTo(b.getName());
                }
            });


        Log.i(tag,"returning song list");

}

    @Override
    public void onPause() {
        Log.i(tag,"onpause");
        super.onPause();
        paused=true;
    }

    @Override
    public void onResume() {

        Log.i(tag,"onresume");
        super.onResume();
        if(paused){
            paused=false;
        }
    }

    @Override
    public void onStop() {
        Log.i(tag,"onstop");
        servicelistset=false;
        super.onStop();

    }

    public void onDestroy() {
        servicelistset=false;
        Log.i(tag,"ondestroy");
        super.onDestroy();
    }


}
