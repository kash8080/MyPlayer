package com.example.rahul.myplayer;

import android.app.AlertDialog;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;


import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;

import android.support.v7.widget.CardView;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener,View.OnClickListener,recycler_adapter.adaptr,
        ApplicationController.informactivity,SeekBar.OnSeekBarChangeListener {

    AudioManager audioManager;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    FragmentTransaction transaction;
    NavigationView navigationView;
    int menuid = 0;
    TextView songname;
    TextView artistname;
    ImageView image;
    ImageButton button;
    pageradapter adapter;
    TabLayout tablayout;
    ViewPager viewpager;
    Toolbar toolbar;
    Boolean isplaying = false;
    //
     ApplicationController con;
    String path;
    View shadow;
    Toolbar card;
    AlertDialog.Builder builder;
    Uri profileuri;
    Bitmap bitmap;
    TextView nav_name;
    SharedPreferences sharedPref;


    //slidinglayout
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total;
    SeekBar seekBar;
   // Bitmap bitmapslide;
    ImageView imageslide;
    updateseekbar1 seekbarasync;
    songs current_song;
    Long setmax=0L;
    public boolean isrepeat,isshuffle;

    //for facebook
    CallbackManager mCallbackmanager;
    ImageLoader imageLoader;
    de.hdodenhof.circleimageview.CircleImageView profileimage;
    LoginButton loginbtn;
    Profile profile;
    AccessToken token;
    AccessTokenTracker tokentracker;
    ProfileTracker profileTracker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("klkl", "oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        songname = (TextView) findViewById(R.id.bar_name);
        artistname = (TextView) findViewById(R.id.bar_artist);
        image = (ImageView) findViewById(R.id.bar_image);
        button = (ImageButton) findViewById(R.id.bar_button);
        viewpager = (ViewPager) findViewById(R.id.pager);
        con = new ApplicationController(this.getApplicationContext(), this);
        card = (Toolbar) findViewById(R.id.controller_bar);

        //sliding player setup
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

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String sync = String.valueOf(sharedPref.getBoolean("check", false));

        Log.i("sttng",sync);

        play_pause.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        repeat.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);

        setrepeatbutton();
        setshufflebutton();

        isrepeat=con.isRepeat();
        isshuffle=con.isShuffle();


        set_card_visibility();
        card.setOnClickListener(this);

        // sliding layout setup end



        button.setOnClickListener(this);
        builder = new AlertDialog.Builder(this);
         imageLoader = ImageLoader.getInstance();


        //card.setVisibility(View.INVISIBLE);
        //shadow.setVisibility(View.INVISIBLE);


       // askaudio();
        card.setContentInsetsAbsolute(0, 0);
        ////////// //navigation view setup
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//tabs setup/////////////
        adapter = new pageradapter(getSupportFragmentManager());
        viewpager.setAdapter(adapter);
        tablayout = (TabLayout) findViewById(R.id.tablayout);
        tablayout.addTab(tablayout.newTab().setText("first"));
        tablayout.addTab(tablayout.newTab().setText("second"));
        tablayout.addTab(tablayout.newTab().setText("third"));
        tablayout.setTabGravity(tablayout.GRAVITY_FILL);
        viewpager.addOnPageChangeListener(this);
        //tablayout.setOnTabSelectedListener(this);
        tablayout.setupWithViewPager(viewpager);

//remove extra margin left space

//-------------------can use shared preference here----------

        View v=navigationView.getHeaderView(0);
        profileimage=(de.hdodenhof.circleimageview.CircleImageView)v.findViewById(R.id.profile_image);
        nav_name=(TextView)v.findViewById(R.id.nav_name) ;


        //for facebook
        token=AccessToken.getCurrentAccessToken();
        profile=Profile.getCurrentProfile();

        mCallbackmanager= CallbackManager.Factory.create();
        loginbtn=(com.facebook.login.widget.LoginButton)navigationView.findViewById(R.id.nav_login_button);
        try{
            loginbtn.registerCallback(mCallbackmanager,fbcallback);

            //loginbtn.setReadPermissions("user_friends");
        }catch (Exception e){e.printStackTrace();}
        settokenandprofile();

        if(isFacebookLoggedIn()){
            con.setloginvalue(true);
        }else con.setloginvalue(false);


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
            Log.i("cccc", "menuid!=2");
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_new_playlist) {
            builder.setTitle("Playlist name");
            builder.setCancelable(true);
            // Set up the input
            //final EditText input = new EditText(this);

            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            View v = getLayoutInflater().inflate(R.layout.dialog, null);
            final EditText input = (EditText) v.findViewById(R.id.newplaylistname);

            builder.setView(v);
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


/*

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noti)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!");

            Intent resultIntent = new Intent(this, player_n.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(player_n.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// 0 allows you to update the notification later on.
            mNotificationManager.notify(0, mBuilder.build());

*/



        return false;
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
       // } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_settings) {

            startActivity(new Intent(this,settings.class));
        } else if (id == R.id.nav_login_button) {

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
        menuid = position;
        invalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.controller_bar: {

                Intent intent = new Intent(this, playerr.class);
                startActivity(intent);
                return;
            }

            case R.id.bar_button: {
                if (isplaying) {
                    isplaying = false;
                    con.pause();
                    button.setImageResource(R.drawable.play);
                } else {
                    isplaying = true;
                    con.resume();
                    button.setImageResource(R.drawable.pause);

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

    public void setbutton(boolean a) {
        isplaying = a;
        if (a) {
            button.setImageResource(R.drawable.pause);
        } else {
            button.setImageResource(R.drawable.play);
        }
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
                } else {
                    image.setImageResource(R.drawable.mp3);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    //---
    public void createnewplaylist(String playlistname) {
        ContentValues mInserts = new ContentValues();
        mInserts.put(MediaStore.Audio.Playlists.NAME, playlistname);
        mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        this.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
    }





    @Override
    protected void onResume() {

        super.onResume();
        con = new ApplicationController(this.getApplicationContext(), this);



        //for facebook
        tokentracker.startTracking();
        profileTracker.startTracking();
        if(isFacebookLoggedIn()){
        if(con.getProfilepic()==null) {
            profile = Profile.getCurrentProfile();

            if (profile != null) {
                //refreshview
                con.setprofilepic(String.valueOf(profile.getProfilePictureUri(100, 100)));
                Log.i("qqqq", String.valueOf(profile.getProfilePictureUri(100, 100)));

            }
          }
            setprofileimage();
        }
        //updateprofileimage();

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

        } catch (Exception e) {
        }




        if(con.getCurrentPosition()==-1){
            con.setCurrent_pos(0);

        }

        Log.i("kkkk","onresume---------------");
       // Log.i("mmmm",String.valueOf(con.getCurrentPosition())+"---"+String.valueOf(con.getlist().size()));

        seekbarasync =new updateseekbar1();
        seekbarasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        super.onResume();
        Log.i("llll","on resume :conisplaying"+con.isPlaying());
        isplaying=con.isPlaying();
        refreshview();
        seticon();
    }


    public void seticon(){
        if(isplaying){
            Log.i("llll","isplaying");
            play_pause.setImageResource(R.drawable.pause_white);

        }else play_pause.setImageResource(R.drawable.play_white);

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
            current.setText("0:00");
            Log.i("lllll","setdata");
            bitmap= BitmapFactory.decodeFile(current_song.getImagepath());

            if(bitmap!=null){image.setImageBitmap(bitmap);}
            else {image.setImageResource(R.drawable.mp3full);}
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
             slider.setPanelHeight(100);

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
            }
            Log.i("mmmm","background: getDuration setmax"+String.valueOf(dur));

            String crnt=gettime(Integer.parseInt(String.valueOf(con.getcurrentplaybacktime()/1000L)));
            Log.i("kkkk","current string:"+crnt);
            current.setText(crnt);
            // Log.i("kkkk","getcurrentplaybacktime:"+String.valueOf(con.getcurrentplaybacktime()/1000L));
            seekBar.setProgress(Integer.parseInt(String.valueOf(con.getcurrentplaybacktime()/1000L)));
            Log.i("kkkk","updateseekbar");
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





    //////////////////facebook login methods
    FacebookCallback<LoginResult> fbcallback=new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            token=loginResult.getAccessToken();
            profile=Profile.getCurrentProfile();
            if(profile!=null){
               // refreshview
                con.setloginvalue(true);
                con.setprofilepic(String.valueOf(profile.getProfilePictureUri(100,100)));
            }

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {

        }
    };

    public void settokenandprofile(){
        tokentracker=new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.i("qqqq"," istracking"+String.valueOf(profileTracker.isTracking())+"access token changed");
                if(AccessToken.getCurrentAccessToken()!=null) {

                    ApplicationController.setloginvalue(true);
                }
                else {
                    ApplicationController.setloginvalue(false);
                }


            }
        };
        profileTracker =new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                Log.i("qqqq"," istracking"+String.valueOf(profileTracker.isTracking())+"current profilechanged");

                profile=currentProfile.getCurrentProfile();
                if(profile!=null){
                    //refreshview
                    updateprofileimage();
                }
            }
        };

    }
    public boolean isFacebookLoggedIn(){
        return AccessToken.getCurrentAccessToken() != null;
    }
    public void setprofileimage(){
        Log.i("qqqq","setprofileimage ");
        nav_name.setText(profile.getName());
        imageLoader.displayImage(con.getProfilepic(),profileimage);
    }

    @Override
    public void updateprofileimage() {
        Log.i("qqqq","updateprofileimage ");

        if(con.getloginvalue()){
            setprofileimage();
        }else{
            nav_name.setText("");
            profileimage.setImageResource(R.mipmap.ic_launcher);
        }
    }

    ////////////////////////////////////////facebook login methods end


    @Override
    protected void onPause() {
        super.onPause();
        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
        tokentracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackmanager.onActivityResult(requestCode,resultCode,data);
    }




}
