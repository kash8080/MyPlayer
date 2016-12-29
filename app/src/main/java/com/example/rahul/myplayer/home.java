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

public class home extends Fragment{

    String tag="tstnn";
    ApplicationController con;
    RecyclerView rec_view;
    recycler_adapter rec_adapter;
    ArrayList<songs> list ;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean paused=true;
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



        //if no list set then set current songs list to the queue
        if(con.getlist()==null){
           con.setMylist(list,"song",false);

        }else if(con.getlist().size()<=0){
            con.setMylist(list,"song",false);
        }

        return v;
    }

}
