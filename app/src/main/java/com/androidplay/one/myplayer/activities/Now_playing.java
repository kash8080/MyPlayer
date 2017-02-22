package com.androidplay.one.myplayer.activities;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
//import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.DataFetch;
import com.androidplay.one.myplayer.SliderImagePagerAdapter;
import com.androidplay.one.myplayer.helper_classes.OnSwipeTouchListener;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.helper_classes.SliderHelper;
import com.androidplay.one.myplayer.helper_classes.ThemeHelper;
import com.androidplay.one.myplayer.recycler_adapter;
import com.androidplay.one.myplayer.songs;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class Now_playing extends AppCompatActivity implements View.OnClickListener{


    Toolbar toolbar;
    RecyclerView recview;
    RecyclerView.LayoutManager mlayoutManager;
    recycler_adapter adapter;
    ArrayList<songs> now_list;
    ApplicationController con;
    ItemTouchHelper ith;
    String method="";
    String artistname="";
    ImageView main_backgroundimage;

    //slidinglayout
    Toolbar card;
    //updateseekbar1 seekbarasync;

    ViewPager sliderviewpager;
    SliderImagePagerAdapter sliderImagePagerAdapter;
    ImageButton previous,next,play_pause,repeat,shuffle,button;
    ImageView imageslide,imagebar;
    TextView current,total,songname,artistnamebar;
    SlidingUpPanelLayout slider;

    LinearLayout slidercontrolcolour;
    SeekBar seekBar;

    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;

    SharedPreferences sharedPref;
    String theme_no;
    Boolean dark;
    ThemeHelper themeHelper;

    SliderHelper sliderHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        con=new ApplicationController(this.getApplicationContext(),this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        theme_no=sharedPref.getString("THEME_LIST","1") ;
        dark = sharedPref.getBoolean("check", false);
        themeHelper=new ThemeHelper(this);
        themeHelper.setthemecolours();

        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
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
        setContentView(R.layout.activity_now_playing);

        now_list=new ArrayList<>();

        Intent intent=getIntent();
        if(intent!=null){
            method=intent.getStringExtra("method");
            artistname=intent.getStringExtra("artistname");
        }
        inititalise();
        toolbar.setBackgroundColor(con.getPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        themeHelper.setthemeAndBackground(main_backgroundimage,toolbar);

        mlayoutManager=new LinearLayoutManager(this);
        recview.setLayoutManager(mlayoutManager);

        if(method!=null && method.equals("artistsongs")){
            getArtistsongs getartistsongs=new getArtistsongs(artistname);
            getartistsongs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            getSupportActionBar().setTitle(artistname);
            adapter=new recycler_adapter(this,now_list,"allsongs_noanimation");
            recview.setAdapter(adapter);

            VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller)findViewById(R.id.fast_scroller3);
            fastScroller.setRecyclerView(recview);
            recview.addOnScrollListener(fastScroller.getOnScrollListener());
            fastScroller.setVisibility(View.INVISIBLE);

        }else {
            now_list = con.getlist();
            getSupportActionBar().setTitle("Now Playing");
            adapter = new recycler_adapter(this, now_list, "now_playing");
            recview.setAdapter(adapter);

            VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller3);
            SectionTitleIndicator sectionTitleIndicator =(SectionTitleIndicator)
                    findViewById(R.id.fast_scroller_section_title_indicator);
            fastScroller.setRecyclerView(recview);
            recview.addOnScrollListener(fastScroller.getOnScrollListener());
            // Connect the section indicator to the scroller
            fastScroller.setSectionIndicator(sectionTitleIndicator);


            ItemTouchHelper.SimpleCallback _ithCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                //and in your imlpementaion of
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    // get the viewHolder's and target's positions in your adapter data, swap them
                    int from = viewHolder.getAdapterPosition();
                    int to = target.getAdapterPosition();
                    int current = con.getCurrentPosition();

                    con.notifydatachange(0, from, to);
                    if (from < current && to >= current) {
                        // current move up
                        current--;
                        con.setCurrent_pos(current);
                    } else if (from > current && to <= current) {
                        //  current  move down
                        current++;
                        con.setCurrent_pos(current);

                    } else if (from == current) {
                        //current = to
                        current = to;
                        con.setCurrent_pos(current);

                    }
                    adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());


                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int i = viewHolder.getAdapterPosition();
                    if (direction == 4) {
                        adapter.notifyItemRemoved(i);
                        con.remove_song(i);
                    }
                }
            };
            ith = new ItemTouchHelper(_ithCallback);
            ith.attachToRecyclerView(recview);

        }

        //slider viewpager setup
        sliderImagePagerAdapter=new SliderImagePagerAdapter(getSupportFragmentManager());
        sliderviewpager.setAdapter(sliderImagePagerAdapter);
        //sliderviewpager.addOnPageChangeListener(pageChangeListener);


        sliderHelper=new SliderHelper(this,sliderviewpager,sliderImagePagerAdapter,slider);
        sliderHelper.setImagebuttons(play_pause,repeat,shuffle,button,previous,next);
        sliderHelper.setImageViews(imageslide,imagebar);
        sliderHelper.setTextViews(current,total,songname,artistnamebar);
        sliderHelper.setRest(slidercontrolcolour,seekBar);

        //set_card_visibility();
        sliderHelper.set_card_visibility();
        card.setContentInsetsAbsolute(0, 0);
        connectControllerToSession(con.getMediaSessionToken());



    }

    public void inititalise(){
        toolbar=(Toolbar)findViewById(R.id.now_toolbar);
        recview=(RecyclerView)findViewById(R.id.now_recview);

        //slider
        sliderviewpager=(ViewPager)findViewById(R.id.sliderviewpager);
        card = (Toolbar) findViewById(R.id.controller_bar);
        slidercontrolcolour=(LinearLayout) findViewById(R.id.grad_bottom_slide);
        songname = (TextView) findViewById(R.id.bar_name);
        artistnamebar = (TextView) findViewById(R.id.bar_artist);
        button = (ImageButton) findViewById(R.id.bar_button);
        imagebar = (ImageView) findViewById(R.id.bar_image);
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
        main_backgroundimage = (ImageView) findViewById(R.id.main_background_image);

        play_pause.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        repeat.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.setOnClickListener(this);
        button.setOnClickListener(this);

        /*
        seekBar.setOnSeekBarChangeListener(this);
        slider.addPanelSlideListener(this);
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(!(method!=null && method.equals("artistsongs"))) {
            getMenuInflater().inflate(R.menu.now_playing_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        switch (id){
            case android.R.id.home:{
                    finish();
                    break;
                }
            case R.id.nowplayingmenu_removeall:{
                AlertDialog.Builder builder =new AlertDialog.Builder(this);

                builder.setMessage("Are you sure you want to clear the queue?");
                builder.setPositiveButton(
                        "yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                adapter.songs_list=new ArrayList<>();
                                con.setMylist(new ArrayList<songs>(),"queue cleared",false);
                                adapter.notifyDataSetChanged();
                            }
                        });

                builder.setNegativeButton(
                        "no",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog=builder.create();
                dialog.show();

            }
            case R.id.nowplayingmenu_saveAsPlaylist:{
                DataFetch dataFetch=new DataFetch(this);
                dataFetch.AddtoPlaylist(now_list);
            }
            default:return super.onOptionsItemSelected(item);
        }
        return true;
    }


    public class getArtistsongs extends AsyncTask<Void,Void,Void>{
        ProgressDialog progress=new ProgressDialog(Now_playing.this);
        int typeBar=0;
        private String artistname;
        ArrayList<songs> newlist;
        public getArtistsongs(String artistname) {
            this.artistname=artistname;
        }

        @Override
        protected void onPreExecute() {
            progress.setMessage("Please wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(typeBar);
            progress.setIndeterminate(true);
            progress.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String selection= MediaStore.Audio.Media.ARTIST+" = ? ";
            String[] args=new String[]{artistname};
            newlist= DataFetch.getSongsOfArtist(Now_playing.this,selection,args);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            now_list.clear();
            now_list.addAll(newlist);
            adapter.notifyDataSetChanged();
            progress.dismiss();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        con.activityOnResume();

        //setrepeatbutton(false);
        //setshufflebutton(false);
        sliderHelper.setrepeatbutton(false);
        sliderHelper.setshufflebutton(false);

        controllerCompat.registerCallback(callback);
        currentPlaybackstate=controllerCompat.getPlaybackState();
        currentmetadata=controllerCompat.getMetadata();
        sliderHelper.set_card_visibility();

        try {
            if (con.isPlaying()) {
                //setcard(true, con.getsong());
                sliderHelper.setcard(true, con.getsong());
            } else {
                songs song = con.getsong();
                if (song != null) {
                    //setcard(false, song);
                    sliderHelper.setcard(false, song);
                } else {
                    //setcard(false, null);
                    sliderHelper.setcard(false,null);
                }
            }
        } catch (Exception e) {}
/*
        seekbarasync =new updateseekbar1();
        seekbarasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        */sliderHelper.startseekbarasync();

        //isplaying=con.isPlaying();
        //refreshPanel();
        sliderHelper.refreshPanel();
        //seticon(false);
        sliderHelper.seticon(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //canrun=false;
        super.onPause();

        sliderHelper.cancelSeekbarAsync();
       /* seekbarasync.cancel(true);
        seekbarasync.canrun=false;
       */ controllerCompat.unregisterCallback(callback);

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
                if(con.musicSrv.msession==null){
                    gettoken gettokenn=new gettoken();
                    gettokenn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if(con.isPlaying()){
                    con.pause();
                }else{
                    con.resume();
                }

                return;
            }

            case R.id.play_pause :{
                Log.i("mmmm","playpause");
                if(con.musicSrv.msession==null){
                    gettoken gettokenn=new gettoken();
                    gettokenn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if(con.isPlaying()){
                    con.pause();
                }else{
                    con.resume();
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
            case R.id.repeat:{

                Log.i("mmmm","repeat");

                Log.i("mmmm","repeat: getDuration "+String.valueOf(con.getDuration()/1000L));
               /* isrepeat=con.isRepeat();
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
                }*/
                sliderHelper.repeatButtonClicked();
                return;
            }
            case R.id.shuffle :{
                Log.i("llll","shuffle");
               /* isshuffle=con.isShuffle();
                if(isshuffle){
                    isshuffle=false;
                    con.setShuffle(false);
                    setshufflebutton(true);
                }else{
                    isshuffle=true;
                    con.setShuffle(true);
                    setshufflebutton(true);
                }*/
                sliderHelper.shuffleButtonClicked();
                return;
            }
            default: return;

        }
    }





    // slider functions
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
    private void connectControllerToSession(MediaSessionCompat.Token token) {
        try {
            controllerCompat=new MediaControllerCompat(this,token);
            controllerCompat.registerCallback(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        PlaybackStateCompat stateCompat=controllerCompat.getPlaybackState();
        MediaMetadataCompat metadataCompat=controllerCompat.getMetadata();
        currentPlaybackstate=stateCompat;
        currentmetadata=metadataCompat;
        setstate(stateCompat);
        setmetadata(metadataCompat);
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

    public void setmetadata(MediaMetadataCompat metadataCompat){
        //refreshPanel();
        sliderHelper.refreshPanel();
        adapter.notifyDataSetChanged();
    }
    public void setstate(PlaybackStateCompat stateCompat){
        if(stateCompat!=null) {
            Log.i("mjkl", "setstate state is not null");
            switch (stateCompat.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    Log.i("mjkl", "set state playing state ");
                    //seticon(true);
                    sliderHelper.seticon(true);
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.i("mjkl", "set state paused state ");
                    //seticon(true);
                    sliderHelper.seticon(true);
                    break;
                }
                default:
            }
        }else{
            Log.i("mjkl", "setstate state is null");
            //seticon(false);
            sliderHelper.seticon(true);

        }
        adapter.notifyDataSetChanged();

    }

}
