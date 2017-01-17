    package com.androidplay.rahul.myplayer;

    import android.app.NotificationManager;

    import android.app.PendingIntent;
    import android.app.Service;
    import android.content.BroadcastReceiver;
    import android.content.ContentUris;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.media.AudioManager;
    import android.net.Uri;
    import android.os.Binder;
    import android.os.IBinder;
    import android.support.v4.app.TaskStackBuilder;
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

    import java.util.ArrayList;
    import java.util.Random;

    public class playerservice extends Service implements AudioManager.OnAudioFocusChangeListener {

    private final IBinder musicBind = new MusicBinder();

    public static ExoPlayer player;
    public static Long current_id = null;
    static MediaCodecAudioTrackRenderer audioRenderer;
    static ExtractorSampleSource sampleSource;
    static DataSource dataSource;

    public static boolean shuffle = false;
    private static Bitmap bitmap;
    public static int current_pos = -1;
    public static boolean repeat = false;

    public static ArrayList<songs> mylist;
    public static Context activitycontext;
    static int currentsongno=0;
    //mediasessioncompat
    static MediaSessionCompat msession ;
    AudioManager audioManager;
    public boolean hasfocus=false;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    private static int playpausenoti=R.drawable.pause_white;

    public playerservice() {
        Log.i("qwsd","service constructor");

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

        player.stop();
        player.release();

        onDestroy();
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
        askaudio();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("qwsd","on start command");

        MediaButtonReceiver.handleIntent(msession,intent);
        return super.onStartCommand(intent, flags, startId);
    }

    public void setcontext(Context contextt) {
        Log.i("qwsd","set context");

        this.activitycontext=contextt;

        //initialising here to make sure context is not null
        if(msession==null) {

            //ComponentName eventReceiver = new ComponentName(getPackageName(), RemoteMediaReceiver.class.getName());
            msession = new MediaSessionCompat(this, "mediasession");
            msession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            // msession.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
            msession.setCallback(new mediacallback());
        }
        if(hasfocus) {
            msession.setActive(true);
        }
        //buildnotification();
    }

    @Override
    public void onDestroy() {
        Log.i("qwsd","service on destroy");

        try{unregisterReceiver(mbreceiver);
            player.stop();
            player.release();
            audioManager.abandonAudioFocus(this);
            msession.release();

        }catch (Exception e){}
        try{mNotificationManager.cancel(400);
        }catch (Exception e){}



        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("qwsd","ontaskremoved");
       // super.onTaskRemoved(rootIntent);
/*
        super.onTaskRemoved(rootIntent);
        onDestroy();

        stopService(ApplicationController.playIntent);
        */
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
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,getsong().getBitmap());
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
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP |
                    PlaybackStateCompat.ACTION_FAST_FORWARD);
            stateBuilder.setState(!isPlaying() ? PlaybackStateCompat.STATE_PAUSED : PlaybackStateCompat.STATE_PLAYING,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
            msession.setPlaybackState(stateBuilder.build());
        }catch (Exception e){e.printStackTrace();}
    }
    public void buildnotification(){
        Log.i("hjhj","build noti");

        Log.i("huhu", " start");
        Log.i("huhu", String.valueOf(msession==null));

        MediaControllerCompat controller =msession.getController();
        MediaMetadataCompat metadata= controller.getMetadata();
        MediaDescriptionCompat descriptionCompat =metadata.getDescription();

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

        Log.i("hjhj",String.valueOf(getactionintent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)));

        if(bitmap!=null){
            mBuilder.setLargeIcon(Bitmap.createScaledBitmap(bitmap,150,150,false));
        }else{
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(activitycontext.getApplicationContext().getResources(),R.drawable.headset_white));
        }


         mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // 400 allows you to update the notification later on.


        mNotificationManager.notify(400, mBuilder.build());
        Log.i("huhu", " done");


    }

    //////////exoplayer controls
    public static boolean isnull() {
        if (player == null) {
            return true;
        } else return false;
    }
    public static boolean isRepeat() {
        return repeat;
    }
    public static void setRepeat(boolean repeat) {
        playerservice.repeat = repeat;
    }
    public static boolean isShuffle() {
        return shuffle;
    }
    public void setShuffle(boolean shuffle) {
        playerservice.shuffle = shuffle;
    }
    public static void setMylist(ArrayList<songs> list) {

        mylist = list;
    }
    public static int getCurrentPosition() {
        return current_pos;
    }
    public static void setCurrent_pos(int current_pos1) {
        current_pos = current_pos1;
    }
    public  void pause() {
    if (player != null) {
        try{
            unregisterReceiver(mbreceiver);
            player.setPlayWhenReady(false);
            setstate();
            updateplaypause();
        }catch (Exception e){e.printStackTrace();}

    }
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
    public  void resume() {
            if(!hasfocus){
                askaudio();
            }
    Log.i("fdfd","resume " + String.valueOf(getDuration()));
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
                updateplaypause();
            } else {
                playsong(0);
            }
        }
    }
    public void seekTo(Long positionms) {
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
    public void playsong(int pos) {
        Log.i("bnbnn", "playsong at pos="+String.valueOf(pos));

        registerReceiver(mbreceiver,intentfilter);
        if (mylist.size() > 0 && current_pos == -1) {
            current_pos = 0;
        }

        if(hasfocus) {


            Log.i("bnbnn", "playsong has focus");
            try {

                current_pos = pos;

                Log.i("bnbn", "playnext" + mylist.size() + " " + current_pos);

                if (player == null) {
                    setplayer();
                }

                Log.i("exoo", "duration of song:" + mylist.get(pos).getName() + String.valueOf(getDuration()));
                current_id = mylist.get(pos).getId();
                Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, current_id);


                if (player != null && player.getPlayWhenReady()) {
                    player.stop();
                }

                final int BUFFER_SEGMENT_SIZE = 64 * 1024;
                final int BUFFER_SEGMENT_COUNT = 256;

                String userAgent = Util.getUserAgent(activitycontext, "ExoPlayerDemo");
                Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);

                dataSource = new DefaultUriDataSource(activitycontext, null, userAgent);

                sampleSource = new ExtractorSampleSource(trackUri, dataSource, allocator, BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT);

                audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);


                player.prepare(audioRenderer);
                Log.i("exoo", "--");

                // 3. Start playback.
                player.setPlayWhenReady(true);
                Log.i("exoo", "--");
                player.seekTo(100L);
    //
                //ApplicationController.app_refresh();


                setstate();
                setmetadata();
                buildnotification();
                updateplaypause();

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("bnbn", e.toString());


            }

        }else{
            askaudio();
        }
        Log.i("bnbn", "isplaying:" + isnull() + isPlaying());

        Log.i("bnbn", "playsong ended");

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
        if (repeat){

            playsong(current_pos);
        } else {
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
        }

    }
    public  void playprev() {
        if (repeat) {

            playsong(current_pos);
        } else {
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
        }
    }
    public Long getcurrentplaybacktime(){
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
        int randomno =rand.nextInt(mylist.size()-1);
        return randomno;
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

    // for pausing music after removing headphones .. we will register/unregister this with play/pause
    private  BroadcastReceiver mbreceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isPlaying()){
                pause();
                playpausenoti=R.drawable.play_white;
                mBuilder.mActions.get(1).icon=playpausenoti;
                mNotificationManager.notify(400,mBuilder.build());
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
            Log.i("hjhj","play");
            resume();
            //NotificationCompat.getAction(mBuilder.build(),1).icon=R.drawable.pause_white;
           /* playpausenoti=R.drawable.pause_white;
            mBuilder.mActions.get(1).icon=playpausenoti;
            mNotificationManager.notify(400,mBuilder.build());
            */
            //buildnotification();
            //ApplicationController.app_refresh();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            //Log.i("hjhj","other");

            return super.onMediaButtonEvent(mediaButtonEvent);

        }


        @Override
        public void onPause() {
            super.onPause();
            Log.i("hjhj","pause");
            pause();
            //NotificationCompat.getAction(mBuilder.build(),1).icon=R.drawable.play_white;
            /*playpausenoti=R.drawable.play_white;
            mBuilder.mActions.get(1).icon=playpausenoti;
            mNotificationManager.notify(400,mBuilder.build());
            */

            //buildnotification();
            //ApplicationController.app_refresh();

        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            playnext();
            buildnotification();
            Log.i("hjhj","next");
            //ApplicationController.app_refresh();

        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            playprev();
            buildnotification();
            Log.i("hjhj","previous");
           // ApplicationController.app_refresh();

        }



        @Override
        public void onStop() {
            pause();
            Log.i("bnbnn","onstop");
            mNotificationManager.cancel(400);
            //ApplicationController.app_refresh();
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
        }else{
            playpausenoti=R.drawable.play_white;
        }
        Log.i("bnbnn","updateplaypause");
        try {
            mBuilder.mActions.get(1).icon = playpausenoti;
            mNotificationManager.notify(400, mBuilder.build());
        }catch (Exception e){e.printStackTrace();}
       // ApplicationController.app_refresh();
    }


    public void askaudio() {
        Log.i("bnbn","askaudio start");


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);


        if(result==AudioManager.AUDIOFOCUS_GAIN){
            Log.i("bnbn"," gotaudio focus");
            hasfocus=true;
            if(player==null){setplayer();}


            try{msession.setActive(true);}
            catch (Exception e){e.printStackTrace();}

        }
        Log.i("bnbnn","askaudio end");
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                //  if (mMediaPlayer == null) initMediaPlayer();
                //else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                //  mMediaPlayer.setVolume(1.0f, 1.0f);
                Log.i("bnbnn","AUDIOFOCUS_GAIN");
                if(player==null){setplayer();}
                msession.setActive(true);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                // if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                //  mMediaPlayer.release();
                //mMediaPlayer = null;
                if(isPlaying()) {
                    playpausenoti = R.drawable.play_white;
                    mBuilder.mActions.get(1).icon = playpausenoti;
                    mNotificationManager.notify(400, mBuilder.build());
                }
                hasfocus=false;
                pause();
                //player.release();
                msession.setActive(false);
                Log.i("bnbnn","AUDIOFOCUS_LOSS");

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                // if (isPlaying()) pause();
                hasfocus=false;
                msession.setActive(false);
                pause();
                Log.i("bnbnn","AUDIOFOCUS_LOSS_TRANSIENT");
                playpausenoti=R.drawable.play_white;
                if(isPlaying()) {
                    mBuilder.mActions.get(1).icon = playpausenoti;
                    mNotificationManager.notify(400, mBuilder.build());
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                //  if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                Log.i("bnbnn","AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                break;
        }

    }

    public PendingIntent getactionintent(int mediakeyevent){
        Intent intent =new Intent(Intent.ACTION_MEDIA_BUTTON);
        Log.i("hjhj",activitycontext.getPackageName());
        intent.setPackage(activitycontext.getPackageName());
        intent.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN,mediakeyevent));

        return PendingIntent.getBroadcast(this,mediakeyevent,intent,0);
    }


    }
