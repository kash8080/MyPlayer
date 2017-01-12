package com.androidplay.rahul.myplayer;

import android.app.AlertDialog;

import android.Manifest;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;


import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;

import android.support.v7.graphics.Palette;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.transition.TransitionInflater;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener,View.OnClickListener,recycler_adapter.adaptr,
        ApplicationController.informactivity,SeekBar.OnSeekBarChangeListener,SlidingUpPanelLayout.PanelSlideListener{

    private final int read_external=11001;
    int menuid = 0;
    Boolean isplaying = false;

    //ui elements
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    NavigationView navigationView;
    TextView songname;
    TextView artistname;
    ImageButton button;
    pageradapter adapter;
    TabLayout tablayout;
    ViewPager viewpager;
    Toolbar toolbar;
    Toolbar card;
    AlertDialog.Builder builder;
    TextView nav_name;
    AppBarLayout appBarLayout;
    String path;
    Bitmap bitmap;
    SharedPreferences sharedPref;
    ApplicationController con;

    //slidinglayout
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total;
    SeekBar seekBar;
    String Current_time;

    ImageView imageslide,image;
    updateseekbar1 seekbarasync;
    songs current_song;
    Long setmax=0L;
    public boolean isrepeat,isshuffle;

    //for facebook
    //CallbackManager mCallbackmanager;
    ImageLoader imageLoader;
    de.hdodenhof.circleimageview.CircleImageView profileimage;
    /*LoginButton loginbtn;
    Profile profile;
    AccessToken token;
    AccessTokenTracker tokentracker;
    ProfileTracker profileTracker;
*/    CoordinatorLayout coordinatorlayout;
        LinearLayout slidercontrolcolour;
/*
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }*/
    ///------ --------- -------------  -------------   -------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("llllp", "oncreate");
        con = new ApplicationController(this.getApplicationContext(), this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String thme=sharedPref.getString("THEME_LIST","1") ;
        switch (thme){
            case "1":setTheme(R.style.AppTheme);break;
            case "2":setTheme(R.style.AppTheme_Purple);break;
            case "3":setTheme(R.style.AppTheme_Red);break;
            case "4":setTheme(R.style.AppTheme_orange);break;
            case "5":setTheme(R.style.AppTheme_indigo);break;
            case "6":setTheme(R.style.AppTheme_brown);break;
            default:setTheme(R.style.AppTheme);break;
        }

        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
        }else{
            if(con.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
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


        setSupportActionBar(toolbar);

        //to check if user revoke permissions while app running .
        if(con.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)){
            request_perm();
            //startActivity(new Intent(this,PermissionActivity.class));
        }else{
            con.loadsongswithimages();
        }

        //settings
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String sync = String.valueOf(sharedPref.getBoolean("check", false));

        setrepeatbutton();
        setshufflebutton();
        isrepeat=con.isRepeat();
        isshuffle=con.isShuffle();

        set_card_visibility();
        imageLoader = ImageLoader.getInstance();
        card.setContentInsetsAbsolute(0, 0);

        nav_tab_setup();

        //navigation header
        View v=navigationView.getHeaderView(0);
        profileimage=(CircleImageView)v.findViewById(R.id.profile_image);
        nav_name=(TextView)v.findViewById(R.id.nav_name) ;

        //for facebook
        //fb_setup();

        Log.i("llllp","on create end");
    }

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
        viewpager.setAdapter(adapter);
        viewpager.addOnPageChangeListener(this);
        tablayout.setupWithViewPager(viewpager);

    }
    private void initialise(){
        songname = (TextView) findViewById(R.id.bar_name);
        artistname = (TextView) findViewById(R.id.bar_artist);
        button = (ImageButton) findViewById(R.id.bar_button);
        viewpager = (ViewPager) findViewById(R.id.pager);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        card = (Toolbar) findViewById(R.id.controller_bar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        tablayout = (TabLayout) findViewById(R.id.tablayout);
        builder = new AlertDialog.Builder(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        coordinatorlayout=(CoordinatorLayout)findViewById(R.id.main_content);
        appBarLayout=(AppBarLayout) findViewById(R.id.MyAppbar);
        slidercontrolcolour=(LinearLayout) findViewById(R.id.grad_bottom_slide);

        //sliding player setup
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

    public Fragment getFragment(int position){
        return adapter.getFragment(position);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

                super.onBackPressed();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("cccc", "oncreateoptions menu");
        // Inflate the menu; this adds items to the action bar if it is present.
        if (menuid == 2) {
            Log.i("cccc", "menuid=2");
            getMenuInflater().inflate(R.menu.playlist, menu);
        } else {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_new_playlist) {
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
            alert.show();
            return true;
        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,settings.class));
            return true;
        }

        if (id == R.id.clear_history) {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.clearHistory();
            return true;
        }
        return false;
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

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }


    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if(position!=0){
            if(((home)getFragment(0)).mActionMode !=null){
                ((home)getFragment(0)).mActionMode.finish();
            }

        }
        menuid = position;
        invalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setbutton(boolean a) {
        if(slider.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED){
            return;
        }
        isplaying = a;
        if (a) {
            button.setImageResource(R.drawable.pause);

        } else {
           button.setImageResource(R.drawable.play);

        }
        seticon();
    }

    public void setcard(boolean a, songs song) {
        setbutton(a);
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
                    image.setImageBitmap(bitmap);
                    imageslide.setImageBitmap(bitmap);
                } else {
                    image.setImageResource(R.drawable.mp3full);
                    imageslide.setImageResource(R.drawable.mp3full);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    public void seticon(){
        if(isplaying){
            Log.i("llll","isplaying");
            //play_pause.setImageResource(R.drawable.pause_white);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause_white));
                Drawable drawable = play_pause.getDrawable();
                ((Animatable) drawable).start();
            }else{
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause_white));
            }

        }else{
            //play_pause.setImageResource(R.drawable.play_white);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play_white));
                Drawable drawable = play_pause.getDrawable();
                ((Animatable) drawable).start();
            }else{
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play_white));
            }
        }

    }
    public void refreshview(){

        Log.i("lllll","refresh");
        current_song=con.getsong();
        setdata();
    }
    public void setdata(){
        try {
            isplaying=con.isPlaying();
            Long timemilli=con.getDuration();

            int timesec=Integer.parseInt(String.valueOf(timemilli/1000L));
            Log.i("kkkk","total time of current song is :"+timesec/60+":"+timesec%60);
            total.setText(gettime(timesec));
            Current_time=gettime(Integer.parseInt(String.valueOf(con.getcurrentplaybacktime()/1000L)));

            current.setText(Current_time);
            Log.i("lllll","setdata");
            bitmap= BitmapFactory.decodeFile(current_song.getImagepath());

            if(bitmap!=null){
                image.setImageBitmap(bitmap);
               slidercontrolcolour.setBackground(ContextCompat.getDrawable(this,R.drawable.grad));
            }
            else {
                image.setImageResource(R.drawable.mp3full);
            }
            Log.i("mmmm","setdata: getDuration setmax"+(String.valueOf(Integer.parseInt(String.valueOf(timemilli/1000L)))));
            setmax=timemilli;
            seekBar.setMax(Integer.parseInt(String.valueOf(timemilli /1000L )));
            //seekBar.setMax(con.getDuration());
            Log.i("lllll","----"+String.valueOf(timemilli));
            seticon();

        }catch (Exception e){}
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

    public void setrepeatbutton(){
        if(isrepeat){
            repeat.setImageResource(R.drawable.repeat_selected);
        }else      repeat.setImageResource(R.drawable.repeat);
    }
    public void setshufflebutton(){
        if(isshuffle){
            shuffle.setImageResource(R.drawable.shuffle_selected);
        }else      shuffle.setImageResource(R.drawable.shuffle);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void setcardss(songs song) {

        set_card_visibility();
        Log.i("llll", "setcardss");
        setcard(true, song);
    }

    public void set_card_visibility(){
        Log.i("klkl", String.valueOf(con.isnull()) +"  "+String.valueOf(con.isPlaying()));

         if (!con.isnull() && con.getsong()!=null){
             int ii=this.getResources().getInteger(R.integer.panelheight);
             slider.setPanelHeight(ii);
             slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

/*
            card.setVisibility(View.VISIBLE);
            shadow.setVisibility(View.VISIBLE);
            */
        }else{
            slider.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }

    }
    @Override
    public void playnextsong() {
        isplaying = con.isPlaying();
        setcard(isplaying, con.getsong());

        //slide
        refreshview();
        seekBar.setProgress(0);
    }

    @Override
    public void refresh() {
        Log.i("bnbnn","minact refresh");

        isplaying = con.isPlaying();
        setcard(isplaying, con.getsong());
        refreshview();
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


    @Override
    public void updateprofileimage() {
        Log.i("qqqq", "updateprofileimage ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
        //tokentracker.stopTracking();
        //profileTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       // mCallbackmanager.onActivityResult(requestCode,resultCode,data);
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
                if (isplaying) {
                    isplaying = false;
                    con.pause();
                    //setbutton(false);
                    //button.setImageResource(R.drawable.play);
                    button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play));
                    Drawable drawable = button.getDrawable();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((Animatable) drawable).start();
                    }
                    seticon();
                } else {
                    isplaying = true;
                    con.resume();
                    //setbutton(true);
                    //button.setImageResource(R.drawable.pause);
                    button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause));
                    Drawable drawable = button.getDrawable();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((Animatable) drawable).start();
                    }
                    seticon();
                }
                return;
            }
            case R.id.previous :{
                Log.i("lllll","previous");
                seekBar.setProgress(0);
                con.playprev();
                refreshview();
                seekBar.setProgress(0);
                return;
            }
            case R.id.play_pause :{
                Log.i("mmmm","playpause");
                Log.i("mmmm",String.valueOf(con.getCurrentPosition())+"---"+String.valueOf(con.getlist().size()));
                isplaying=con.isPlaying();
                Log.i("mmmm",String.valueOf(isplaying));

                if(isplaying){
                    try{con.pause();isplaying=false;
                        seticon();
                    }catch (Exception e){}
                }else{
                    try{
                        if(con.getCurrentPosition()==0 && con.getlist()!=null ){
                            if(con.getlist().size()>0) {
                                if(con.getcurrentplaybacktime()>1000L){
                                    con.resume();
                                    isplaying=true;
                                    seticon();
                                }else {
                                    Log.i("mmmm", "playpause not playing -1 null");
                                    con.playsong(0);
                                    isplaying=true;
                                    seticon();
                                    refreshview();

                                }
                            }
                        }
                        else {
                            Log.i("mmmm","resuming");

                            con.resume();
                            isplaying = true;
                            seticon();
                        }
                    }catch (Exception e){}
                }
                return;
            }
            case R.id.next :{
                Log.i("mmmm","next---------------");

                Log.i("lllll","next");
                seekBar.setProgress(0);
                con.playnext();

                refreshview();
                seekBar.setProgress(0);
                return;
            }
            case R.id.repeat :{

                Log.i("mmmm","repeat");

                Log.i("mmmm","repeat: getDuration "+String.valueOf(con.getDuration()/1000L));
                if(isrepeat){
                    isrepeat=false;
                    con.setRepeat(false);
                    setrepeatbutton();
                }else{
                    isrepeat=true;
                    con.setRepeat(true);
                    setrepeatbutton();
                }
                return;
            }
            case R.id.shuffle :{
                Log.i("llll","shuffle");
                if(isshuffle){
                    isshuffle=false;
                    con.setShuffle(false);
                    setshufflebutton();
                }else{
                    isshuffle=true;
                    con.setShuffle(true);
                    setshufflebutton();
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
        /*
        //animation for tablayout
        //AnimatorSet set=new AnimatorSet();
        int size=this.getResources().getInteger(R.integer.animationsize);
        ObjectAnimator object1=ObjectAnimator.ofFloat(tablayout,"translationY",-size,0);
        object1.setDuration(500);
        object1.start();
        //set.playTogether(object1);
        //set.start();
*/
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
        seticon();
        Log.i("llllp","on resume end");
    }


    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    //to prevent user from opening navigation view while in action mode
    public void lockdrawer(){
        if(drawer!=null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            String currentDateandTime = sdf.format(new Date());
            Log.i("colortiming","tablayout color change "+currentDateandTime);
            changecolor change=new changecolor();
            //0 for lock 1 for unlock
            change.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,0);
        }
    }

    //to restore navigation view after action mode
    public void releasedrawer(){
        if(drawer!=null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            String currentDateandTime = sdf.format(new Date());
            Log.i("colortiming","tablayout color restore "+currentDateandTime);

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

}
