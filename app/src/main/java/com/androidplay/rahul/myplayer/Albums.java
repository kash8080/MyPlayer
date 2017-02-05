package com.androidplay.rahul.myplayer;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class Albums extends Fragment implements Toolbar_ActionMode_Callback.album_interface{

    String tag="alb";
    RecyclerView rec_view;
    recycler_adapter rec_adapter;
    ArrayList<songs> list=new ArrayList<>() ;
    private RecyclerView.LayoutManager mLayoutManager;
    ContentResolver res;
    int columncount=2;
    ApplicationController con;
    boolean hassavedlist=false;
    boolean cancelled=false;

    //actionmode
    public ActionMode mActionMode;
    public boolean canremoveSelection=true;
    AlertDialog.Builder builder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        con=new ApplicationController(getActivity().getApplicationContext(),getActivity());
        View v= inflater.inflate(R.layout.activity_albums,container,false);
        res=getActivity().getContentResolver();
        list=new ArrayList<>();
        Log.i("album","on create");

        /*
        if(savedInstanceState!=null) {
            String s = (String) savedInstanceState.get("instancesaved");
            if (s != null && s.equals("true") && con.albumslist!=null) {
                Log.i("album", "restored instance state");
                hassavedlist = true;
                list = con.albumslist;
                loaded=true;
                Log.i("activitystate", "list.size()=" + String.valueOf(list.size()));
            }
        }
*/

        rec_view=(RecyclerView)v.findViewById(R.id.recview_albums);

        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) v.findViewById(R.id.fast_scroller1);
        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(rec_view);
        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        rec_view.addOnScrollListener(fastScroller.getOnScrollListener());

        loaded=false;
        cancelled=false;
        doasync inback=new doasync();
        inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        columncount=(int)this.getResources().getInteger(R.integer.columncount);
        mLayoutManager=new GridLayoutManager(getActivity(),columncount);
        rec_adapter=new recycler_adapter(getActivity(),list,"album");

        rec_view.setLayoutManager(mLayoutManager);
        rec_view.setAdapter(rec_adapter);

        implementRecyclerViewListeners();

        Log.i("cccc","on create album");
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelled=true;
    }

    //need to do this in background
    public void setlist(){
        list.clear();
        loaded=false;
        Log.i("album","setting list");
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
                   null, album_name);
       }catch (java.lang.SecurityException e){
           e.printStackTrace();
       }
        Log.i(tag,"cursor loaded");
        if(cursor!=null){
            Log.i(tag,"cursor!=null");
            cursor.moveToFirst();
            do{
                Log.i(tag, "--");
                Long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(_id)));
                String name = cursor.getString(cursor.getColumnIndex(album_name));
                String artistt = cursor.getString(cursor.getColumnIndex(artist));
                String pic = cursor.getString(cursor.getColumnIndex(albumart));
                int total = Integer.parseInt(cursor.getString(cursor.getColumnIndex(tracks)));
                songs song = new songs(id, name, artistt, pic, total);
                list.add(song);
            }while(cursor.moveToNext() && !cancelled);

        try {
            cursor.close();
        }catch (Exception e ){
            e.printStackTrace();
        }

    }

        Log.i("album","setlist done");
    }

    boolean loaded=false;
    public class doasync extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            setlist();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loaded=true;
            if(!cancelled){
                Log.i("album","adapternotified");
                rec_adapter.notifyDataSetChanged();
            }
        }
    }


    MainActivity mainact;
    @Override
    public void onAttach(Context context) {
        mainact=(MainActivity)context;
        super.onAttach(context);
    }


    //action mode methods
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
                    new Toolbar_ActionMode_Callback(this,"album"));

            mainact.lockdrawer();
            rec_adapter.mActionmodeset(true);

        }else if (!hasCheckedItems && mActionMode != null)

            // there no selected items, finish the actionMode
            mActionMode.finish();
        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(rec_adapter.getSelectedCount()) + " selected");
    }
    //Set action mode null after use
    @Override
    public void setNullToActionMode() {
        if (mActionMode != null) {
            Log.i("animt","null to action mode");

            mActionMode = null;
            mainact.releasedrawer();
            removeSelection();
            rec_adapter.mActionmodeset(false);
        }
    }
    @Override
    public void removeSelection(){
        if(canremoveSelection){
            rec_adapter.removeSelection();
        }
    }
    @Override
    public void context_delete(){
        Log.i("ationmode","album delete");
        builder=new AlertDialog.Builder(getActivity());
        builder.setMessage("are you sure you want to delete "+String.valueOf(rec_adapter.getSelectedCount())+" selected songs");
        builder.setCancelable(false) ;
        builder.setPositiveButton(
                "yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        canremoveSelection=true;
                        rec_adapter.context_albumdelete();
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
    @Override
    public void context_addtoPlaylist(){
        Log.i("ationmode","album delete");
       rec_adapter.album_addtoplaylist_contextual();
    }

    @Override
    public void canremoveSelection(boolean g) {
        canremoveSelection=g;
    }
    @Override
    public void context_addtoqueue(){
        Log.i("ationmode","album delete");
        rec_adapter.album_addtoqueue_contextual();
    }

}
