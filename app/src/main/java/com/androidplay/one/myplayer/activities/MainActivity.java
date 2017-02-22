package com.androidplay.one.myplayer.activities;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;

import android.Manifest;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;


import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;

import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplay.one.myplayer.SliderImagePagerAdapter;
import com.androidplay.one.myplayer.fragments.Albums;
import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.fragments.Artist;
import com.androidplay.one.myplayer.MySuggestionProvider;
import com.androidplay.one.myplayer.fragments.Folders;
import com.androidplay.one.myplayer.fragments.playlist;
import com.androidplay.one.myplayer.helper_classes.OnSwipeTouchListener;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.fragments.home;
import com.androidplay.one.myplayer.helper_classes.SliderHelper;
import com.androidplay.one.myplayer.helper_classes.ThemeHelper;
import com.androidplay.one.myplayer.helper_functions;
import com.androidplay.one.myplayer.pageradapter;
import com.androidplay.one.myplayer.songs;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener,View.OnClickListener {

    private final int read_external=11001;
    int menuid = 0;
    Boolean isplaying = false;

    ThemeHelper themeHelper;
    //ui elements
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    Toolbar bottom_control_toolbar;
    NavigationView navigationView;
    pageradapter adapter;
    TabLayout tablayout;
    ViewPager viewpager;
    Toolbar toolbar;
    AlertDialog.Builder builder;
    AppBarLayout appBarLayout;
    SharedPreferences sharedPref;
    ApplicationController con;
    ImageView main_backgroundimage;
    FloatingActionButton main_fab;
    MenuItem timermenuItem;

    //slidinglayout
    ViewPager sliderviewpager;
    SliderImagePagerAdapter sliderImagePagerAdapter;
    Toolbar card;
    String path;
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total,songname,artistname;
    SeekBar seekBar;
    ImageButton button;
    ImageView imageslide,image;
    LinearLayout slidercontrolcolour;
    SliderHelper sliderHelper;

    ImageLoader imageLoader;

    public CoordinatorLayout coordinatorlayout;

    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;
    LinearLayout recView_tablayout;

    String theme_no;
    Boolean dark;
    static Context context;
    TimerClass timerClass;



    //fragments lists;
    public ArrayList<songs> albumlist;
    public ArrayList<songs> artistlist;

    private home homefragment;
    private Albums albumfragment;
    private playlist playlistfragment;
    private Folders folderfragment;
    private Artist artistfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            if(con.needforpermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
                startActivity(new Intent(this, PermissionActivity.class));
                finish();
                return;
                //finish called to stop further proccess of this activity
            }
        }else{
            if(con.needforpermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
                startActivity(new Intent(this, PermissionActivity.class));
                finish();
                return;
                //finish called to stop further proccess of this activity
            }else{
                super.onCreate(savedInstanceState);
            }

        }
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.fade_edited));
        }
        //initialise everything
        initialise();

        themeHelper.setthemeAndBackground(coordinatorlayout,navigationView,main_backgroundimage,appBarLayout,tablayout,null);

        setSupportActionBar(toolbar);
        handle_enter_transition();
        //to check if user revoke permissions while app running .
        if(con.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)){
            Log.i("llllp", "need for permissions");

            request_perm();
            //startActivity(new Intent(this,PermissionActivity.class));
        }else{
            Log.i("llllp", "loadsongswthimges");

            con.loadsongswithimages();
        }

        SharedPreferences sp = this.getSharedPreferences("MyPlayer_CurrentsongInfo", Activity.MODE_PRIVATE);
        int pos = sp.getInt("service_currentSongPositionInList", 0);
        if(con.musicSrv!=null) {
            if (!con.musicSrv.previousstatesaved) {
                con.musicSrv.restoreCurrentSongValue(false);
            }
        }else{
            con.setCurrent_pos(pos);
        }

        imageLoader = ImageLoader.getInstance();
        card.setContentInsetsAbsolute(0, 0);

        nav_tab_setup();

        //slider viewpager setup
        sliderImagePagerAdapter=new SliderImagePagerAdapter(getSupportFragmentManager());
        sliderviewpager.setAdapter(sliderImagePagerAdapter);
        //sliderviewpager.addOnPageChangeListener(pageChangeListener);

        sliderHelper=new SliderHelper(this,sliderviewpager,sliderImagePagerAdapter,slider);
        sliderHelper.setImagebuttons(play_pause,repeat,shuffle,button,previous,next);
        sliderHelper.setImageViews(imageslide,image);
        sliderHelper.setTextViews(current,total,songname,artistname);
        sliderHelper.setRest(slidercontrolcolour,seekBar);
        sliderHelper.set_card_visibility();

        connectControllerToSession(con.getMediaSessionToken());
        gettoken gettokenn=new gettoken();
        gettokenn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.i("llllp","on create end");



    }

    public void handle_enter_transition(){

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Transition enter;
                try {
                    enter = TransitionInflater.from(this).inflateTransition(R.transition.fade_edited);
                } catch (Exception e) {
                    enter = TransitionInflater.from(this).inflateTransition(R.transition.fade_edited_1);
                }

                enter.setDuration(300);
                getWindow().setEnterTransition(enter);
                getWindow().setExitTransition(enter);


                getWindow().getSharedElementEnterTransition().setDuration(300);
                getWindow().getSharedElementReturnTransition().setDuration(300)
                        .setInterpolator(new AccelerateInterpolator());
            }
        }catch (Exception e){
            Log.i("",e.toString());
            e.printStackTrace();
        }
    }
    private MediaControllerCompat.Callback callback=new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.i("mjkl","playbackstate change callback");
            super.onPlaybackStateChanged(state);
            currentPlaybackstate=state;
            setstate(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.i("mjkl","metadata change callback");
            super.onMetadataChanged(metadata);
            currentmetadata=metadata;
            setmetadata(metadata);
        }
    };

    private void nav_tab_setup(){
        ////////// //navigation view setup
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        navigationView.setNavigationItemSelectedListener(this);

        //tabs setup/////////////
        tablayout.addTab(tablayout.newTab().setText("first"));
        tablayout.addTab(tablayout.newTab().setText("second"));
        tablayout.addTab(tablayout.newTab().setText("third"));
        tablayout.setTabGravity(tablayout.GRAVITY_FILL);
        //tablayout.setOnTabSelectedListener(this);
        adapter = new pageradapter(getSupportFragmentManager());

        viewpager.setAdapter(adapter);
        viewpager.addOnPageChangeListener(this);
        tablayout.setupWithViewPager(viewpager);

    }

    private void initialise(){
        viewpager = (ViewPager) findViewById(R.id.pager);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        tablayout = (TabLayout) findViewById(R.id.tablayout);
        builder = new AlertDialog.Builder(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        coordinatorlayout=(CoordinatorLayout)findViewById(R.id.main_content);
        appBarLayout=(AppBarLayout) findViewById(R.id.MyAppbar);
        bottom_control_toolbar=(Toolbar)findViewById(R.id.bottom_control_toolbar);
        recView_tablayout=(LinearLayout)findViewById(R.id.ssss);
        main_backgroundimage = (ImageView) findViewById(R.id.main_background_image);
        main_fab=(FloatingActionButton)findViewById(R.id.main_fab);

        Menu menuu= navigationView.getMenu().getItem(2).getSubMenu();
        timermenuItem=menuu.getItem(0);

        //sliding player setup
        sliderviewpager=(ViewPager)findViewById(R.id.sliderviewpager);
        card = (Toolbar) findViewById(R.id.controller_bar);
        slidercontrolcolour=(LinearLayout) findViewById(R.id.grad_bottom_slide);
        songname = (TextView) findViewById(R.id.bar_name);
        artistname = (TextView) findViewById(R.id.bar_artist);
        button = (ImageButton) findViewById(R.id.bar_button);
        image = (ImageView) findViewById(R.id.bar_image);
        slider=(SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        previous=(ImageButton)findViewById(R.id.previous);
        next=(ImageButton)findViewById(R.id.next);
        play_pause=(ImageButton)findViewById(R.id.play_pause);
        repeat=(ImageButton)findViewById(R.id.repeat);
        shuffle=(ImageButton)findViewById(R.id.shuffle);
        current=(TextView) findViewById(R.id.current_time);
        total=(TextView) findViewById(R.id.total_time);
        seekBar=(SeekBar)findViewById(R.id.seekbar);
        imageslide=(ImageView)findViewById(R.id.player_image);

        play_pause.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        repeat.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.setOnClickListener(this);
        button.setOnClickListener(this);
        main_fab.setOnClickListener(this);

    }

    private void connectControllerToSession(MediaSessionCompat.Token token) {
        if(token!=null) {
            try {
                controllerCompat = new MediaControllerCompat(this, token);
                controllerCompat.registerCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            PlaybackStateCompat stateCompat = controllerCompat.getPlaybackState();
            MediaMetadataCompat metadataCompat = controllerCompat.getMetadata();
            currentPlaybackstate = stateCompat;
            currentmetadata = metadataCompat;
            setstate(stateCompat);
            setmetadata(metadataCompat);
            controllerCompat.registerCallback(callback);
        }

    }
    private class gettoken extends AsyncTask<Void,Void,Void>{

                boolean run=true;
                @Override
                protected Void doInBackground(Void... voids) {

                    while(run){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        publishProgress();
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Void... values) {
                    if(!(con.getMediaSessionToken()==null)){
                        run=false;
                    }
                    super.onProgressUpdate(values);
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    connectControllerToSession(con.getMediaSessionToken());
                    super.onPostExecute(aVoid);
                }
            }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(slider.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED){
                slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }else {
                if(menuid==3){
                    if(folderfragment.getisOnRootFolder()){
                        super.onBackPressed();
                    }else{
                        folderfragment.openbackfolder();
                    }
                }else {
                    super.onBackPressed();
                }
            }
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_player) {
            Intent intent = new Intent(this, playerr.class);
            intent.putExtra("name", songname.getText());
            intent.putExtra("artist", artistname.getText());
            intent.putExtra("path", path);
            startActivity(intent);

        } else if (id == R.id.nav_queue) {
            startActivity(new Intent(this, Now_playing.class));

        } else if (id == R.id.nav_settings) {
            Intent intent =new Intent(this,settings.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.nav_timer) {
            handleTimerMenuClick();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }
    public void refreshFragment(int pos){
        if(pos==2 && playlistfragment!=null){
            playlistfragment.refreshview();
        }
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }
    @Override
    public void onPageScrollStateChanged(int state) {
    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("cccc", "oncreateoptions menu");
        // Inflate the menu; this adds items to the action bar if it is present.
        if (menuid == 0) {
            Log.i("cccc", "menuid=2");
            getMenuInflater().inflate(R.menu.main, menu);
        }else if (menuid == 1) {
            Log.i("cccc", "menuid=2");
            getMenuInflater().inflate(R.menu.main, menu);
        }else if (menuid == 2) {
            Log.i("cccc", "menuid=2");
            getMenuInflater().inflate(R.menu.playlist, menu);
        }else if (menuid == 3) {
            Log.i("cccc", "menuid=2");
            getMenuInflater().inflate(R.menu.main_nosort, menu);
        }else if(menuid == 4) {
            // Associate searchable configuration with the SearchView
            getMenuInflater().inflate(R.menu.main_artist, menu);
            // SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            //searchAutoComplete.setHintTextColor(ContextCompat.getColor(this,R.color.colorSecondaryText));
        }

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem item=menu.findItem(R.id.search_view);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        ComponentName componentName=new ComponentName(this,SearchableActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:{
                startActivity(new Intent(this,settings.class));
                break;
            }
            case R.id.sort_name:{
                ArrayList<songs> mlist;
                if(menuid==0){
                    mlist=con.getAllsonglist();
                }else if(menuid==1){
                    mlist=albumfragment.list;
                }else{
                    return true;
                }
                Collections.sort(mlist, new Comparator<songs>() {
                    @Override
                    public int compare(songs songs, songs t1) {
                        return songs.getName().compareToIgnoreCase(t1.getName());
                    }
                });
                if(menuid==0){
                    homefragment.rec_adapter.notifyDataSetChanged();
                }else if(menuid==1){
                    albumfragment.rec_adapter.notifyDataSetChanged();
                }

                break;
            }
            case R.id.sort_name_desc:{
                ArrayList<songs> mlist;
                if(menuid==0){
                    mlist=con.getAllsonglist();
                }else if(menuid==1){
                    mlist=albumfragment.list;
                }else{
                    return true;
                }
                Collections.sort(mlist, new Comparator<songs>() {
                    @Override
                    public int compare(songs songs, songs t1) {
                        return -(songs.getName().compareToIgnoreCase(t1.getName()));
                    }
                });
                if(menuid==0){
                    homefragment.rec_adapter.notifyDataSetChanged();
                }else if(menuid==1){
                    albumfragment.rec_adapter.notifyDataSetChanged();
                }
                break;
            }
            case R.id.sort_artist:{
                ArrayList<songs> mlist;
                if(menuid==0){
                    mlist=con.getAllsonglist();
                }else if(menuid==1){
                    mlist=albumfragment.list;
                }else{
                    return true;
                }

                Collections.sort(mlist, new Comparator<songs>() {
                    @Override
                    public int compare(songs songs, songs t1) {
                        return songs.getArtist().compareToIgnoreCase(t1.getArtist());
                    }
                });
                if(menuid==0){
                    homefragment.rec_adapter.notifyDataSetChanged();
                }else if(menuid==1){
                    albumfragment.rec_adapter.notifyDataSetChanged();
                }
                break;
            }
            case R.id.sort_artist_desc:{
                ArrayList<songs> mlist;
                if(menuid==0){
                    mlist=con.getAllsonglist();
                }else if(menuid==1){
                    mlist=albumfragment.list;
                }else{
                    return true;
                }
                Collections.sort(mlist, new Comparator<songs>() {
                    @Override
                    public int compare(songs songs, songs t1) {
                        return -songs.getArtist().compareToIgnoreCase(t1.getArtist());
                    }
                });
                if(menuid==0){
                    homefragment.rec_adapter.notifyDataSetChanged();
                }else if(menuid==1){
                    albumfragment.rec_adapter.notifyDataSetChanged();
                }
                break;
            }
            case R.id.clear_history:{
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
                suggestions.clearHistory();
                break;
            }
            case R.id.sort_artist_name:{
                ArrayList<songs> mlist;
                mlist=artistfragment.list;

                Collections.sort(mlist, new Comparator<songs>() {
                    @Override
                    public int compare(songs songs, songs t1) {
                        return songs.getName().compareToIgnoreCase(t1.getName());
                    }
                });
                artistfragment.rec_adapter.notifyDataSetChanged();

                break;
            }
            case R.id.sort_artist_name_desc:{
                ArrayList<songs> mlist;
                mlist=artistfragment.list;

                Collections.sort(mlist, new Comparator<songs>() {
                    @Override
                    public int compare(songs songs, songs t1) {
                        return -songs.getName().compareToIgnoreCase(t1.getName());
                    }
                });
                artistfragment.rec_adapter.notifyDataSetChanged();

                break;
            }
            case R.id.sort_artist_noofalbums:{
                ArrayList<songs> mlist;
                mlist=artistfragment.list;

                Collections.sort(mlist, new Comparator<songs>() {
                    @Override
                    public int compare(songs song, songs t1) {
                        return -Integer.valueOf(song.getNumberOfAlbums()).compareTo(Integer.valueOf(t1.getNumberOfAlbums()));                    }
                });
                artistfragment.rec_adapter.notifyDataSetChanged();

                break;
            }
            case R.id.sort_artist_noofsongs:{
                ArrayList<songs> mlist;
                mlist=artistfragment.list;

                Collections.sort(mlist, new Comparator<songs>() {
                    @Override
                    public int compare(songs song, songs t1) {
                        return -Integer.valueOf(song.getNumberOfTracks()).compareTo(Integer.valueOf(t1.getNumberOfTracks()));
                    }
                });
                artistfragment.rec_adapter.notifyDataSetChanged();

                break;
            }
            default:return false;
        }
        return true;
    }
    public void createnewplaylist(String playlistname) {
        ContentValues mInserts = new ContentValues();
        mInserts.put(MediaStore.Audio.Playlists.NAME, playlistname);
        mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        this.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
    }

    @Override
    public void onPageSelected(int position) {
        if(position!=menuid){
            try {
                if(menuid==0) {
                    if (homefragment.mActionMode != null) {
                        homefragment.mActionMode.finish();
                    }
                }else if(menuid==1) {
                    if (albumfragment.mActionMode != null) {
                        albumfragment.mActionMode.finish();
                    }
                }else if(menuid==3) {
                    if (artistfragment.mActionMode != null) {
                        artistfragment.mActionMode.finish();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(position==2){
            main_fab.setVisibility(View.VISIBLE);
            main_fab.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
        }else{
            main_fab.animate().scaleX(0f).scaleY(0f).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    main_fab.setVisibility(View.INVISIBLE);
                }
            }).start();
        }
        menuid = position;
        invalidateOptionsMenu();
    }


    public void setHomefragment(home homefragment) {
        this.homefragment = homefragment;
    }

    public void setAlbumfragment(Albums albumfragment) {
        this.albumfragment = albumfragment;
    }

    public void setFolderfragment(Folders folderfragment) {
        this.folderfragment = folderfragment;
    }

    public void setArtistfragment(Artist artistfragment) {
        this.artistfragment = artistfragment;
    }

    public void setPlaylistfragment(playlist playlistfragment) {
        this.playlistfragment = playlistfragment;
    }

    public void request_perm(){
        Log.i("llllp","request perm");

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                read_external);
        // read_external is an
        // app-defined int constant. The callback method gets the
        // result of the request.

    }
    @Override
    public void onRequestPermissionsResult ( int requestCode,String permissions[],int[] grantResults){
        Log.i("llllp","onresult");

        switch (requestCode) {
            case read_external: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    con.loadsongswithimages();
                    adapter = new pageradapter(getSupportFragmentManager());
                    viewpager.setAdapter(adapter);


                } else {
                            startActivity(new Intent(this,PermissionActivity.class));

                }

            }
            case 12: {

            }

        }
    }

    @Override
    public void onClick(View v) {
        Log.i("pkss","on click");

        int id = v.getId();
        switch (id) {
            case R.id.controller_bar: {

                //Intent intent = new Intent(this, playerr.class);
                //startActivity(intent);
                if(slider.getPanelState()== SlidingUpPanelLayout.PanelState.COLLAPSED){
                    slider.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
                return;
            }

            case R.id.bar_button: {
                if(slider.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED){

                    return;
                }
                if(con.isPlaying()){
                    con.pause();
                }else{
                    con.resume();
                }
                if(controllerCompat==null){
                    gettoken gettokenn=new gettoken();
                    gettokenn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                return;
            }

            case R.id.play_pause :{

                /*// Log.i("mmmm",String.valueOf(con.getCurrentPosition())+"---"+String.valueOf(con.getlist().size()));
                currentPlaybackstate=controllerCompat.getPlaybackState();
                Log.i("jihu","musicsrv ,controller compat playback state "+String.valueOf(currentPlaybackstate.getState()));

                if(currentPlaybackstate!=null) {
                    Log.i("jihu", "current playback state is not null play_pause click");

                    MediaControllerCompat.TransportControls controls = controllerCompat.getTransportControls();
                    Log.i("jihu","musicsrv ,controller compat playback controls null"+String.valueOf(controls==null));

                    switch (currentPlaybackstate.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING: {
                            Log.i("mjkl", "state playing state to pause");
                            controls.pause();
                            break;
                        }
                        case PlaybackStateCompat.STATE_PAUSED: {
                            Log.i("mjkl", "paused state to play");
                            controls.play();
                            break;
                        }
                        default:
                            Log.i("mjkl", "unhandled state " + currentPlaybackstate.getState());

                    }
                }else{
                    Log.i("jihu", "current playback state is null play_pause click");

                    con.playsong(0);
                }
                */

                if(con.isPlaying()){
                    con.pause();
                }else{
                    con.resume();
                }
                if(controllerCompat==null){
                    gettoken gettokenn=new gettoken();
                    gettokenn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                return;
            }
            case R.id.previous :{
                previous.animate().scaleX(1.25f).scaleY(1.25f).setDuration(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        previous.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    }
                });

                Log.i("lllll","previous");
                seekBar.setProgress(0);
                con.playprev();
                //refreshview();
                seekBar.setProgress(0);

                return;
            }
            case R.id.next :{
                Log.i("mmmm","next---------------");
                next.animate().scaleX(1.25f).scaleY(1.25f).setDuration(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        next.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    }
                });

                Log.i("lllll","next");
                seekBar.setProgress(0);
                con.playnext();
                //refreshview();
                seekBar.setProgress(0);
                return;
            }
            case R.id.repeat :{

                Log.i("mmmm","repeat");

                Log.i("mmmm","repeat: getDuration "+String.valueOf(con.getDuration()/1000L));
                sliderHelper.repeatButtonClicked();
                return;
            }
            case R.id.shuffle :{
                Log.i("llll","shuffle");
                sliderHelper.shuffleButtonClicked();
                return;
            }
            case R.id.main_fab: {
                Log.i("pkss","main fab clicked");
                context=this;
                builder.setTitle("Playlist name");
                builder.setCancelable(true);
                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT );
                builder.setView(input);
                builder.setPositiveButton(
                        "Create",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                createnewplaylist(input.getText().toString());
                                viewpager.setAdapter(adapter);
                                viewpager.setCurrentItem(2);
                            }
                        });
                builder.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });


                AlertDialog alert = builder.create();
                try {
                    alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }catch (Exception E){
                    E.printStackTrace();
                }
                alert.show();
                return;
            }
        }

    }

    @Override
    protected void onResume() {
        Log.i("llllp","onresume");
        super.onResume();
        con.activityOnResume();
        sliderHelper.seticon(false);
        sliderHelper.setrepeatbutton(false);
        sliderHelper.setshufflebutton(false);
        connectControllerToSession(con.getMediaSessionToken());
        if(controllerCompat!=null) {
            controllerCompat.registerCallback(callback);
            currentPlaybackstate=controllerCompat.getPlaybackState();
            currentmetadata=controllerCompat.getMetadata();
        }
        sliderHelper.set_card_visibility();
        try {
            Log.i("klkl", "onresume");
            if (con.isPlaying()) {
                Log.i("klkl", "isplaying" + con.getsong().getName());
                sliderHelper.setcard(true, con.getsong());
            } else {
                songs song = con.getsong();
                if (song != null) {
                    sliderHelper.setcard(false, song);
                } else {
                    sliderHelper.setcard(false, null);
                }
            }
        } catch (Exception e) {}


        if(con.getCurrentPosition()==-1){
            con.setCurrent_pos(0);
        }

        sliderHelper.startseekbarasync();
        isplaying=con.isPlaying();
        sliderHelper.refreshPanel();
        Log.i("seticons", "from on resume");

        Log.i("llllp","on resume end");
        if(con.playlistfragmentchanged){
            con.playlistfragmentchanged=false;
            refreshFragment(2);
        }
        connectControllerToSession(con.getMediaSessionToken());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(controllerCompat!=null) {
            controllerCompat.unregisterCallback(callback);
        }
        sliderHelper.cancelSeekbarAsync();
        //tokentracker.stopTracking();
        //profileTracker.stopTracking();
    }

    //to prevent user from opening navigation view while in action mode
    public void lockdrawer(){
        if(drawer!=null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            changecolor change=new changecolor();
            //0 for lock 1 for unlock
            change.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,0);
        }
    }
    //to restore navigation view after action mode
    public void releasedrawer(){
        if(drawer!=null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            changecolor change=new changecolor();
            //0 for lock 1 for unlock
            change.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,1);
        }
    }
    public class changecolor extends AsyncTask<Integer,Void,Void>{

        int i;
        @Override
        protected Void doInBackground(Integer... voids) {
            i=voids[0];
            try {
                if(i==0){
                    Thread.sleep(140);
                }else{
                    Thread.sleep(125);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(i==0){
                if (Build.VERSION.SDK_INT >= 21) {
                    // MainActivity.this.getWindow().setStatusBarColor(Color.rgb(69, 90, 100));
                    MainActivity.this.getWindow().setStatusBarColor(con.getPrimary());
                }
                //colorprimarylight
                //tablayout.setBackgroundColor(Color.rgb(96,125,139));
                tablayout.setBackgroundColor(con.getPrimaryLight());
            }else{
                if (Build.VERSION.SDK_INT >= 21) {
                    //MainActivity.this.getWindow().setStatusBarColor(Color.rgb(38, 50, 56));
                    MainActivity.this.getWindow().setStatusBarColor(con.getPrimaryDark());
                }
                //colorprimary
                //tablayout.setBackgroundColor(Color.rgb(69,90,100));
                tablayout.setBackgroundColor(con.getPrimary());
            }
            super.onPostExecute(aVoid);
        }
    }





    private void handleTimerMenuClick(){
        builder=new AlertDialog.Builder(this);
        String[] array=new String[]{"15 min","30 min","45 min","1 hour","1.5 hours","2 hours","Sleep timer off"};
        builder.setCancelable(true)
                .setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:{
                                setSleeptime(900);
                                break;
                            }
                            case 1:{
                                setSleeptime(1800);
                                break;
                            }
                            case 2:{
                                setSleeptime(2700);
                                break;
                            }
                            case 3:{
                                setSleeptime(3600);
                                break;
                            }
                            case 4:{
                                setSleeptime(5400);
                                break;
                            }
                            case 5:{
                                setSleeptime(7200);
                                break;
                            }
                            case 6:{
                                if(timerClass!=null){
                                    timerClass.cancel(true);
                                }
                                con.startnewtimer(-1);
                                break;
                            }

                        }
                    }
                });
        builder.create().show();
    }
    private void setSleeptime(int secs){
        if(timerClass!=null){
            timerClass.cancel(true);
        }
        timerClass=new TimerClass(secs);
        timerClass.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        con.startnewtimer(secs);
    }


    private void setmetadata(MediaMetadataCompat metadataCompat){
        sliderHelper.refreshPanel();
        try {
            //for giff
            homefragment.rec_adapter.notifyDataSetChanged();
        }catch (Exception e){}
    }
    private void setstate(PlaybackStateCompat stateCompat){
        Log.i("mjkl", "setstate");
        Log.i("seticons", "setstate");

        if(stateCompat!=null) {
            Log.i("mjkl", "setstate state is not null");
            switch (stateCompat.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    Log.i("mjkl", "set state playing state ");
                    sliderHelper.seticon(true);
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.i("mjkl", "set state paused state ");
                    sliderHelper.seticon(true);
                    break;
                }
                default:
            }
        }else{
            sliderHelper.seticon(false);
        }
        try {
            homefragment.rec_adapter.notifyDataSetChanged();
        }catch (Exception e){}
    }

    public class TimerClass extends AsyncTask<Void,Void,Void>{

        private int secs;
        String currenttimeleft;
        public TimerClass(int sec){
            secs=sec;
        }
        @Override
        protected Void doInBackground(Void... voids) {

            while(!isCancelled() && secs>0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                secs--;
                currenttimeleft= helper_functions.gettime(secs);
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            timermenuItem.setTitle(currenttimeleft);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            timermenuItem.setTitle("Sleep Timer");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            timermenuItem.setTitle("Sleep Timer");
            if(!isCancelled()){
                //con.stopmusic();
                controllerCompat.unregisterCallback(callback);
                controllerCompat=null;
                sliderHelper.seticon(false);
            }
        }
    }
}
