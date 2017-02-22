package com.androidplay.one.myplayer.activities;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.SliderImagePagerAdapter;
import com.androidplay.one.myplayer.helper_classes.OnSwipeTouchListener;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.helper_classes.ThemeHelper;
import com.androidplay.one.myplayer.helper_classes.VerticalSeekBar;
import com.androidplay.one.myplayer.songs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.logging.Handler;

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
    int isrepeat=0;
    View back_colour;
    LinearLayout gradient_back;
    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;
    ViewPager sliderviewpager;
    SliderImagePagerAdapter sliderImagePagerAdapter;


    //equaliser
    Button presetbutton;
    ImageView showequaliser;
    CardView equaliserCard;
    SeekBar seek1;
    SeekBar seek2;
    SeekBar seek3;
    SeekBar seek4;
    SeekBar seek5;
    SeekBar bassboost;
    TextView seek1text;
    TextView seek2text;
    TextView seek3text;
    TextView seek4text;
    TextView seek5text;
    short numberoffreqBands=5;
    Equalizer equalizer;
    short upperbandlevel,lowerbandlevel;
    ArrayList<Short> reverbIds=new ArrayList<>();
    AlertDialog.Builder builder;
    boolean equaliser_statehidden=true;
    //BassBoost bassBoost;
    MediaPlayer player;
    ThemeHelper themeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        con=new ApplicationController(this.getApplicationContext(),this);

        themeHelper=new ThemeHelper(this);
        themeHelper.setthemecolours();

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

        //slider viewpager setup
        sliderImagePagerAdapter=new SliderImagePagerAdapter(getSupportFragmentManager());
        sliderviewpager.setAdapter(sliderImagePagerAdapter);
        //sliderviewpager.addOnPageChangeListener(pageChangeListener);

        //-----------kk-k--k
        connectControllerToSession(con.getMediaSessionToken());


        oncreateequaliserfunction();
    }


    public void initialise(){
        toolbar=(Toolbar)findViewById(R.id.player_toolbar);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){e.printStackTrace();}
        Log.i("llll","on create :conisplaying"+con.isPlaying());

        sliderviewpager=(ViewPager)findViewById(R.id.sliderviewpager);
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

        //equaliser
        showequaliser=(ImageView)findViewById(R.id.player_showequaliser);
        equaliserCard=(CardView)findViewById(R.id.equaliser_card);
        presetbutton=(Button)findViewById(R.id.equaliser_preset);
        seek1=(VerticalSeekBar) findViewById(R.id.seekbar1);
        seek2=(VerticalSeekBar) findViewById(R.id.seekbar2);
        seek3=(VerticalSeekBar) findViewById(R.id.seekbar3);
        seek4=(VerticalSeekBar) findViewById(R.id.seekbar4);
        seek5=(VerticalSeekBar) findViewById(R.id.seekbar5);
        bassboost=(SeekBar) findViewById(R.id.bass_boost);
        toolbar=(Toolbar) findViewById(R.id.MyToolbar);
        seek1text=(TextView)findViewById(R.id.seek1text);
        seek2text=(TextView)findViewById(R.id.seek2text);
        seek3text=(TextView)findViewById(R.id.seek3text);
        seek4text=(TextView)findViewById(R.id.seek4text);
        seek5text=(TextView)findViewById(R.id.seek5text);



        // seekbarasync.execute();
        play_pause.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        repeat.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        showequaliser.setOnClickListener(this);

        image.setScaleType(ImageView.ScaleType.CENTER_CROP);


    }
    private void connectControllerToSession(MediaSessionCompat.Token token) {
        try {
            controllerCompat=new MediaControllerCompat(this,con.getMediaSessionToken());
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
    public ViewPager.OnPageChangeListener pageChangeListener=new ViewPager.OnPageChangeListener(){
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            //Toast.makeText(MainActivity.this,"new Page selected",Toast.LENGTH_SHORT).show();
            con.playsong(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        con.activityOnResume();
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


        //equaliser
        equalizer=con.getEqualiser();
        if(bassboost!=null) {
            bassboost.setFocusable(true);
            bassboost.setOnSeekBarChangeListener(this);
            bassboost.setMax(1000);
            bassboost.setProgress(con.getbassboost());
        }
        if(equalizer!=null) {
            presetbutton.setText(equalizer.getPresetName(equalizer.getCurrentPreset()));
        }


    }

    @Override
    protected void onPause() {

        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
        controllerCompat.unregisterCallback(callback);

        if(bassboost!=null) {
            bassboost.setFocusable(false);
            bassboost.setOnSeekBarChangeListener(null);
        }
        super.onPause();
    }


    public void seticon(boolean animation){
        isplaying=con.isPlaying();
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

            //refresh equaliser
            refreshseekbarsasync();
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
            case android.R.id.home:{
                finish();
                return true;
            }
            case R.id.queue:{
                Log.i("lllll","queue called");
                startActivity(new Intent(this,Now_playing.class));
                return true;
            }
            /*
            case R.id.player_equaliser:{
                Log.i("lllll","queue called");
                startActivity(new Intent(this,Equaliser.class));
                return true;
            }*/
            default:return super.onOptionsItemSelected(item);
        }
    }

    public void refreshview(){

        // sliderviewpger refresh
        sliderviewpager.removeOnPageChangeListener(pageChangeListener);

        sliderImagePagerAdapter.setcount(con.getlist().size());
        sliderImagePagerAdapter.notifyDataSetChanged();
        sliderviewpager.setCurrentItem(con.getCurrentPosition(),false);

        sliderviewpager.addOnPageChangeListener(pageChangeListener);

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
            //bitmap= BitmapFactory.decodeFile(current_song.getImagepath());
            setmax=timemilli;
            seekBar.setMax(Integer.parseInt(String.valueOf(timemilli /1000L )));
            seekBar.setProgress((currtm));
            /*
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
*/
        }catch (Exception e){

        }
    }
    public String gettime(int secs){
        if(secs<0){
            return "0:00";
        }
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
            case R.id.equaliser_preset:{
                builder=new AlertDialog.Builder(this);
                /// getplaylist to populate popupmenu
                ArrayList<String> pl=con.getPresetList();
                if(pl==null || pl.size()==0){
                    return ;
                }
                String[] presetlist=new String[pl.size()];
                for(int i=0;i<pl.size();i++){
                    presetlist[i]=pl.get(i);
                }
                //presetlist=(String[]) pl.toArray();
                builder.setTitle("Choose a Preset");
                builder.setItems(presetlist, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        con.setPresetList(i);
                        dialogInterface.dismiss();
                        refreshseekbars();
                    }
                });
                builder.create().show();
                break;
            }case R.id.player_showequaliser :{
                show_hide_equaliser();
                return;
            }
            default: return;

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int seekbarid = seekBar.getId();
        switch (seekbarid) {
            case R.id.seekbar: {
                Log.i("lllll", "progress changed :");
                // Log.i("kkkk","on progress changed:"+String.valueOf(progress)+" -- "+!con.isnull() +fromUser);
                Log.i("mmmm", "onprogresschamged -" + String.valueOf(progress));
                if (!con.isnull() && fromUser) {
                    // con.seekTo(progress);
                    Log.i("kkkk", "on progress changed:" + String.valueOf(progress));
                    con.seekTo(Long.parseLong(String.valueOf(progress * 1000)));
                }
                break;
            }
            case R.id.seekbar1: {
                if (equalizer != null) {
                    short level = (short) (progress - ((upperbandlevel - lowerbandlevel)));
                    if (level > upperbandlevel) {
                        level = upperbandlevel;
                    } else {
                        if (level < lowerbandlevel) {
                            level = lowerbandlevel;
                        }
                    }
                    Log.i("equalis", "progress changed level=" + level);
                    equalizer.setBandLevel((short) 0, level);
                }
                break;
            }
            case R.id.seekbar2: {
                if (equalizer != null) {
                    short level = (short) (progress - ((upperbandlevel - lowerbandlevel)));
                    if (level > upperbandlevel) {
                        level = upperbandlevel;
                    } else {
                        if (level < lowerbandlevel) {
                            level = lowerbandlevel;
                        }
                    }
                    Log.i("equalis", "progress changed level=" + level);
                    equalizer.setBandLevel((short) 0, level);
                }
                break;
            }
            case R.id.seekbar3: {
                if (equalizer != null) {
                    short level = (short) (progress - ((upperbandlevel - lowerbandlevel)));
                    if (level > upperbandlevel) {
                        level = upperbandlevel;
                    } else {
                        if (level < lowerbandlevel) {
                            level = lowerbandlevel;
                        }
                    }
                    Log.i("equalis", "progress changed level=" + level);
                    equalizer.setBandLevel((short) 0, level);
                }
                break;
            }
            case R.id.seekbar4: {
                if (equalizer != null) {
                    short level = (short) (progress - ((upperbandlevel - lowerbandlevel)));
                    if (level > upperbandlevel) {
                        level = upperbandlevel;
                    } else {
                        if (level < lowerbandlevel) {
                            level = lowerbandlevel;
                        }
                    }
                    Log.i("equalis", "progress changed level=" + level);
                    equalizer.setBandLevel((short) 0, level);
                }
                break;
            }
            case R.id.seekbar5: {
                if (equalizer != null) {
                    short level = (short) (progress - ((upperbandlevel - lowerbandlevel)));
                    if (level > upperbandlevel) {
                        level = upperbandlevel;
                    } else {
                        if (level < lowerbandlevel) {
                            level = lowerbandlevel;
                        }
                    }
                    Log.i("equalis", "progress changed level=" + level);
                    equalizer.setBandLevel((short) 0, level);
                }
                break;
            }
            case R.id.bass_boost: {
                Log.i("equalis", "progress changed progress=" + progress);

                //bassBoost.setStrength((short)progress);
                con.setboost((short) progress);
                break;
            }
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




    //equalisermethods
    public void refreshseekbars(){

        equalizer=con.getEqualiser();
        if(equalizer!=null) {
            numberoffreqBands = equalizer.getNumberOfBands();
            upperbandlevel = equalizer.getBandLevelRange()[0];
            upperbandlevel = equalizer.getBandLevelRange()[1];

            String s;
            for (short i = 0; i < numberoffreqBands; i++) {
                Log.i("equalis", "refreshseekbars band level=" + equalizer.getBandLevel(i));
                int aa=equalizer.getCenterFreq(i);
                if(aa>1000000){
                    //convert to khz
                    float f= ((float)aa / 1000000);
                    s=String.format("%.1f \n kHz",f);
                    gettextViewforpos(i).setText(s);

                }else{
                    s = aa / 1000 + "\n Hz";
                    gettextViewforpos(i).setText(s);
                }
                int maxx = 2 * (upperbandlevel - lowerbandlevel);
                getSeekBarforpos(i).setMax(maxx);
                short prog = (short) (lowerbandlevel + ((upperbandlevel - lowerbandlevel)) + equalizer.getBandLevel(i));
                Log.i("equalis", "refreshseekbars max=" + maxx + " progress=" + prog);

                getSeekBarforpos(i).setProgress(prog);
            }
            Log.i("equalis", "refreshseekbars getbassboost=" + con.getbassboost());
            presetbutton.setText(equalizer.getPresetName(equalizer.getCurrentPreset()));
        }else{
            for (short i = 0; i < numberoffreqBands; i++) {
                getSeekBarforpos(i).setProgress(0);
            }
            return;
        }
        bassboost.setProgress(con.getbassboost());

    }
    public TextView gettextViewforpos(short i){
        switch (i){
            case 0:return seek1text;
            case 1:return seek2text;
            case 2:return seek3text;
            case 3:return seek4text;
            case 4:return seek5text;
        }
        return seek1text;
    }
    public SeekBar getSeekBarforpos(short i){
        switch (i){
            case 0:return seek1;
            case 1:return seek2;
            case 2:return seek3;
            case 3:return seek4;
            case 4:return seek5;
        }
        return seek1;
    }
    public ArrayList<String> getReverbList(){
        reverbIds.clear();
        ArrayList<String> list=new ArrayList<>();
        list.add("None");
        reverbIds.add(PresetReverb.PRESET_NONE);
        list.add("Small Room");
        reverbIds.add(PresetReverb.PRESET_SMALLROOM);
        list.add("Medium Room");
        reverbIds.add(PresetReverb.PRESET_MEDIUMROOM);
        list.add("Large Room");
        reverbIds.add(PresetReverb.PRESET_LARGEROOM);
        list.add("Medium Hall");
        reverbIds.add(PresetReverb.PRESET_MEDIUMHALL);
        list.add("Large Hall");
        reverbIds.add(PresetReverb.PRESET_LARGEHALL);
        list.add("Plate");
        reverbIds.add(PresetReverb.PRESET_PLATE);
        return list;
    }
    public void show_hide_equaliser(){
        if(equaliser_statehidden) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int cx = equaliserCard.getWidth();
                int cy = (equaliserCard.getHeight());

                // get the initial radius for the clipping circle
                float finalradius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(equaliserCard, cx, cy, 0, finalradius);

                anim.setDuration(300);
                // start the animation
                equaliserCard.setVisibility(View.VISIBLE);
                anim.start();
            } else {
                equaliserCard.setVisibility(View.VISIBLE);
            }
            showequaliser.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.equaliser));
            equaliser_statehidden=false;
        }else{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int cx = equaliserCard.getWidth();
                int cy = (equaliserCard.getHeight());

                // get the initial radius for the clipping circle
                float finalradius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(equaliserCard, cx, cy, finalradius, 0);

                anim.setDuration(300).addListener(listener);
                // start the animation
                equaliserCard.setVisibility(View.VISIBLE);
                anim.start();
            } else {
                equaliserCard.setVisibility(View.INVISIBLE);
            }
            showequaliser.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.equaliser_white));

            equaliser_statehidden=true;
        }
    }
    public Animator.AnimatorListener listener= new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
        }
        @Override
        public void onAnimationEnd(Animator animator) {
            equaliserCard.setVisibility(View.INVISIBLE);
        }
        @Override
        public void onAnimationCancel(Animator animator) {
        }
        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    };
    public void setEqualiser(){
        numberoffreqBands=equalizer.getNumberOfBands();
        upperbandlevel=equalizer.getBandLevelRange()[0];
        upperbandlevel=equalizer.getBandLevelRange()[1];

        Log.i("equalis","setEqualiser");
        Log.i("equalis","setEqualiser upperbandlevel="+upperbandlevel+"lowerbandlevel="+lowerbandlevel);
        refreshseekbars();

        seek1.setOnSeekBarChangeListener(this);
        seek2.setOnSeekBarChangeListener(this);
        seek3.setOnSeekBarChangeListener(this);
        seek4.setOnSeekBarChangeListener(this);
        seek5.setOnSeekBarChangeListener(this);

    }
    public void oncreateequaliserfunction(){
        equalizer=con.getEqualiser();
        presetbutton.setOnClickListener(this);
        if(equalizer!=null){
            setEqualiser();
        }
        player=con.getmediaplayer();
        bassboost.setMax(1000);
    }
    public void refreshseekbarsasync(){
        Log.i("equalis","refreshseekbars async");

        android.os.Handler handler=new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("equalis","refreshseekbars async runnable");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playerr.this.refreshseekbars();
                    }
                });

            }
        },500);
    }


}
