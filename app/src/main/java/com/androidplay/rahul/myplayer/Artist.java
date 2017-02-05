package com.androidplay.rahul.myplayer;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by Rahul on 23-01-2017.
 */

public class Artist extends Fragment implements Toolbar_ActionMode_Callback.artist_interface{
    ArrayList<songs> list=new ArrayList<>() ;
    ApplicationController con;
    ContentResolver res;
    boolean hassavedlist=false;
    RecyclerView rec_view;
    private recycler_adapter rec_adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    boolean cancelled=false;

    //actionmode
    public ActionMode mActionMode;
    public boolean canremoveSelection=true;
    AlertDialog.Builder builder;

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

        implementRecyclerViewListeners();
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
                    new Toolbar_ActionMode_Callback(this,"artist"));

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
            removeSelections();
            rec_adapter.mActionmodeset(false);
        }
    }
    @Override
    public void removeSelections(){
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
                        rec_adapter.context_artistdelete();
                        Toast.makeText(Artist.this.getActivity(),"deleting",Toast.LENGTH_LONG).show();
                        removeSelections();
                    }
                });

        builder.setNegativeButton(
                "no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        canremoveSelection=true;
                        removeSelections();
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
        Log.i("artist","album delete");
        if(rec_adapter==null){
            Log.i("artist","rec adapter is null");

        }else{
            rec_adapter.context_artistaddtoPlaylist();

        }
    }

    @Override
    public void canremoveSelection(boolean g) {
        canremoveSelection=g;
    }


    @Override
    public void onResume() {

        super.onResume();
    }
}
