package com.example.rahul.myplayer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


//import com.example.rahul.myplayer.MyService.MusicBinder;

public class home extends Fragment{
    public boolean canremoveSelection=true;
    String tag="tstnn";
    ApplicationController con;
    RecyclerView rec_view;
    recycler_adapter rec_adapter;
    ArrayList<songs> list ;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean paused=true;
    ContentResolver musicResolver;
    public ActionMode mActionMode;
    AlertDialog.Builder builder;

    public home() {

    }
    MainActivity mainact;
    @Override
    public void onAttach(Context context) {
        mainact=(MainActivity)context;
        super.onAttach(context);
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
        implementRecyclerViewListeners();

        return v;
    }

    public void implementRecyclerViewListeners(){
        rec_view.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rec_view, new RecyclerClick_Listener() {
           @Override
           public void onClick(View view, int position) {
               //Log.i("contxt","home recycler listener on single tap");
               //If ActionMode not null select item
               Log.i("clickedd","home on click");

               if (mActionMode != null)
                    onListItemSelect(position);
           }

             @Override
            public void onLongClick(View view, int position) {
                 //Log.i("contxt","home recycler listener on long tap");
                 Log.i("clickedd","home on long click");

                 //Select item on long click
                onListItemSelect(position);
              }
            }));
    }

    //List item select method
    private void onListItemSelect(int position) {
        rec_adapter.toggleSelection(position);//Toggle the selection

        boolean hasCheckedItems = rec_adapter.getSelectedCount() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null){
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(
                    new Toolbar_ActionMode_Callback(getActivity(),rec_adapter,con.getAllsonglist()));

            mainact.lockdrawer();
            rec_adapter.mActionmodeset(true);

             //to change status bar colour in action mode
       /* if (Build.VERSION.SDK_INT >= 21) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.rgb(69,90,100));
        }*/
        }else if (!hasCheckedItems && mActionMode != null)

            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(rec_adapter.getSelectedCount()) + " selected");
        }
    //Set action mode null after use
    public void setNullToActionMode() {
        if (mActionMode != null) {
            Log.i("animt","null to action mode");

            mActionMode = null;
            mainact.releasedrawer();
            removeSelection();
            rec_adapter.mActionmodeset(false);
       /* if (Build.VERSION.SDK_INT >= 21) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.rgb(38,50,56));
        }*/
        }
        }

    public void addtoplaylist_contextual(){
        Log.i("contxt1","add to playlist");
        rec_adapter.addtoplaylist_contextual();
    }
    public void addtoqueue_contextual(){
        Log.i("contxt1","add to queue");
        rec_adapter.addtoqueue_contextual();
    }
    public void delete_contextual(){
        Log.i("contxt1","delete");
        builder=new AlertDialog.Builder(getActivity());
        builder.setMessage("are you sure you want to delete "+String.valueOf(rec_adapter.getSelectedCount())+" selected songs");
        builder.setCancelable(false) ;
        builder.setPositiveButton(
                "yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        canremoveSelection=true;
                        rec_adapter.delete_contextual();
                        removeSelection();
                    }
                });

        builder.setNegativeButton(
                "no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        canremoveSelection=true;
                        removeSelection();
                        if(mActionMode!=null) {
                            mActionMode.finish();
                        }
                        }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
    public void removeSelection(){
        if(canremoveSelection){
            rec_adapter.removeSelection();
        }
    }
}
