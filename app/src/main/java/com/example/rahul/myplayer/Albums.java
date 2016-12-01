package com.example.rahul.myplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Albums extends Fragment {

    String tag="albumss";
    RecyclerView rec_view;
    recycler_adapter rec_adapter;
    ArrayList<songs> list ;
    private RecyclerView.LayoutManager mLayoutManager;
    ContentResolver res;
    int columncount=2;

    @Override
    public void onResume() {
        Log.i("cccc","onresume albums");
        super.onResume();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.activity_albums,container,false);
        res=getActivity().getContentResolver();
        list=new ArrayList<>();
        setlist();
        rec_view=(RecyclerView)v.findViewById(R.id.recview_albums);

        columncount=(int)this.getResources().getInteger(R.integer.columncount);
        mLayoutManager=new GridLayoutManager(getActivity(),columncount);
        rec_adapter=new recycler_adapter(getActivity(),list,"album");

        rec_view.setLayoutManager(mLayoutManager);
        rec_view.setAdapter(rec_adapter);

        Log.i("cccc","on create album");
        return v;
    }


    public void setlist(){
        Log.i(tag,"setting list");
        final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        final String _id = MediaStore.Audio.Albums._ID;
        final String album_name = MediaStore.Audio.Albums.ALBUM;
        final String artist = MediaStore.Audio.Albums.ARTIST;
        final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
        final String tracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS;

        final String[] columns = { _id, album_name, artist, albumart, tracks };
        Cursor cursor = res.query(uri, columns, null,
                null,MediaStore.Audio.Albums.ALBUM);
        Log.i(tag,"cursor loaded");
    if(cursor!=null){Log.i(tag,"cursor!=null");
        while(cursor.moveToNext()){
            Log.i(tag,"--");
            Long id=Long.parseLong(cursor.getString(cursor.getColumnIndex(_id))); Log.i(tag,"--");
            String name=cursor.getString(cursor.getColumnIndex(album_name));; Log.i(tag,"--");
            String artistt=cursor.getString(cursor.getColumnIndex(artist));; Log.i(tag,"--");
            String pic=cursor.getString(cursor.getColumnIndex(albumart));; Log.i(tag,"--");
            int total=Integer.parseInt(cursor.getString(cursor.getColumnIndex(tracks)));; Log.i(tag,"--");

            songs song =new songs(id,name,artistt,pic,total); Log.i(tag,"--");
             list.add(song); Log.i(tag,"--");
        }try {
            cursor.close();
        }catch (Exception e ){e.printStackTrace();}

    }
        Log.i(tag,"setlist done");
    }

}
