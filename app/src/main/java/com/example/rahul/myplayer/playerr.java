package com.example.rahul.myplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.ExoPlayer;

public class playerr extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,
                                    ApplicationController.informactivity{

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
    public boolean isrepeat,isshuffle;
    String Current_time;
    public int viewupdater=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playerr);
        Log.i("kkkk","oncreate---------------");

        con=new ApplicationController(this.getApplicationContext(),this);
        toolbar=(Toolbar)findViewById(R.id.player_toolbar);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){}
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

        setrepeatbutton();
        setshufflebutton();


    }

    @Override
    protected void onResume() {


        if(con.getCurrentPosition()==-1){
            con.setCurrent_pos(0);

        }

        Log.i("kkkk","onresume---------------");
        Log.i("mmmm",String.valueOf(con.getCurrentPosition())+"---"+String.valueOf(con.getlist().size()));

        seekbarasync =new updateseekbar1();
        seekbarasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //seekbarasync.execute();

        super.onResume();
        Log.i("llll","on resume :conisplaying"+con.isPlaying());
        isplaying=con.isPlaying();
        refreshview();
        seticon();


    }

    @Override
    protected void onPause() {
        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
        super.onPause();
    }

    public void seticon(){
        if(isplaying){
            Log.i("llll","isplaying");
            play_pause.setImageResource(R.drawable.pause_white);

        }else play_pause.setImageResource(R.drawable.play_white);

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
            isplaying=con.isPlaying();
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

            if(bitmap!=null){image.setImageBitmap(bitmap);}
            else {image.setImageResource(R.drawable.mp3full);}
            //Log.i("mmmm","setdata: getDuration setmax"+(String.valueOf(Integer.parseInt(String.valueOf(timemilli/1000L)))));
            setmax=timemilli;
            seekBar.setMax(Integer.parseInt(String.valueOf(timemilli /1000L )));
            //seekBar.setMax(con.getDuration());
            //Log.i("lllll","----"+String.valueOf(timemilli));
            seticon();
            seekBar.setProgress((currtm));

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
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
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
    public void playnextsong() {
        refreshview();
        seekBar.setProgress(0);
    }

    @Override
    public void refresh() {
        refreshview();
    }

    @Override
    public void updateprofileimage() {

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

}
