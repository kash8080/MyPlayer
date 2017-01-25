package com.androidplay.rahul.myplayer;

import android.Manifest;
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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class Now_playing extends AppCompatActivity implements recycler_adapter.adaptr,View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,SlidingUpPanelLayout.PanelSlideListener{


    Toolbar toolbar;
    RecyclerView recview;
    RecyclerView.LayoutManager mlayoutManager;
    recycler_adapter adapter;
    ArrayList<songs> now_list;
    ApplicationController con;
    ItemTouchHelper ith;
    String method="";
    String artistname="";

    //slidinglayout
    Toolbar card;
    LinearLayout slidercontrolcolour;
    String path;
    boolean canrun=true;
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total,songname,artistnamebar;
    SeekBar seekBar;
    String Current_time;
    ImageButton button;
    Bitmap bitmap;
    ImageView imageslide,imagebar;
    updateseekbar1 seekbarasync;
    songs current_song;
    Long setmax=0L;
    public boolean isrepeat,isshuffle;
    boolean isplaying=false;
    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        con=new ApplicationController(this.getApplicationContext(),this);

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
            if(con.needforpermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
                startActivity(new Intent(this, MainActivity.class));
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
        toolbar=(Toolbar)findViewById(R.id.now_toolbar);
        toolbar.setBackgroundColor(con.getPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recview=(RecyclerView)findViewById(R.id.now_recview);
        mlayoutManager=new LinearLayoutManager(this);
        recview.setLayoutManager(mlayoutManager);

        if(method!=null && method.equals("artistsongs")){
            String selection= MediaStore.Audio.Media.ARTIST+" = ? ";
            String[] args=new String[]{artistname};
            now_list=DataFetch.getSongsOfArtist(this,selection,args);
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
            adapter=new recycler_adapter(this,now_list,"now_playing");
            recview.setAdapter(adapter);

            VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller)findViewById(R.id.fast_scroller3);
            fastScroller.setRecyclerView(recview);
            recview.addOnScrollListener(fastScroller.getOnScrollListener());

            ItemTouchHelper.SimpleCallback _ithCallback=new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,ItemTouchHelper.LEFT ){
                //and in your imlpementaion of
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    // get the viewHolder's and target's positions in your adapter data, swap them
                    int from=viewHolder.getAdapterPosition();
                    int to=target.getAdapterPosition();
                    int current=con.getCurrentPosition();

                    con.notifydatachange(0,from,to);
                    if(from<current && to>=current){
                        // current move up
                        current--;
                        con.setCurrent_pos(current);
                    }
                    else if(from>current && to<=current) {
                        //  current  move down
                        current++;
                        con.setCurrent_pos(current);

                    }else if(from==current){
                        //current = to
                        current=to;
                        con.setCurrent_pos(current);

                    }
                    adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());


                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int i=viewHolder.getAdapterPosition();
                    if(direction==4) {
                        adapter.notifyItemRemoved(i);
                        con.remove_song(i);
                    }
                }
            };
            ith = new ItemTouchHelper(_ithCallback);
            ith.attachToRecyclerView(recview);

        }


        //slider
        set_card_visibility();
        card.setContentInsetsAbsolute(0, 0);
        connectControllerToSession(con.getMediaSessionToken());


    }

    public void inititalise(){

        //slider
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
            default:return super.onOptionsItemSelected(item);
        }
        return true;
    }


    //adapter method
    @Override
    public void setcardss(songs song) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        setrepeatbutton(false);
        setshufflebutton(false);
        controllerCompat.registerCallback(callback);
        currentPlaybackstate=controllerCompat.getPlaybackState();
        currentmetadata=controllerCompat.getMetadata();
        set_card_visibility();

        try {
            if (con.isPlaying()) {
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

        seekbarasync =new updateseekbar1();
        seekbarasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        isplaying=con.isPlaying();
        refreshPanel();
        seticon(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        canrun=false;
        super.onPause();

        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
        controllerCompat.unregisterCallback(callback);

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
                Log.i("mmmm","playpause");
                // Log.i("mmmm",String.valueOf(con.getCurrentPosition())+"---"+String.valueOf(con.getlist().size()));
                currentPlaybackstate=controllerCompat.getPlaybackState();
                if(currentPlaybackstate!=null) {
                    Log.i("mjkl", "current playback state is not null play_pause click");

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
                        default:
                            Log.i("mjkl", "unhandled state " + currentPlaybackstate.getState());

                    }
                }else{
                    Log.i("mjkl", "current playback state is null play_pause click");

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
            case R.id.repeat:{

                Log.i("mmmm","repeat");

                Log.i("mmmm","repeat: getDuration "+String.valueOf(con.getDuration()/1000L));
                if(isrepeat){
                    isrepeat=false;
                    con.setRepeat(false);
                    setrepeatbutton(true);
                }else{
                    isrepeat=true;
                    con.setRepeat(true);
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

    public void seticon(boolean a){
        Log.i("seticon","start---");

        isplaying=con.isPlaying();
        if(isplaying){
            Log.i("seticon","isplaying");
            //play_pause.setImageResource(R.drawable.pause_white);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && a && slider.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause_white));
                Drawable drawable = play_pause.getDrawable();
                ((Animatable) drawable).start();
            }else{
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause_white));
            }

        }else{
            Log.i("seticon","!isplaying");

            //play_pause.setImageResource(R.drawable.play_white);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && a &&  slider.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
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
        Log.i("seticon","setbutto start---");

        isplaying=con.isPlaying();
        if (isplaying) {
            Log.i("seticon","isplaying button");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && a &&  slider.getPanelState()== SlidingUpPanelLayout.PanelState.COLLAPSED) {
                button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause));
                Drawable drawable = button.getDrawable();
                ((Animatable) drawable).start();
            }else{
                button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause));
            }
        } else {
            Log.i("seticon","!isplaying button");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && a &&  slider.getPanelState()== SlidingUpPanelLayout.PanelState.COLLAPSED) {
                button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play));
                Drawable drawable = button.getDrawable();
                ((Animatable) drawable).start();
            }else{
                button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play));
            }
        }
        Log.i("seticon","setbuttn end---");

    }
    public void setrepeatbutton(boolean animation){
        isrepeat=con.isRepeat();
        if(isrepeat){
            repeat.setImageResource(R.drawable.repeat_selected);
        }else      repeat.setImageResource(R.drawable.repeat);

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
    public void refreshPanel(){

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
                        .into(imagebar);
                slidercontrolcolour.setVisibility(View.VISIBLE);
                slidercontrolcolour.setBackground(ContextCompat.getDrawable(this,R.drawable.grad));
            }
            else {
                slidercontrolcolour.setVisibility(View.VISIBLE);

                //image.setImageResource(R.drawable.mp3full);
                Picasso.with(this)
                        .load(R.drawable.guitar)
                        .error(R.drawable.mp3full)
                        .into(imagebar);
            }
            Log.i("mmmm","setdata: getDuration setmax"+(String.valueOf(Integer.parseInt(String.valueOf(timemilli/1000L)))));
            setmax=timemilli;
            seekBar.setMax(timesec);
            seekBar.setProgress(timecurrentsec);
            //seekBar.setMax(con.getDuration());
            Log.i("lllll","----"+String.valueOf(timemilli));
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
    public void setcard(boolean a, songs song) {
        seticon(false);
        path = song.getImagepath();
        if (song != null) {
            Log.i("klkl", "song!=null..setting card");
            try {
                String name = song.getName();
                String artist = song.getArtist();
                songname.setText(name);
                artistnamebar.setText(artist);
                Bitmap bitmap = BitmapFactory.decodeFile(song.getImagepath());
                if (bitmap != null) {
                    //image.setImageBitmap(bitmap);
                    //imageslide.setImageBitmap(bitmap);
                    Picasso.with(this)
                            .load(Uri.parse("file://"+song.getImagepath()))
                            .error(R.drawable.mp3full)
                            .into(imagebar);
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
                            .into(imagebar);
                    Picasso.with(this)
                            .load(R.drawable.guitar)
                            .error(R.drawable.mp3full)
                            .into(imageslide);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            imagebar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
    public void set_card_visibility(){
        Log.i("klkl", String.valueOf(con.isnull()) +"  "+String.valueOf(con.isPlaying()));

        if (!con.isnull() && con.getsong()!=null){
            int ii=this.getResources().getInteger(R.integer.panelheight);
            slider.setPanelHeight(ii);
            slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else{
            slider.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }

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

    public void setmetadata(MediaMetadataCompat metadataCompat){
        refreshPanel();
    }
    public void setstate(PlaybackStateCompat stateCompat){
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
            Log.i("mjkl", "setstate state is null");
            seticon(false);
        }
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
    public class updateseekbar1 extends AsyncTask<Void,Void,Void> {

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


}
