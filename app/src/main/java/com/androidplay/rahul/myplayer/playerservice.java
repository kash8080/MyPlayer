    package com.androidplay.rahul.myplayer;

    import android.Manifest;
    import android.app.Activity;
    import android.app.ActivityManager;
    import android.app.Notification;
    import android.app.NotificationManager;

    import android.app.PendingIntent;
    import android.app.Service;
    import android.content.BroadcastReceiver;
    import android.content.ContentUris;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.SharedPreferences;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.media.AudioManager;
    import android.media.audiofx.Equalizer;
    import android.net.Uri;
    import android.os.AsyncTask;
    import android.os.Binder;
    import android.os.IBinder;
    import android.preference.PreferenceManager;
    import android.support.v4.app.TaskStackBuilder;
    import android.support.v4.content.ContextCompat;
    import android.support.v4.media.session.MediaButtonReceiver;
    import android.support.v7.app.NotificationCompat;
    import android.support.v4.media.MediaDescriptionCompat;
    import android.support.v4.media.MediaMetadataCompat;
    import android.support.v4.media.session.MediaControllerCompat;
    import android.support.v4.media.session.MediaSessionCompat;
    import android.support.v4.media.session.PlaybackStateCompat;
    import android.util.Log;
    import android.view.KeyEvent;

    import com.google.android.exoplayer.ExoPlaybackException;
    import com.google.android.exoplayer.ExoPlayer;
    import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
    import com.google.android.exoplayer.MediaCodecSelector;
    import com.google.android.exoplayer.extractor.ExtractorSampleSource;
    import com.google.android.exoplayer.upstream.Allocator;
    import com.google.android.exoplayer.upstream.DataSource;
    import com.google.android.exoplayer.upstream.DefaultAllocator;
    import com.google.android.exoplayer.upstream.DefaultUriDataSource;
    import com.google.android.exoplayer.util.Util;
    import com.google.gson.Gson;
    import com.google.gson.reflect.TypeToken;

    import java.io.BufferedReader;
    import java.io.FileNotFoundException;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.lang.reflect.Type;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Random;

    import static com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifest.StreamElement.TYPE_AUDIO;

    public class playerservice extends Service implements AudioManager.OnAudioFocusChangeListener {

        private final IBinder musicBind = new MusicBinder();

        public static ExoPlayer player;
        public static Long current_id = null;
         MediaCodecAudioTrackRenderer audioRenderer;
        static ExtractorSampleSource sampleSource;
        static DataSource dataSource;
        static Context myContext;
        public static boolean shuffle = false;
        private static Bitmap bitmap;
        public static int current_pos = -1;
        public static int repeat = 0;
        static boolean initialsetup=true;
        public static ArrayList<songs> mylist;
        public static Context activitycontext;
        static String currentsongno="--";
        //mediasessioncompat
        static MediaSessionCompat msession ;
        AudioManager audioManager;
        public static boolean hasfocus=false;
        NotificationCompat.Builder mBuilder;
        NotificationManager mNotificationManager;
        private static int playpausenoti=R.drawable.pause_white;
        Notification notification;
        private int ONGOING_NOTIFICATION_ID=400;
        private Long savedSeekto=100L;
        boolean losstransient=false;
        static SharedPreferences sp;
        static boolean restoredsong=false;

        Equalizer equalizer;
        ArrayList<String> presetlist;

        public static Boolean previousstatesaved=false;
        public playerservice() {
            Log.i("qwsd","service constructor");

        }
        public void setEverything(){
            if(msession==null) {
                //ComponentName eventReceiver = new ComponentName(getPackageName(), RemoteMediaReceiver.class.getName());
                msession = new MediaSessionCompat(this,playerservice.class.getSimpleName());//-----------
                msession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
                // msession.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
                msession.setCallback(new mediacallback());
            }
            if(player==null) {
                setplayer();
            }
            if(mylist==null || mylist.size()==0) {
                mylist = new ArrayList<>();
                readlistfromMemory();
            }
            //sp = myContext.getSharedPreferences("MyPlayer_controls", Activity.MODE_PRIVATE);
            sp = this.getSharedPreferences("MyPlayer_controls", Activity.MODE_PRIVATE);
            repeat=sp.getInt("isRepeat",1);
            shuffle=sp.getBoolean("isShuffle",false);

        }
        @Override
        public IBinder onBind(Intent intent) {
            Log.i("qwsd","service bind");

            activitycontext=ApplicationController.activitycontext;
            return musicBind;

        }
        @Override
        public boolean onUnbind(Intent intent) {
            Log.i("qwsd","service unbind");

            //player.stop();
            //player.release();
            super.onUnbind(intent);
            //onDestroy();
            return false;
        }

        public class MusicBinder extends Binder {
            playerservice getService() {
                Log.i("qwsd","get service binder");

                return playerservice.this;
            }
        }

        @Override
        public void onCreate() {
            Log.i("qwsd","service oncreste");
            super.onCreate();
            myContext=this;
            presetlist=new ArrayList<>();
            if(msession==null) {
                //ComponentName eventReceiver = new ComponentName(getPackageName(), RemoteMediaReceiver.class.getName());
                msession = new MediaSessionCompat(this,playerservice.class.getSimpleName());//-----------
                msession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
                // msession.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
                msession.setCallback(new mediacallback());
            }
            setplayer();
            mylist=new ArrayList<>();
            readlistfromMemory();
            sp = myContext.getSharedPreferences("MyPlayer_controls", Activity.MODE_PRIVATE);
            sp = this.getSharedPreferences("MyPlayer_controls", Activity.MODE_PRIVATE);
            repeat=sp.getInt("isRepeat",1);
            shuffle=sp.getBoolean("isShuffle",false);

        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.i("qwsd","on start command");
            int i= super.onStartCommand(intent, flags, startId);
            MediaButtonReceiver.handleIntent(msession,intent);
            return i;
        }

        public void setcontext(Context contextt) {
            Log.i("qwsd","service setcontext");
            Log.i("qwsd","setcontext foreground="+isServiceRunningInForeground(this,playerservice.class));

            setEverything();
            this.activitycontext=contextt;
            if(initialsetup){
                restoreCurrentSongValue();
                initialsetup=false;
            }
            if(hasfocus) {
                msession.setActive(true);
            }
            Log.i("qwsd","setcontext foreground="+isServiceRunningInForeground(this,playerservice.class));
        }
        @Override
        public void onDestroy() {
            Log.i("qwsd","service on destroy");
            savelistToMemory();
            savecurrentsonginfo();

            try{unregisterReceiver(mbreceiver);
                player.stop();
                player.release();
                audioManager.abandonAudioFocus(this);
                msession.release();
            }catch (Exception e){}
            try{
                stopForeground(true);
                mNotificationManager.cancel(400);
            }catch (Exception e){}

            super.onDestroy();
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            //Log.i("qwsd","ontaskremoved foreground="+isServiceRunningInForeground(this,playerservice.class));

            savelistToMemory();
            savecurrentsonginfo();

            if(!isServiceRunningInForeground(this,playerservice.class)) {
                try {
                    unregisterReceiver(mbreceiver);
                    player.stop();
                    player.release();
                    audioManager.abandonAudioFocus(this);
                    msession.release();
                } catch (Exception e) {
                }
                try {
                    stopForeground(true);
                    mNotificationManager.cancel(400);
                } catch (Exception e) {
                }
            }else{
                pause();
            }

            //Log.i("qwsd","ontaskremoved afterstopforeground foreground="+isServiceRunningInForeground(this,playerservice.class));
            super.onTaskRemoved(rootIntent);
        }

        public  void setplayer() {
        player = ExoPlayer.Factory.newInstance(1);

        player.addListener(new ExoPlayer.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i("kkkk", "state changed");


                if (playbackState == ExoPlayer.STATE_ENDED) {
                    //player back ended
                    playnext();
                    //ApplicationController.app_playnextsong();
                }
            }

            @Override
            public void onPlayWhenReadyCommitted() {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }
        });
    }

        public void setmetadata(){
            try {
                MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, getsong().getName());
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,getsong().getArtist());
                if (getsong().getImagepath() != null) {
                    //metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,getsong().getBitmap());
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, getsong().getImagepath());
                }

                msession.setMetadata(metadataBuilder.build());
                msession.setActive(true);
                //buildnotification();
            }catch (Exception e){e.printStackTrace();}
            //buildnotification();
        }
        public void setstate(){

            try {
                PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
                stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP );
                stateBuilder.setState(!isPlaying() ? PlaybackStateCompat.STATE_PAUSED : PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);

                msession.setPlaybackState(stateBuilder.build());
            }catch (Exception e){e.printStackTrace();}
        }
        public void buildnotification(){
            Log.i("hjhj","build noti");

            Log.i("huhu", " start");
            Log.i("huhu", String.valueOf(msession==null));
            MediaControllerCompat controller;
            MediaMetadataCompat metadata;
            MediaDescriptionCompat descriptionCompat;

            try{
                 controller =msession.getController();
                 metadata= controller.getMetadata();
                 descriptionCompat =metadata.getDescription();
            }catch (Exception e){
                return;
            }

             mBuilder = new NotificationCompat.Builder(activitycontext);
            Log.i("huhu", " a");

             bitmap=null;
            try{
                bitmap= BitmapFactory.decodeFile(getsong().getImagepath());
            }catch (Exception e){}

            Intent resultIntent = new Intent(this, playerr.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(playerr.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder
                    .setSmallIcon(R.drawable.headset_white)
                    .setContentTitle(descriptionCompat.getTitle())
                    .setContentText(String.valueOf(descriptionCompat.getSubtitle()))
                    .setLargeIcon(descriptionCompat.getIconBitmap())
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setShowWhen(false)
                    .setWhen(0L)
                    .setContentIntent(controller.getSessionActivity())
                    .setDeleteIntent(getactionintent(KeyEvent.KEYCODE_MEDIA_STOP))
                    .addAction(new NotificationCompat.Action(R.drawable.previous,"previous",getactionintent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)))
                    .addAction(new NotificationCompat.Action(playpausenoti,"play",getactionintent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)))
                    .addAction(new NotificationCompat.Action(R.drawable.next,"next",getactionintent(KeyEvent.KEYCODE_MEDIA_NEXT)))


            .setContentIntent(resultPendingIntent)

            .setStyle(new NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(1)
                                    .setShowCancelButton(true)
                                    .setCancelButtonIntent(getactionintent(KeyEvent.KEYCODE_MEDIA_STOP))
                                    .setMediaSession(msession.getSessionToken()));
            if(isPlaying()){
                mBuilder.setOngoing(true);
            }else{
                mBuilder.setOngoing(false);
            }

            Log.i("hjhj",String.valueOf(getactionintent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)));

            if(bitmap!=null){
                mBuilder.setLargeIcon(bitmap);
            }else{
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(activitycontext.getApplicationContext().getResources(),R.drawable.headset_white));
            }


             mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 400 allows you to update the notification later on.

            notification=mBuilder.build();
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mBuilder.build());
            Log.i("huhu", " done");


        }

        boolean canplay=true;
        //////////exoplayer controls
        public class setcanplay extends AsyncTask<Integer,Void,Void>{
            int iss;
            int pos;
            @Override
            protected Void doInBackground(Integer... voids) {
                iss=voids[0];
                if(iss==1) {
                    pos = voids[1];
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                canplay=true;
                if(iss==1){
                    playsong(pos);
                }
            }
        }
        boolean can_play=true;
        boolean should_resume=false;
        public void playsong(int pos) {
            Log.i("playy", "playsong at pos="+String.valueOf(pos));

            Log.i("equall", "playsong start pos="+pos);
            currentsongno=String.valueOf(pos);
            if(mylist==null){
                mylist=new ArrayList<>();
                return;
            }
            registerReceiver(mbreceiver,intentfilter);
            if (mylist.size() > 0 && current_pos == -1) {
                current_pos = 0;
            }

            if(hasfocus) {
                Log.i("playy", "playsong has focus");
                try {
                    current_pos = pos;
                    Log.i("bnbn", "playnext" + mylist.size() + " " + current_pos);
                    if (player == null) {
                        setplayer();
                    }

                    Log.i("exoo", "duration of song:" + mylist.get(pos).getName() + String.valueOf(getDuration()));
                    current_id = mylist.get(pos).getId();
                    Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, current_id);

                    Log.i("urisong", trackUri.toString());

                    if (player != null && player.getPlayWhenReady()) {
                        player.stop();
                    }

                    final int BUFFER_SEGMENT_SIZE = 64 * 1024;
                    final int BUFFER_SEGMENT_COUNT = 256;

                    String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
                    Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);

                    dataSource = new DefaultUriDataSource(this, null, userAgent);
                    sampleSource = new ExtractorSampleSource(trackUri, dataSource, allocator, BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT);
                    //audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);
                    audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,MediaCodecSelector.DEFAULT);
                    /*{
                        @Override
                        public void onAudioSessionId(int audioSessionId) {
                            Log.i("equall","onaudiosessionid="+audioSessionId);
                               /* releaseEqualizer();
                                equalizer = new Equalizer(10, audioSessionId);
                                // Configure equalizer here.

                                //short[] range = equalizer.getBandLevelRange();

                                //equalizer.usePreset((short) 0);
                                equalizer.setEnabled(true);

                        }

                        @Override
                        public void onDisabled() {
                            Log.i("equall","ondisabled=");

                            //releaseEqualizer();
                        }

                        private void releaseEqualizer() {
                            Log.i("equall","releaseEqualizer start");

                            if (equalizer != null) {
                                Log.i("equall","releaseEqualizer releasing equaliser!=null");
                                equalizer.release();
                                equalizer = null;
                            }
                        }


                    };
            */
                    Log.i("equall","preparing audio renderer");
                    player.prepare(audioRenderer);
                    // 3. Start playback.
                   // player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f);
                    Log.i("equall","setPlayWhenReady");

                    player.setPlayWhenReady(true);

                    Log.i("exoo", "--");
                    player.seekTo(100L);

                    setstate();
                    setmetadata();
                    buildnotification();
                    startForeground(ONGOING_NOTIFICATION_ID,mBuilder.build());
                    updateplaypause();
                     //async as=new async();
                        //as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("bnbn", e.toString());


                }

            }else{
                askaudio(pos);
                should_resume=true;
            }
            Log.i("bnbn", "isplaying:" + isnull() + isPlaying());
            Log.i("equall", "playsong ended");
        }
        public class async extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pause();
            }
        }
        public void pause() {
            Log.i("playy","pause");
            //stopfg(false)
            if (player != null) {
                try{
                    unregisterReceiver(mbreceiver);
                    player.setPlayWhenReady(false);
                    setstate();
                    updateplaypause();
                    stopForeground(false);
                }catch (Exception e){e.printStackTrace();}

            }
        }
        public  void resume() {
            Log.i("fdfd","resume " + String.valueOf(getDuration()));

            if(!hasfocus){
                askaudio(-1);
                return;
            }

            if(getDuration()==-1){
                Log.i("fdfd","resume -1");
                if(current_pos==-1){
                    current_pos=0;
                }
                playsong(current_pos);

            }else {
                Log.i("fdfd","resume ! -1");

                if (player != null && !player.getPlayWhenReady()) {
                    try {
                        registerReceiver(mbreceiver, intentfilter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    player.setPlayWhenReady(true);
                    setstate();
                    startForeground(ONGOING_NOTIFICATION_ID,mBuilder.build());
                    updateplaypause();
                } else {
                    playsong(0);
                }
            }
        }
        public static boolean isnull() {
            if (player == null) {
                return true;
            } else return false;
        }
        public static int isRepeat() {
            return repeat;
        }
        public static void setRepeat(int repeat) {
            playerservice.repeat = repeat;
            sp = myContext.getSharedPreferences("MyPlayer_controls", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("isRepeat",repeat);
            editor.commit();
        }
        public static boolean isShuffle() {
            return shuffle;
        }
        public void setShuffle(boolean shuffle) {
            playerservice.shuffle = shuffle;
            sp = this.getSharedPreferences("MyPlayer_controls", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isShuffle",shuffle);
            editor.commit();
        }
        public static void setMylist(ArrayList<songs> list) {
            Log.i("qwsd","setmylist service");
            mylist = list;
        }
        public static int getCurrentPosition() {
            return current_pos;
        }
        public static void setCurrent_pos(int current_pos1) {
            current_pos = current_pos1;
        }
        public static songs getsong() {
            if (mylist != null && current_pos != -1 && mylist.size()>current_pos) {
                return mylist.get(current_pos);
            } else if (mylist != null && current_pos == -1 && mylist.size() > 0) {
                current_pos = 0;
                mylist.get(current_pos);
            }
            return null;
        }
        public static void seekTo(Long positionms) {
            if (player != null) {
                //  Log.i("kkkk","player.seekto:"+String.valueOf(positionms));
                player.seekTo(positionms);

            }

        }
        public static Long getDuration() {
            if (player != null) {
                return player.getDuration();
            } else return 0L;
        }

        public static boolean isPlaying() {
            Log.i("bb", "isplaying");
            if (player != null) {
                return player.getPlayWhenReady();
            }
            return false;
        }
        public static void setnull() {
            if (player != null) {
                player.release();

            }
        }
        public  void playnext() {
            if (repeat==2){

                playsong(current_pos);
            }else if(repeat==1){
                if (shuffle) {
                    playsong(getrandno());

                } else {
                    if (current_pos == mylist.size() - 1) {
                        current_pos = 0;
                    } else {
                        current_pos++;
                    }
                    playsong(current_pos);
                }
            }else{
                playsong(current_pos);
                async as=new async();
                as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
        public  void playprev() {
            if (repeat==2) {

                playsong(current_pos);
            } else if(repeat==1){
                if (shuffle) {
                    playsong(getrandno());

                } else {
                    if (current_pos == 0) {
                        current_pos = mylist.size() - 1;
                    } else {
                        current_pos--;
                    }

                    playsong(current_pos);
                }
            }else{
                playsong(current_pos);
                async as=new async();
                as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        public static Long getcurrentplaybacktime(){
            if(current_pos==-1 || player==null || mylist.size()==0 || mylist==null){
                return 0L;
            }
            return player.getCurrentPosition();
        }
        public static ArrayList<songs> getlist() {
            if(mylist==null){
                mylist=new ArrayList<>();
            }
            if(mylist.size()>0)
                return mylist;
            return null;
        }
        public void  remove_song(int i) {
           if(mylist!=null && mylist.size()>0){
               Log.i("wewe","start    current pos -"+String.valueOf(current_pos));

               Log.i("wewe","removing song -"+String.valueOf(i));
               try{
                   mylist.remove(i);
                   if(i==current_pos){
                       playnext();
                       pause();
                   }else if(i<current_pos){
                       current_pos--;
                    }
                   Log.i("wewe","end    current pos -"+String.valueOf(current_pos));

               }catch (Exception e){e.printStackTrace();}
           }


        }
        public static void notifydatachange(int id,int from ,int to){

            songs saved=mylist.get(from);
            mylist.remove(from);
            mylist.add(to,saved);

        }
        public static int getrandno(){
            Random rand =new Random();
            if(mylist.size()<=1){
                return current_pos;
            }else {
                int randomno = rand.nextInt(mylist.size() - 1);
                return randomno;
            }
        }
        public static void addsongstolist(ArrayList<songs> songlist){
            for(songs s:songlist){
                mylist.add(s);
            }
            //mylist.addAll(songlist);
        }
        public void addSongToNextPos(songs s){
            if(mylist!=null ){
                int i=current_pos+1;
                if(i<mylist.size()){
                    mylist.add(i,s);
                }else{
                    mylist.add(s);
                }
            }else{
                mylist=new ArrayList<>();
                mylist.add(s);
                current_pos=0;
            }
        }

        public static void savelistToMemory(){
            Log.e("savestate", "savelistToMemory");

            if(mylist==null ){
                return;
            }
            Long start=System.currentTimeMillis();

            Gson gson=new Gson();
            Type listType = new TypeToken<List<songs>>() {}.getType();

            String liststring=gson.toJson(mylist,listType);
            String filename = "queueList.txt";
            FileOutputStream outputStream;

            try {
                 outputStream = myContext.openFileOutput(filename, Context.MODE_PRIVATE);
                 outputStream.write(liststring.getBytes());
                 outputStream.flush();
                 outputStream.close();
            } catch (Exception e) {
                 e.printStackTrace();
            }
            Long end=System.currentTimeMillis()-start;
            savecurrentsonginfo();
            Log.i("qwsd","savelist time taken:"+String.valueOf(end));

        }
        public static void readlistfromMemory() {
            Log.e("savestate", "readlistfromMemory");

            Long start=System.currentTimeMillis();
            String ret = "";
            try {
                InputStream inputStream = myContext.openFileInput("queueList.txt");

                if ( inputStream != null ) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ( (receiveString = bufferedReader.readLine()) != null ) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    ret = stringBuilder.toString();
                }
            }
            catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }
            Gson gson=new Gson();
            Type listType = new TypeToken<ArrayList<songs>>(){}.getType();
            ArrayList<songs> target2 = gson.fromJson(ret, listType);
            if(target2!=null&&target2.size()>0){
                mylist=target2;
            }

            //Log.i("qwsd","readList first Song="+target2.get(0).getName());
        }
        public static void savecurrentsonginfo(){
            //position,seek value
            Log.e("savestate", "savecurrentsonginfo");

            SharedPreferences sp = myContext.getSharedPreferences("MyPlayer_CurrentsongInfo", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("service_currentSongPositionInList",current_pos);
            editor.putLong("service_currentSongSeekValue",getcurrentplaybacktime());
            editor.commit();
        }
        public static void restoreCurrentSongValue(){
            Log.e("playy", "restorecurrentsonginfo");

            if(ApplicationController.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)){

            }else {
                if(hasfocus) {
                    Log.e("playy", "restored currentsonginfo");
                    //position,seek value
                    previousstatesaved = true;
                    SharedPreferences sp = myContext.getSharedPreferences("MyPlayer_CurrentsongInfo", Activity.MODE_PRIVATE);
                    int pos = sp.getInt("service_currentSongPositionInList", 0);
                    current_pos = pos;
                    final Long seek = sp.getLong("service_currentSongSeekValue", 100);
                    ((playerservice) myContext).playsong(pos);
                    seekTo(seek);
                    ((playerservice) myContext).pause();
                    try {
                        ((playerservice) myContext).mNotificationManager.cancel(400);
                    } catch (Exception e) {
                    }
                    restoredsong = true;
                    //play song and pause it back to restore it
                }else{
                    ((playerservice)myContext).askaudio(-2);
                }
            }
        }

            // for pausing music after removing headphones .. we will register/unregister this with play/pause
        private  BroadcastReceiver mbreceiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(isPlaying()){
                    pause();
                    playpausenoti=R.drawable.play_white;
                    mBuilder.setOngoing(false);
                    mBuilder.mActions.get(1).icon=playpausenoti;
                    mNotificationManager.notify(ONGOING_NOTIFICATION_ID,mBuilder.build());
                    //ApplicationController.app_refresh();
                }
               // ApplicationController.app_refresh();
            }
        };
         IntentFilter intentfilter =new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);


        private  class mediacallback extends MediaSessionCompat.Callback{
            @Override
            public void onSkipToQueueItem(long id) {
                super.onSkipToQueueItem(id);
            }

            @Override
            public void onPlay() {
                super.onPlay();
                Log.i("playy","on play");
                resume();
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                //Log.i("hjhj","other");
                return super.onMediaButtonEvent(mediaButtonEvent);
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.i("playy","on pause");
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playnext();
                buildnotification();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playprev();
                buildnotification();
            }

            @Override
            public void onStop() {
                pause();
                Log.i("playy","on stop");
                msession.setActive(false);
                try {
                    stopForeground(true);
                    mNotificationManager.cancel(400);
                }catch (Exception e){}
                //ApplicationController.app_refresh();
                audioManager.abandonAudioFocus(playerservice.this);
                super.onStop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        }


        public void updateplaypause(){
            if(isPlaying()){
                playpausenoti=R.drawable.pause_white;
                mBuilder.setOngoing(true);
                try {
                    mBuilder.mActions.get(1).icon = playpausenoti;
                    mNotificationManager.notify(400, mBuilder.build());
                }catch (Exception e){e.printStackTrace();}
            }else{
                playpausenoti=R.drawable.play_white;
                mBuilder.setOngoing(false);
                try {
                    mBuilder.mActions.get(1).icon = playpausenoti;
                    mNotificationManager.notify(400, mBuilder.build());
                }catch (Exception e){e.printStackTrace();}
            }
            Log.i("playy","updateplaypause");

           // ApplicationController.app_refresh();
        }

        public void askaudio(int poss) {
            Log.i("playy","askaudio start");
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            if(result==AudioManager.AUDIOFOCUS_GAIN){
                Log.i("playy"," gotaudio focus");
                hasfocus=true;
                /*if(player==null){setplayer();}

                try{
                    msession.setActive(true);
                    restoreCurrentSongValue();
                }
                catch (Exception e){e.printStackTrace();}
                Log.i("playy","askaudio end");
*/

                if(player==null){setplayer();}
                msession.setActive(true);
                player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f);

                if(poss==-1){
                    Log.i("playy","poss=-1");
                    resume();
                }else if(poss==-2){
                    Log.i("playy","poss=-2");
                    restoreCurrentSongValue();
                }else{
                    Log.i("playy","poss=playsong");
                    playsong(poss);
                }
            }
            Log.i("playy","askaudio end");
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.i("playy","AUDIOFOCUS_GAIN");
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    // Lost focus for an unbounded amount of time: stop playback and release media player
                    hasfocus=false;
                    pause();
                    savecurrentsonginfo();
                    savelistToMemory();
                    //player.release();
                    updateplaypause();
                    msession.setActive(false);
                    restoredsong=false;

                    //audioManager.abandonAudioFocus(this);
                    Log.i("playy","AUDIOFOCUS_LOSS");

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // Lost focus for a short time, but we have to stop
                    // playback. We don't release the media player because playback
                    // is likely to resume
                    Log.i("playy","AUDIOFOCUS_LOSS_TRANSIENT");
                    hasfocus=false;
                    pause();
                    savecurrentsonginfo();
                    savelistToMemory();
                    restoredsong=false;
                    updateplaypause();
                    losstransient=true;
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    //  if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                    player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, .4f);

                    Log.i("playy","AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
            }

        }

        public PendingIntent getactionintent(int mediakeyevent){
            Intent intent =new Intent(Intent.ACTION_MEDIA_BUTTON);
           // Log.i("hjhj",activitycontext.getPackageName());
            intent.setPackage(activitycontext.getPackageName());
            intent.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN,mediakeyevent));

            return PendingIntent.getBroadcast(this,mediakeyevent,intent,0);
        }

        public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }

                }
            }
            return false;
        }


        public ArrayList<String> getPresetList(){
            if(equalizer!=null) {
                presetlist = new ArrayList<>();
                for (short i = 0; i < equalizer.getNumberOfPresets(); i++) {
                    presetlist.add(equalizer.getPresetName(i));
                }
                return presetlist;
            }else return new ArrayList<>();
        }
        public void setPresetList(int i){
            Log.i("equall","setPresetList");
           if(equalizer!=null) {
               Log.i("equall","setting preset");
               equalizer.usePreset((short) i);
           }
        }

    }
