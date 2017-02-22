package com.androidplay.one.myplayer.activities;

import android.Manifest;
import android.animation.Animator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.helper_classes.ThemeHelper;
import com.androidplay.one.myplayer.recycler_adapter;
import com.androidplay.one.myplayer.songs;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by Rahul on 24-01-2017.
 */

public class ChooseArtistAlbum extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recview;
    RecyclerView.LayoutManager mlayoutManager;
    recycler_adapter adapter;
    ArrayList<songs> list;
    ApplicationController con;
    Context context;
    String artistname;
    ContentResolver res;
    int columncount;
    FloatingActionButton fab;
    LinearLayout bottomslide;
    ImageView main_backgroundimage;

    SharedPreferences sharedPref;
    String theme_no;
    Boolean dark;
    ThemeHelper themeHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context=this;
        Log.i("llllp", "oncreate");
        con = new ApplicationController(this.getApplicationContext(), this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        theme_no=sharedPref.getString("THEME_LIST","1") ;
        dark = sharedPref.getBoolean("check", false);

        themeHelper=new ThemeHelper(this);
        themeHelper.setthemecolours();

        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
        }else{
            if(con.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                startActivity(new Intent(this, PermissionActivity.class));
                //finish called to stop further proccess of this activity
                finish();
                return;
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
            }else{
                super.onCreate(savedInstanceState);
            }
        }

        artistname=getIntent().getStringExtra("artistname");
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_chooseartistalbum);

        list=new ArrayList<>();
        res=getContentResolver();
        initialise();
        themeHelper.setthemeAndBackground(main_backgroundimage,toolbar);

        getalbums();

        toolbar.setBackgroundColor(con.getPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(artistname);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        columncount=(int)this.getResources().getInteger(R.integer.columncount);
        mlayoutManager=new GridLayoutManager(this,columncount);
        adapter=new recycler_adapter(this,list,"album");

        recview.setLayoutManager(mlayoutManager);
        recview.setAdapter(adapter);

        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller)findViewById(R.id.fast_scroller3);
        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(recview);
        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recview.addOnScrollListener(fastScroller.getOnScrollListener());
        fastScroller.setVisibility(View.INVISIBLE);

        bottomslide.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(context, Now_playing.class);
                intent2.putExtra("artistname", artistname);
                intent2.putExtra("method", "artistsongs");
                startActivity(intent2);
            }
        });

    }
    private void initialise(){
        toolbar=(Toolbar)findViewById(R.id.now_toolbar);
        recview=(RecyclerView)findViewById(R.id.now_recview);
        bottomslide=(LinearLayout)findViewById(R.id.bottom_slide);
        fab=(FloatingActionButton)findViewById(R.id.Artist_allsongs);
        main_backgroundimage = (ImageView) findViewById(R.id.main_background_image);

    }


    public void getalbums(){
        list.clear();
        Log.i("artistadaptr","setting list choose album");

        final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        final String _id = MediaStore.Audio.Albums._ID;
        final String album_name = MediaStore.Audio.Albums.ALBUM;
        final String artist = MediaStore.Audio.Albums.ARTIST;
        final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
        final String tracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS;

        final String[] columns = { _id, album_name, artist, albumart, tracks };
        String selection=artist +" = ? ";
        String[] args=new String[]{artistname};
        Cursor cursor=null;
        try {
            cursor = res.query(uri, columns, selection,
                    args,null);
        }catch (java.lang.SecurityException e){
            e.printStackTrace();
            Log.i("artistadaptr",e.toString());

        }

        int size=cursor.getCount();
        if(size==0){
            Log.i("artistadaptr","cursor==null or no album");
            Intent intent2 = new Intent(context, Now_playing.class);
            intent2.putExtra("artistname",artistname);
            intent2.putExtra("method","artistsongs");
            context.startActivity(intent2);
        }
        if(cursor!=null && cursor.moveToFirst()){
            do{            Log.i("artistadaptr","new album found");


                Long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(_id)));
                String name = cursor.getString(cursor.getColumnIndex(album_name));
                String artistt = cursor.getString(cursor.getColumnIndex(artist));
                String pic = cursor.getString(cursor.getColumnIndex(albumart));
                int total = Integer.parseInt(cursor.getString(cursor.getColumnIndex(tracks)));
                songs song = new songs(id, name, artistt, pic, total);
                list.add(song);
            }while(cursor.moveToNext());

            try {
                cursor.close();
            }catch (Exception e ){
                e.printStackTrace();
            }

        }
        Log.i("artistadaptr","gget album end");


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case android.R.id.home:finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
