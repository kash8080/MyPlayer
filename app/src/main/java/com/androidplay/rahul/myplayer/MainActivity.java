package com.androidplay.rahul.myplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
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

import android.support.v7.graphics.Palette;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionValues;
import com.transitionseverywhere.extra.Scale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener,View.OnClickListener,recycler_adapter.adaptr,
        SeekBar.OnSeekBarChangeListener,SlidingUpPanelLayout.PanelSlideListener{

    private final int read_external=11001;
    int menuid = 0;
    Boolean isplaying = false;

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
    TextView nav_name;
    AppBarLayout appBarLayout;
    Bitmap bitmap;
    SharedPreferences sharedPref;
    ApplicationController con;
    PopupMenu popup;
    ImageView main_backgroundimage;
    //slidinglayout
    Toolbar card;
    String path;
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total,songname,artistname;
    SeekBar seekBar;
    String Current_time;
    ImageButton button;
    ImageView imageslide,image;
    updateseekbar1 seekbarasync;
    songs current_song;
    Long setmax=0L;
    public boolean isshuffle;
    private int isrepeat=0;
    LinearLayout slidercontrolcolour;

    ImageLoader imageLoader;
    de.hdodenhof.circleimageview.CircleImageView profileimage;

    CoordinatorLayout coordinatorlayout;

    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;
    LinearLayout recView_tablayout;

    String theme_no;
    Boolean dark;
    static Context context;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context=this;
        Log.i("llllp", "oncreate");
        con = new ApplicationController(this.getApplicationContext(), this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        theme_no=sharedPref.getString("THEME_LIST","1") ;
        dark = sharedPref.getBoolean("check", false);
        setthemecolours();

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

        setthemeAndBackground();

        setSupportActionBar(toolbar);

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
                con.musicSrv.restoreCurrentSongValue();
            }
        }else{
            con.setCurrent_pos(pos);
        }
        imageLoader = ImageLoader.getInstance();
        set_card_visibility();
        card.setContentInsetsAbsolute(0, 0);

        nav_tab_setup();

        //navigation header
        View v=navigationView.getHeaderView(0);
        //profileimage=(CircleImageView)v.findViewById(R.id.profile_image);
        //nav_name=(TextView)v.findViewById(R.id.nav_name) ;

        imageslide.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                seekBar.setProgress(0);
                con.playnext();
                seekBar.setProgress(0);
            }
            public void onSwipeRight() {
                seekBar.setProgress(0);
                con.playprev();
                seekBar.setProgress(0);
            }
        });

        connectControllerToSession(con.getMediaSessionToken());
        gettoken gettokenn=new gettoken();
        gettokenn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.i("llllp","on create end");

    }

    private void setthemecolours(){
        if(dark){
            setTheme(R.style.AppThemeDark_night);
        }else {
            switch (theme_no) {
                case "1":
                    setTheme(R.style.AppTheme);
                    break;
                case "2":
                    setTheme(R.style.AppTheme_Purple);
                    break;
                case "3":
                    setTheme(R.style.AppTheme_Red);
                    break;
                case "4":
                    setTheme(R.style.AppTheme_orange);
                    break;
                case "5":
                    setTheme(R.style.AppTheme_indigo);
                    break;
                case "6":
                    setTheme(R.style.AppTheme_brown);
                    break;
                default:
                    setTheme(R.style.AppTheme);
                    break;
            }
        }
    }
    private void setthemeAndBackground() {
        Boolean dark = sharedPref.getBoolean("check", false);
        int img_no = sharedPref.getInt("image_chooser", 0);
        if (dark) {
            Log.i("settn", "dark");
            navigationView.setBackgroundColor(con.getPrimary());
            main_backgroundimage.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDarknight));
            appBarLayout.setAlpha(1);
            tablayout.setAlpha(1);
        } else {

            if (img_no >= 1) {
                Log.i("settn", "main act current value=" + img_no);
                DisplayMetrics displaymetrics = new DisplayMetrics();
                this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                coordinatorlayout.setBackgroundColor(ContextCompat.getColor(this,R.color.colorbackgroundgrey));
                int width=displaymetrics.widthPixels;
                int height=displaymetrics.heightPixels;
                switch (img_no) {
                    case 1: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            //set height of cardview=width
                            Picasso.with(this)
                                    .load(R.drawable.coffee_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        } else {

                            // create the animation (the final radius is zero)
                            main_backgroundimage.setVisibility(View.INVISIBLE);
                            Picasso.with(this)
                                    .load(R.drawable.coffee)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                    case 2: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            Picasso.with(this)
                                    .load(R.drawable.presents_land)
                                    .error(R.drawable.coffee_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        } else {
                            Picasso.with(this)
                                    .load(R.drawable.presents_port)
                                    .error(R.drawable.coffee)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                    case 3: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            Picasso.with(this)
                                    .load(R.drawable.leaves_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        } else {
                            Picasso.with(this)
                                    .load(R.drawable.leaves_port)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                    case 4: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            Picasso.with(this)
                                    .load(R.drawable.leaves2_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        } else {
                            Picasso.with(this)
                                    .load(R.drawable.leaves2_port)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                    case 5: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            Picasso.with(this)
                                    .load(R.drawable.bloom_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);

                        } else {
                            Picasso.with(this)
                                    .load(R.drawable.bloom_port)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                }
                //make toolbar and tablayout semitransparent for grey theme and opaque for others
               // if (theme_no.equals("1")) {
                    appBarLayout.setAlpha(0.7f);
                    tablayout.setAlpha(.7f);
               /* } else {
                    appBarLayout.setAlpha(1);
                    tablayout.setAlpha(1);
                }*/
            } else {
                Log.i("settn", "not dark");

                main_backgroundimage.setBackground(null);
                main_backgroundimage.setBackgroundColor(0xffffffff);
                appBarLayout.setAlpha(1);
                tablayout.setAlpha(1);
            }
        }
    }
    public Callback picassocallback=new Callback() {
        @Override
        public void onSuccess() {
            Log.i("picss","onsuccess");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && main_backgroundimage.isAttachedToWindow()){
                int cx = main_backgroundimage.getWidth();
                int cy = (main_backgroundimage.getHeight() );

                // get the initial radius for the clipping circle
                float finalradius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(main_backgroundimage, 0, cy, 0,finalradius);
                anim.setDuration(600);
                // start the animation
                main_backgroundimage.setVisibility(View.VISIBLE);
                anim.start();
            }else{
                main_backgroundimage.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onError() {
            Log.i("picss","onerror");
            main_backgroundimage.setVisibility(View.VISIBLE);

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
        adapter.addFragment(new home());
        adapter.addFragment(new Albums());
        adapter.addFragment(new playlist());
        adapter.addFragment(new Artist());
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

        //sliding player setup
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
        seekBar.setOnSeekBarChangeListener(this);
        imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.setOnClickListener(this);
        button.setOnClickListener(this);

        slider.addPanelSlideListener(this);
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

    public Fragment getFragment(int position){
        return adapter.getFragment(position);
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
                for(int i=0;i<4;i++){
                     adapter.getFragment(i);

                }
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("cccc", "oncreateoptions menu");
        // Inflate the menu; this adds items to the action bar if it is present.
         if (menuid == 2) {
            Log.i("cccc", "menuid=2");
            getMenuInflater().inflate(R.menu.playlist, menu);
        } else if (menuid == 3) {
             Log.i("cccc", "menuid=2");
             getMenuInflater().inflate(R.menu.main_nosort, menu);
             SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);

             MenuItem item=menu.findItem(R.id.search_view);
             SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
             searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         } else  {
            // Associate searchable configuration with the SearchView
            SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);

            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem item=menu.findItem(R.id.search_view);
            SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            // SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            //searchAutoComplete.setHintTextColor(ContextCompat.getColor(this,R.color.colorSecondaryText));
        }

        return super.onCreateOptionsMenu(menu);
    }

    public Context getcontext(){
        return context;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.add_new_playlist:{
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
                break;
            }
            case R.id.action_settings:{
                startActivity(new Intent(this,settings.class));
                break;
            }
            case R.id.sort_name:{
                ArrayList<songs> mlist;
                if(menuid==0){
                     mlist=con.getAllsonglist();
                }else if(menuid==1){
                    mlist=((Albums)adapter.getFragment(1)).list;
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
                    ((home)adapter.getFragment(0)).rec_adapter.notifyDataSetChanged();
                }else if(menuid==1){
                    ((Albums)adapter.getFragment(1)).rec_adapter.notifyDataSetChanged();
                }

                break;
            }
            case R.id.sort_name_desc:{
                ArrayList<songs> mlist;
                if(menuid==0){
                    mlist=con.getAllsonglist();
                }else if(menuid==1){
                    mlist=((Albums)adapter.getFragment(1)).list;
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
                    ((home)adapter.getFragment(0)).rec_adapter.notifyDataSetChanged();
                }else if(menuid==1){
                    ((Albums)adapter.getFragment(1)).rec_adapter.notifyDataSetChanged();
                }
                break;
            }
            case R.id.sort_artist:{
                ArrayList<songs> mlist;
                if(menuid==0){
                    mlist=con.getAllsonglist();
                }else if(menuid==1){
                    mlist=((Albums)adapter.getFragment(1)).list;
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
                    ((home)adapter.getFragment(0)).rec_adapter.notifyDataSetChanged();
                }else if(menuid==1){
                    ((Albums)adapter.getFragment(1)).rec_adapter.notifyDataSetChanged();
                }
                break;
            }
            case R.id.sort_artist_desc:{
                ArrayList<songs> mlist;
                if(menuid==0){
                    mlist=con.getAllsonglist();
                }else if(menuid==1){
                    mlist=((Albums)adapter.getFragment(1)).list;
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
                    ((home)adapter.getFragment(0)).rec_adapter.notifyDataSetChanged();
                }else if(menuid==1){
                    ((Albums)adapter.getFragment(1)).rec_adapter.notifyDataSetChanged();
                }
                break;
            }
            case R.id.clear_history:{
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
                suggestions.clearHistory();
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
        adapter.refreshFragment(pos);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if(position!=menuid){
            try {
                if(menuid==0) {
                    if (((home) getFragment(menuid)).mActionMode != null) {
                        ((home) getFragment(menuid)).mActionMode.finish();
                    }
                }else if(menuid==1) {
                    if (((Albums) getFragment(menuid)).mActionMode != null) {
                        ((Albums) getFragment(menuid)).mActionMode.finish();
                    }
                }else if(menuid==3) {
                    if (((Artist) getFragment(menuid)).mActionMode != null) {
                        ((Artist) getFragment(menuid)).mActionMode.finish();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        menuid = position;
        invalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.i("lllll","progress changed :");
        // Log.i("kkkk","on progress changed:"+String.valueOf(progress)+" -- "+!con.isnull() +fromUser);
        Log.i("mmmm","onprogresschamged -"+String.valueOf(progress));

        if(!con.isnull() && fromUser){
            // con.seekTo(progress);
            Log.i("kkkk","on progress changed:"+String.valueOf(progress));
            con.seekTo(Long.parseLong(String.valueOf(progress*1000)));
        }

    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void updateseekbarAsync() {
        Long dur=con.getDuration()/1000L;
        Log.i("mmmm","updateseekbar main .. method called");

        if(!con.isnull() && con.isPlaying()){
            Log.i("mmmm","setmax="+String.valueOf(setmax));

            if(dur>0 && !(dur.equals(setmax))  ){
                seekBar.setMax(Integer.parseInt(String.valueOf(dur)));
                int timesec=Integer.parseInt(String.valueOf(dur));
                total.setText(gettime(timesec));
                setmax=dur;
                Log.i("mmmm","seekbar setmax updated");
                //

            }
            Log.i("mmmm","background: getDuration setmax"+String.valueOf(dur));
            Current_time=gettime(Integer.parseInt(String.valueOf(con.getcurrentplaybacktime()/1000L)));
            Log.i("kkkk","current string:"+Current_time);

            current.setText(Current_time);
            // Log.i("kkkk","getcurrentplaybacktime:"+String.valueOf(con.getcurrentplaybacktime()/1000L));
            seekBar.setProgress(Integer.parseInt(String.valueOf(con.getcurrentplaybacktime()/1000L)));
            Log.i("kkkk","updateseekbar");
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        if(newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)){

            button.setVisibility(View.INVISIBLE);
           /* button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.queue));
            TransitionManager.beginDelayedTransition(card);
            button.setVisibility(View.VISIBLE);*/
        }else if(newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)){
            button.setVisibility(View.VISIBLE);
                if(con.isPlaying()){
                    Log.i("llll","isplaying");
                    button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause));
                }else{
                    button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play));
                }
        }
    }


    public class updateseekbar1 extends AsyncTask<Void,Void,Void>{

        private boolean canrun=true;
        @Override
        protected void onPreExecute() {
            Log.i("kkkk","onpreexecute---------------");

            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            canrun=false;
        }

        @Override
        protected Void doInBackground(Void... params) {
           // Log.i("kkkk","doinbackground11");
            while (canrun) {
                Log.i("kkkk"," canrun main doinbackground");
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
           // Log.i("bnbn",con.isPlaying()+" "+con.getcurrentplaybacktime());
            updateseekbarAsync();
            super.onProgressUpdate(values);
        }
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
    public void onRequestPermissionsResult ( int requestCode,
                                             String permissions[],int[] grantResults){
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
                currentPlaybackstate=controllerCompat.getPlaybackState();
                if(currentPlaybackstate!=null) {
                    Log.i("mjkl", "current playback state is not null bar_button click");

                    MediaControllerCompat.TransportControls controls = controllerCompat.getTransportControls();
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
                        default: {
                            controls.play();
                            Log.i("mjkl", "unhandled state " + currentPlaybackstate.getState());
                        }
                    }
                }else{
                    Log.i("mjkl", "current playback state is null bar_button click");
                    con.playsong(0);
                }
                return;
            }

            case R.id.play_pause :{
                Log.i("jihu","playpause");
                Log.i("jihu","musicsrv null"+String.valueOf(con.musicSrv==null));
                Log.i("jihu","musicsrv msession null"+String.valueOf(con.musicSrv.msession==null));
                Log.i("jihu","musicsrv ,session activv isactive"+String.valueOf(con.musicSrv.msession.isActive()));

                // Log.i("mmmm",String.valueOf(con.getCurrentPosition())+"---"+String.valueOf(con.getlist().size()));
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

                isrepeat=con.isRepeat();
                if(isrepeat==0){
                    isrepeat=1;
                    con.setRepeat(1);
                    setrepeatbutton(true);
                }else if(isrepeat==1){
                    isrepeat=2;
                    con.setRepeat(2);
                    setrepeatbutton(true);
                }else{
                    isrepeat=0;
                    con.setRepeat(0);
                    setrepeatbutton(true);
                }

                return;
            }
            case R.id.shuffle :{
                Log.i("llll","shuffle");
                if(isshuffle){
                    isshuffle=false;
                    con.setShuffle(false);
                    setshufflebutton(true);
                }else{
                    isshuffle=true;
                    con.setShuffle(true);
                    setshufflebutton(true);
                }
                return;
            }
            default: return;


        }

    }

    @Override
    protected void onResume() {
        Log.i("llllp","onresume");
        super.onResume();
        setrepeatbutton(false);
        setshufflebutton(false);
        connectControllerToSession(con.getMediaSessionToken());
        if(controllerCompat!=null) {
            controllerCompat.registerCallback(callback);
            currentPlaybackstate=controllerCompat.getPlaybackState();
            currentmetadata=controllerCompat.getMetadata();
        }
        set_card_visibility();
        try {
            Log.i("klkl", "onresume");
            if (con.isPlaying()) {
                Log.i("klkl", "isplaying" + con.getsong().getName());
                setcard(true, con.getsong());
            } else {
                songs song = con.getsong();
                if (song != null) {
                    setcard(false, song);
                } else {
                    setcard(false, null);
                }
            }
        } catch (Exception e) {}


        if(con.getCurrentPosition()==-1){
            con.setCurrent_pos(0);
        }

        seekbarasync =new updateseekbar1();
        seekbarasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        isplaying=con.isPlaying();
        refreshview();
        Log.i("seticons", "from on resume");

        seticon(false);
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
        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
        if(controllerCompat!=null) {
            controllerCompat.unregisterCallback(callback);
        }
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




    //-----ui elements methods
    public void setrepeatbutton(boolean animation){
        isrepeat=con.isRepeat();
        if(isrepeat==0){
            repeat.setImageResource(R.drawable.repeat);
        }else if(isrepeat==1){
            repeat.setImageResource(R.drawable.repeat_selected);
        }else{
            repeat.setImageResource(R.drawable.repeat_one);
        }

        if(animation) {
            repeat.animate().scaleX(1.25f).scaleY(1.25f).setDuration(0).withEndAction(new Runnable() {
                @Override
                public void run() {
                    repeat.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            });
        }
    }
    public void setshufflebutton(boolean animation){
        isshuffle=con.isShuffle();
        if(isshuffle){
            shuffle.setImageResource(R.drawable.shuffle_selected);
        }else      shuffle.setImageResource(R.drawable.shuffle);

        if(animation) {
            shuffle.animate().scaleX(1.25f).scaleY(1.25f).setDuration(0).withEndAction(new Runnable() {
                @Override
                public void run() {
                    shuffle.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            });
        }
    }


    public void seticon(boolean a){
        Log.i("seticon","start---");

        isplaying=con.isPlaying();

        if(isplaying){
            Log.i("seticon","isplaying");
            //play_pause.setImageResource(R.drawable.pause_white);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && a && slider.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED ) {
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause_white));
                Drawable drawable = play_pause.getDrawable();
                ((Animatable) drawable).start();
            }else{
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause_white));
            }

        }else{
            Log.i("seticon","!isplaying");

            //play_pause.setImageResource(R.drawable.play_white);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && a &&  slider.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED ) {
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play_white));
                Drawable drawable = play_pause.getDrawable();
                ((Animatable) drawable).start();
            }else{
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play_white));
            }
        }
        setbutton(a);

        Log.i("seticon","seticon end---");

    }

   public void setbutton(boolean a) {

        isplaying=con.isPlaying();
        if (isplaying) {
            Log.i("seticon","isplaying button");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && a &&  slider.getPanelState()== SlidingUpPanelLayout.PanelState.COLLAPSED ) {
                button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause));
                Drawable drawable = button.getDrawable();

                ((Animatable) drawable).start();

            }else{
                button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause));
            }
        } else {
            Log.i("seticon","!isplaying button");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && a &&  slider.getPanelState()== SlidingUpPanelLayout.PanelState.COLLAPSED ) {
                button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play));
                Drawable drawable = button.getDrawable();
                ((Animatable) drawable).start();
            }else{
                button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play));
            }
        }

    }
    public void refreshview(){

        Log.i("lllll","refresh");
        current_song=con.getsong();
        setdata();
        if(current_song!=null) {
            setcard(con.isPlaying(), con.getsong());
        }
    }
    public void setdata(){
        try {
            Log.i("mjkl","setdata");

            isplaying=con.isPlaying();
            Long timemilli=con.getDuration();
            Long timemillicurrent=con.getcurrentplaybacktime();
            int timesec=Integer.parseInt(String.valueOf(timemilli/1000L));
            int timecurrentsec=Integer.parseInt(String.valueOf(timemillicurrent/1000L));
            Log.i("kkkk","total time of current song is :"+timesec/60+":"+timesec%60);
            total.setText(gettime(timesec));
            Current_time=gettime(Integer.parseInt(String.valueOf(con.getcurrentplaybacktime()/1000L)));

            current.setText(Current_time);
            Log.i("lllll","setdata");
            bitmap= BitmapFactory.decodeFile(current_song.getImagepath());

            if(bitmap!=null){

                //image.setImageBitmap(bitmap);
                Picasso.with(this)
                        .load(Uri.parse("file://"+current_song.getImagepath()))
                        .error(R.drawable.guitar)
                        .into(image);
                slidercontrolcolour.setVisibility(View.VISIBLE);
                slidercontrolcolour.setBackground(ContextCompat.getDrawable(this,R.drawable.grad));
            }
            else {
                slidercontrolcolour.setVisibility(View.VISIBLE);

                //image.setImageResource(R.drawable.mp3full);
                Picasso.with(this)
                        .load(R.drawable.guitar)
                        .error(R.drawable.mp3full)
                        .into(image);
            }
            Log.i("mmmm","setdata: getDuration setmax"+(String.valueOf(Integer.parseInt(String.valueOf(timemilli/1000L)))));
            setmax=timemilli;
            seekBar.setMax(timesec);
            seekBar.setProgress(timecurrentsec);
            //seekBar.setMax(con.getDuration());
            Log.i("lllll","----"+String.valueOf(timemilli));
            Log.i("seticons", "from set data");

            seticon(false);

        }catch (Exception e){e.printStackTrace();
            Log.i("mjkl","exception---");
        }
    }
    public String gettime(int secs){
        int min=secs/60;
        int sec=secs%60;
        String time;
        if(sec<10){
            time=String.valueOf(min)+":0"+String.valueOf(sec);

        }else{
            time=String.valueOf(min)+":"+String.valueOf(sec);

        }
        return time;

    }

    //from recycler adapter
    @Override
    public void setcardss(songs song) {

        set_card_visibility();
        Log.i("llll", "setcardss");
        setcard(true, song);
    }

    public void set_card_visibility(){
        Log.i("klkl", String.valueOf(con.isnull()) +"  "+String.valueOf(con.isPlaying()));
/*
        if (con.getsong()!=null){
            int ii=this.getResources().getInteger(R.integer.panelheight);
            slider.setPanelHeight(ii);
            slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else{
            slider.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
*/
      /*  SharedPreferences sp = this.getSharedPreferences("MyPlayer_CurrentsongInfo", Activity.MODE_PRIVATE);
        int pos = sp.getInt("service_currentSongPositionInList", 0);
         Long seek = sp.getLong("service_currentSongSeekValue", 100);
        if(pos==0 && seek==100){
            slider.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }else{
        */
            int ii=this.getResources().getInteger(R.integer.panelheight);
            slider.setPanelHeight(ii);
            slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        //}
    }

    public void setcard(boolean a, songs song) {
        Log.i("seticons", "from set card");

        seticon(false);
        path = song.getImagepath();
        if (song != null) {
            Log.i("klkl", "song!=null..setting card");
            try {
                String name = song.getName();
                String artist = song.getArtist();
                songname.setText(name);
                artistname.setText(artist);
                Bitmap bitmap = BitmapFactory.decodeFile(song.getImagepath());
                if (bitmap != null) {
                    //image.setImageBitmap(bitmap);
                    //imageslide.setImageBitmap(bitmap);
                    Picasso.with(this)
                            .load(Uri.parse("file://"+song.getImagepath()))
                            .error(R.drawable.mp3full)
                            .into(image);
                    Picasso.with(this)
                            .load(Uri.parse("file://"+song.getImagepath()))
                            .error(R.drawable.mp3full)
                            .into(imageslide);
                } else {
                    //image.setImageResource(R.drawable.mp3full);
                    //imageslide.setImageResource(R.drawable.mp3full);
                    Picasso.with(this)
                            .load(R.drawable.guitar)
                            .error(R.drawable.mp3full)
                            .into(image);
                    Picasso.with(this)
                            .load(R.drawable.guitar)
                            .error(R.drawable.mp3full)
                            .into(imageslide);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    public void setmetadata(MediaMetadataCompat metadataCompat){
        refreshview();
        try {
            ((home) getFragment(0)).rec_adapter.notifyDataSetChanged();
        }catch (Exception e){}
    }
    public void setstate(PlaybackStateCompat stateCompat){
        Log.i("mjkl", "setstate");
        Log.i("seticons", "setstate");

        if(stateCompat!=null) {
            Log.i("mjkl", "setstate state is not null");
            switch (stateCompat.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    Log.i("mjkl", "set state playing state ");
                    seticon(true);
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.i("mjkl", "set state paused state ");
                    seticon(true);
                    break;
                }
                default:
            }
        }else{
            seticon(false);
        }
        try {
            ((home) getFragment(0)).rec_adapter.notifyDataSetChanged();
        }catch (Exception e){}
    }

    public class gettoken extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            while(con.getMediaSessionToken()==null){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            connectControllerToSession(con.getMediaSessionToken());
            super.onPostExecute(aVoid);
        }
    }

}
