package com.example.rahul.myplayer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class open_playlist extends AppCompatActivity implements ApplicationController.informactivity ,recycler_adapter.playlist_data,View.OnClickListener{

    RecyclerView rec_view;
    RecyclerView.LayoutManager mlayoutmanager;
    recycler_adapter adapter;
    ContentResolver resolver=null;
    ArrayList<songs> list;
    Long playlist_id;
    ApplicationController con;
    String tag="tstngss";
    String tag1="tstngsss";
    String method,album_art=null;
    Long album_id;
    boolean listset=false;
    Toolbar toolbar,desc_toolbar;
    String title="";
    ImageView image,over_image;
    TextView over_title,over_artist,numberofsongs;
    String playall="false";
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Bitmap bitmap;
    FloatingActionButton fab1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(tag,"on create ");
        con=new ApplicationController(this.getApplicationContext(),this);

        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
        }else{
            if(con.needforpermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
                startActivity(new Intent(this, MainActivity.class));
                //finish called to stop further proccess of this activity
                finish();
            }else{
                super.onCreate(savedInstanceState);
            }

        }

        inititalise();
        doasync inback=new doasync();

        try{
            method=getIntent().getStringExtra("method");
            if(method.equals("playlist")){
                playlist_id=getIntent().getLongExtra("playlist_id",0);
                title=getIntent().getStringExtra("playlist_name");
                get_playlist();
            }else if(method.equals("album")) {
                album_art=getIntent().getStringExtra("album_art");
                title=getIntent().getStringExtra("album_name");
                playall=getIntent().getStringExtra("album_playall");
                album_id=getIntent().getLongExtra("album_id",0);
                getalbum();
            }
        }catch (Exception e){}

        over_title.setText(title);
        String string=list.size()+" songs";
        numberofsongs.setText(string);

        if(method.equals("playlist")){
            inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,2);
            adapter=new recycler_adapter(this,list,"open_playlist");
        }else{
            if(playall.equals("true")){
                adapter=new recycler_adapter(this,list,"open_album_true");
            }else{
                adapter=new recycler_adapter(this,list,"open_album");
            }
            rec_view.setAdapter(adapter);
        }

        Log.i(tag,"adapter done in oncreate");
        rec_view.setLayoutManager(mlayoutmanager);
        Log.i(tag,"setting adapter");
        rec_view.setAdapter(adapter);

        refreshview();

        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);

        fab1.setOnClickListener(this);


        appBarLayout= (AppBarLayout) findViewById(R.id.MyAppbar);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollrange=-1;
            boolean isShow=false;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.i("ddddff","vertical offset="+String.valueOf(verticalOffset));
                if (scrollrange == -1) {
                    collapsingToolbarLayout.setTitle(" ");
                    scrollrange = appBarLayout.getTotalScrollRange();
                    Log.i("ddddff","scroll range="+String.valueOf(scrollrange));

                }
                if (scrollrange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(title);
                    numberofsongs.setTextColor(Color.TRANSPARENT);
                    //collapsingToolbarLayout.setCollapsedTitleTextColor(Color.GRAY);
                    isShow = true;
                } else if(isShow) {
                    numberofsongs.setTextColor(Color.WHITE);
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }
    public void refreshview(){
        Log.i("lkll","pop1");
        if(album_art!=null){
            bitmap = BitmapFactory.decodeFile(album_art);
            if(bitmap!=null){
                over_image.setVisibility(View.VISIBLE);
                over_image.setImageBitmap(bitmap);
                image.setImageBitmap(bitmap);
            }else{
                Log.i("lkll","pop");
                over_image.setVisibility(View.INVISIBLE);
            }
        }else{
            Log.i("lkll","pop2");
            over_image.setVisibility(View.INVISIBLE);
        }
        // for getting colors from bitmap  using pallete lbrary
        if(bitmap!=null) {
            Palette palette = Palette.from(bitmap).generate();
            Palette.Swatch swatch = palette.getDarkMutedSwatch();
            try {
                desc_toolbar.setBackgroundColor(swatch.getRgb());
            } catch (Exception e) {

            }
        }
    }
    public void inititalise(){
        setContentView(R.layout.activity_open_playlist);

        rec_view=(RecyclerView)findViewById(R.id.rec_view2);
        mlayoutmanager=new LinearLayoutManager(this);

        toolbar=(Toolbar)findViewById(R.id.MyToolbar);
        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
        image=(ImageView)findViewById(R.id.album_image);
        desc_toolbar=(Toolbar)findViewById(R.id.desc_bar);
        over_image=(ImageView)findViewById(R.id.over_image);
        over_title=(TextView)findViewById(R.id.overtitle);
        over_artist=(TextView)findViewById(R.id.over_artist);
        numberofsongs=(TextView)findViewById(R.id.numberofsongs);
        fab1=(FloatingActionButton)findViewById(R.id.fab1);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        list=new ArrayList<>();
        resolver = getContentResolver();
    }

    public void getalbum(){
        String selection = "is_music != 0";

        if (album_id > 0) {
            selection = selection + " and album_id = " + album_id;
        }

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        final String sortOrder = MediaStore.Audio.Media.TITLE ;

        Cursor cursor = null;
        try {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = resolver.query(uri, projection, selection, null, sortOrder);
            if (cursor != null) {
                Log.i(tag1,"nnnnnnnnnnn cursor!=null");
                while (cursor.moveToNext()) {
                    Log.i(tag1,"move to next");
                   String name=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    //song.setDuration(cursor.getLong(4));
                    String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    //String path=cursor.getString(2);
                    //song.setAlbumId(cursor.getLong(6));
                    long id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    songs song =new songs(id,name,artist,"",album_id);
                    list.add(song);Log.i(tag1,"song added");
                    Log.i(tag1,name+" "+artist);
                }
            }

        } catch (Exception e) {
            Log.i(tag1,"catch");
            Log.e("Media", e.toString());
        } finally {
            if (cursor != null) {
                try{cursor.close();}catch (Exception e){e.printStackTrace();}
            }
        }
        Log.i(tag1,"list: "+list.size());
    }
    public ArrayList<songs> get_playlist(){
            Log.i(tag,"getting playlistg :"+playlist_id);

        songs song;Log.i(tag,"--");
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);


        Log.i(tag,"--");
        String[] projection = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.ALBUM_ID


        };
        Cursor tracks = resolver.query(uri,projection, null, null, null);Log.i(tag,"--");
            if(tracks!=null){
                Log.i(tag,"not null");
                while(tracks.moveToNext()) {
                    Log.i(tag,"ccc");
                    String name =tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                    Long id=Long.parseLong(tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
                    String artist=tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                    Long albumid=tracks.getLong(tracks.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                    song =new songs(id,name,artist,"",albumid);
                    Log.i(tag,"--");
                    list.add(song);

                }
                try{Log.i(tag,"try");tracks.close();}catch (Exception e){e.printStackTrace();}
            }

        Log.i(tag,"returning list of size"+list.size());
        setalbumartfromsongs();
        return list;

    }

    public void setalbumartfromsongs(){
        songs current;
        Cursor cursor;

        final String _id = MediaStore.Audio.Albums._ID;
        final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
        String[] projection={ _id,albumart};

        String path=null;

        for(int i=0;i<list.size();i++){

            current=list.get(i);
            try {
                cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, "_ID=" + current.getAlbum_id(),
                        null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                        current.setImagepath(path);
                       if(path!=null){
                           album_art=path;
                           return;
                       }
                    }
                    cursor.close();
                }
            }catch (Exception e){Log.i("aaaa","fffff");}

        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        listset=false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        switch (id){

            case android.R.id.home:{
                finish();
                return true;
            }
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void playnextsong() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void updateprofileimage() {

    }

    @Override
    public Long getplaylist_id() {
        return playlist_id;
    }

    public void play_all_songs(){
        con.setMylist(list,"open_playlist",false);
        con.playsong(0);
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fab1){
            play_all_songs();
        }
    }
    public class doasync extends AsyncTask<Integer,Void,Void>{
        int i;
        @Override
        protected Void doInBackground(Integer... integers) {
            i=integers[0];
            if(i==0){
                getalbum();
            }else if(i==1){
                get_playlist();
            }else if(i==2){
                setalbumartfromsongs();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //if album or after downloading images of playlist
            //rec_view.setAdapter(adapter);
            refreshview();
            super.onPostExecute(aVoid);
        }
    }

}



