package com.androidplay.rahul.myplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class playerr extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{

    Toolbar toolbar;
    boolean isplaying=false;
    ApplicationController con;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total;
    SeekBar seekBar;
    Bitmap bitmap;
    ImageView image;
    updateseekbar1 seekbarasync;
    songs current_song;
    Long setmax=0L;
    public boolean isshuffle;
    String Current_time;
    public int viewupdater=0;
    int isrepeat=0;
    View back_colour;
    LinearLayout gradient_back;
    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;

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
        con=new ApplicationController(this.getApplicationContext(),this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String thme=sharedPref.getString("THEME_LIST","1") ;
        switch (thme){
            case "1":setTheme(R.style.AppTheme);break;
            case "2":setTheme(R.style.AppTheme_Purple);break;
            case "3":setTheme(R.style.AppTheme_Red);break;
            case "4":setTheme(R.style.AppTheme_orange);break;
            case "5":setTheme(R.style.AppTheme_indigo);break;
            case "6":setTheme(R.style.AppTheme_brown);break;
            default:setTheme(R.style.AppTheme);break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition enterTrans=new Explode();
            getWindow().setEnterTransition(enterTrans);
        }
        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
        }else{
            if(con.needforpermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
                startActivity(new Intent(this, PermissionActivity.class));
                //finish called to stop further proccess of this activity
                finish();
                return;
            }else{
                super.onCreate(savedInstanceState);
            }

        }
        setContentView(R.layout.activity_playerr);
        Log.i("kkkk","oncreate---------------");

        initialise();

        image.setOnTouchListener(new OnSwipeTouchListener(this) {
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
        //-----------kk-k--k
        connectControllerToSession(con.getMediaSessionToken());

    }

    public void initialise(){
        toolbar=(Toolbar)findViewById(R.id.player_toolbar);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){e.printStackTrace();}
        Log.i("llll","on create :conisplaying"+con.isPlaying());

        previous=(ImageButton)findViewById(R.id.previous);
        next=(ImageButton)findViewById(R.id.next);
        play_pause=(ImageButton)findViewById(R.id.play_pause);
        repeat=(ImageButton)findViewById(R.id.repeat);
        shuffle=(ImageButton)findViewById(R.id.shuffle);
        current=(TextView) findViewById(R.id.current_time);
        total=(TextView) findViewById(R.id.total_time);
        seekBar=(SeekBar)findViewById(R.id.seekbar);
        image=(ImageView)findViewById(R.id.player_image);
        back_colour=findViewById(R.id.foreground_image_colour);
        gradient_back=(LinearLayout)findViewById(R.id.gradient_back);

        isrepeat=con.isRepeat();
        isshuffle=con.isShuffle();

        // seekbarasync.execute();

        play_pause.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        repeat.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        image.setScaleType(ImageView.ScaleType.CENTER_CROP);


    }
    private void connectControllerToSession(MediaSessionCompat.Token token) {
        try {
            controllerCompat=new MediaControllerCompat(this,con.getMediaSessionToken());
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

    public void setmetadata(MediaMetadataCompat metadataCompat){
        refreshview();
    }
    public void setstate(PlaybackStateCompat stateCompat){
        if(stateCompat!=null) {
            switch (stateCompat.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    Log.i("mjkl", "state playing state ");
                    isplaying=true;
                    seticon(true);
                    break;
                }
                case PlaybackStateCompat.STATE_NONE: {
                    Log.i("mjkl", "none state state ");
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.i("mjkl", "paused state ");
                    isplaying=false;
                    seticon(true);
                    break;
                }
                case PlaybackStateCompat.STATE_BUFFERING: {
                    Log.i("mjkl", "buffering state ");
                    break;
                }
                default:
                    Log.i("mjkl", "unhandled state " + stateCompat.getState());
            }
        }else{
            isplaying=false;
            seticon(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setrepeatbutton(false);
        setshufflebutton(false);
        seticon(false);
        controllerCompat.registerCallback(callback);
        currentPlaybackstate=controllerCompat.getPlaybackState();

        currentmetadata=controllerCompat.getMetadata();
        if(con.getCurrentPosition()==-1){
            con.setCurrent_pos(0);
        }

        Log.i("kkkk","onresume---------------");

        seekbarasync =new updateseekbar1();
        seekbarasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        isplaying=con.isPlaying();
        refreshview();

    }

    @Override
    protected void onPause() {
        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
        controllerCompat.unregisterCallback(callback);
        super.onPause();
    }

    public void seticon(boolean animation){

        if(isplaying){
            Log.i("llll","isplaying");
            //play_pause.setImageResource(R.drawable.pause_white);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && animation) {
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause_white));
                Drawable drawable = play_pause.getDrawable();
                ((Animatable) drawable).start();
            }else{
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause_white));
            }

        }else{
            //play_pause.setImageResource(R.drawable.play_white);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && animation) {
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play_white));
                Drawable drawable = play_pause.getDrawable();
                ((Animatable) drawable).start();
            }else{
                play_pause.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play_white));
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.playerr,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id =item.getItemId();
        switch (id){
            case R.id.queue:{
                Log.i("lllll","queue called");
                startActivity(new Intent(this,Now_playing.class));
                return true;
            }case android.R.id.home:{
                finish();
                return true;
            }
            default:return super.onOptionsItemSelected(item);
        }
    }

    public void refreshview(){


        Log.i("lllll","refresh");
        current_song=con.getsong();
        setdata();
    }

    public void setdata(){
        try {
            Long timemilli=con.getDuration();
            int currtm=Integer.parseInt(String.valueOf(con.getcurrentplaybacktime()/1000L));
            int timesec=Integer.parseInt(String.valueOf(timemilli/1000L));
            Log.i("kkkk","total time of current song is :"+timesec/60+":"+timesec%60);
            total.setText(gettime(timesec));
            Current_time=gettime(currtm);
            current.setText(Current_time);
            Log.i("lllll","setdata");
            getSupportActionBar().setTitle(current_song.getName());
            getSupportActionBar().setSubtitle(current_song.getArtist());
            bitmap= BitmapFactory.decodeFile(current_song.getImagepath());
            setmax=timemilli;
            seekBar.setMax(Integer.parseInt(String.valueOf(timemilli /1000L )));
            seekBar.setProgress((currtm));
            if(bitmap!=null){
                //image.setImageBitmap(bitmap);
                Picasso.with(this)
                        .load(Uri.parse("file://"+current_song.getImagepath()))
                        .error(R.drawable.guitar)
                        .into(image);
                Palette palette=Palette.from(bitmap).generate();
                Palette.Swatch swatch;
                try {
                    swatch = palette.getDarkMutedSwatch();
                }catch (Exception e){
                    swatch = palette.getMutedSwatch();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(swatch.getRgb());
                }
                gradient_back.setBackground(ContextCompat.getDrawable(this,R.drawable.grad));

            }else{
                //image.setImageResource(R.drawable.guitar);
                Picasso.with(this)
                        .load(R.drawable.guitar)
                        .error(R.drawable.mp3)
                        .into(image);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(Color.BLACK);
                }
            }

        }catch (Exception e){

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
    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
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
            case R.id.play_pause :{
                Log.i("mmmm","playpause");
               // Log.i("mmmm",String.valueOf(con.getCurrentPosition())+"---"+String.valueOf(con.getlist().size()));

                if(currentPlaybackstate!=null) {
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
                    con.playsong(0);
                }

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

            Current_time=gettime(Integer.parseInt(String.valueOf(con.getcurrentplaybacktime()/1000L)));
            Log.i("kkkk","current string:"+Current_time);
            current.setText(Current_time);
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
            //Log.i("kkkk","doinbackground11");
            while (canrun) {
                Log.i("kkkk","canrun player doinbackground");
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
        if(isshuffle){
            shuffle.setImageResource(R.drawable.shuffle_selected);
        }else{
            shuffle.setImageResource(R.drawable.shuffle);
        }
        if(animation) {
            shuffle.animate().scaleX(1.25f).scaleY(1.25f).setDuration(0).withEndAction(new Runnable() {
                @Override
                public void run() {
                    shuffle.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            });
        }
    }

}
