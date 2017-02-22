package com.androidplay.one.myplayer;

/**
 * Created by Rahul on 20-07-2016.
 */
public class exoplayer {

   //   MediaCodecAudioTrackRenderer <- ExtractorSampleSource<-DefaultUriDataSource>

/*
Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE/1024);
DataSource dataSource = new DefaultUriDataSource(context, null, userAgent);
ExtractorSampleSource sampleSource = new ExtractorSampleSource(
    uri, dataSource, allocator, BUFFER_SEGMENT_COUNT/1 * BUFFER_SEGMENT_SIZE);
MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(
    context, sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(
    sampleSource, MediaCodecSelector.DEFAULT);
 */

    /*

    // 1. Instantiate the player.
 ExoPlayer player = ExoPlayer.Factory.newInstance(RENDERER_COUNT);
 // 2. Construct renderers.
 MediaCodecVideoTrackRenderer videoRenderer = ...
 MediaCodecAudioTrackRenderer audioRenderer = ...
 // 3. Inject the renderers through prepare.
 player.prepare(videoRenderer, audioRenderer);
 // 4. Pass the surface to the video renderer.
 player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
 // 5. Start playback.
 player.setPlayWhenReady(true);
 ...
 player.release(); // Don’t forget to release when done!
     */


    //exoplayer functions
        /*public  void setplayer() {
            Log.i("serviceLife","setplayer");
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
        }*/





     /* public void playsong(int pos) {

            if(player==null){
                setEverything();
            }
           /* for equaliser
           if(!canplay){

                return;
            }
            canplay=false;
            setcanplay setpl=new setcanplay();
            setpl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
*/
        /*
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
                    Log.i("exoo", "duration of song:" + mylist.get(pos).getName() + String.valueOf(getDuration()));
                    current_id = mylist.get(pos).getId();
                    Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, current_id);

                    Log.i("urisong", trackUri.toString());

                    if (player != null && player.getPlayWhenReady()) {
                        player.stop();
                    }

                    //final int BUFFER_SEGMENT_SIZE = 64 * 1024;//initial
                    //final int BUFFER_SEGMENT_COUNT = 256;//initial

                    final int BUFFER_SEGMENT_SIZE = 32 * 1024;
                    final int BUFFER_SEGMENT_COUNT = 64;

                    String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
                    Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);//initial
                    //Allocator allocator = new DefaultAllocator(5000);

                    dataSource = new DefaultUriDataSource(this, null, userAgent);//initial
                    //dataSource = new DefaultUriDataSource(this, null);

                    //sampleSource = new ExtractorSampleSource(trackUri, dataSource, allocator, BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT);//initial
                    sampleSource = new ExtractorSampleSource(trackUri, dataSource,allocator,BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT);

                    audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);//initial
                    /*audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,MediaCodecSelector.DEFAULT){
                        @Override
                        public void onAudioSessionId(int audioSessionId) {
                            Log.i("equall","onaudiosessionid="+audioSessionId);
                                releaseEqualizer();
                                equalizer = new Equalizer(0,audioSessionId);
                                // Configure equalizer here.

                                //short[] range = equalizer.getBandLevelRange();
                             equalizer.setEnabled(true);

                        }

                        @Override
                        public void onDisabled() {
                            Log.i("equall","ondisabled=");
                            releaseEqualizer();
                        }

                        private void releaseEqualizer() {
                            Log.i("equall","releaseEqualizer start");

                            if (equalizer != null) {
                                equalizer.setEnabled(false);
                                Log.i("equall","releaseEqualizer releasing equaliser!=null");
                                equalizer.release();
                                equalizer = null;
                            }
                        }


                    };
*/
        /*
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
        */


    /*public static void restoreCurrentSongValue(){
            Log.e("serviceLife", "restorecurrentsonginfo");

            if(ApplicationController.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)){

            }else {
                if(hasfocus) {
                    Log.e("serviceLife", "restored currentsonginfo");
                    //position,seek value
                    previousstatesaved = true;
                    SharedPreferences sp = myContext.getSharedPreferences("MyPlayer_CurrentsongInfo", Activity.MODE_PRIVATE);
                    int pos = sp.getInt("service_currentSongPositionInList", 0);
                    current_pos = pos;
                    final Long seek = sp.getLong("service_currentSongSeekValue", 100);
                    player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f);
                    ((playerservice) myContext).playsongwithoutnotification(pos);
                    seekTo(seek);
                    ((playerservice) myContext).pause();
                    player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f);
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
*/
     /*
    for mp3agic  id3 edit
    private void setid3image(){

            File f=new File(sdata);
            boolean w=f.setWritable(true);
        Log.i("fgdd","setWritable "+w);
        boolean r=f.setReadable(true);
        Log.i("fgdd","setReadable "+r);
        try {
            Mp3File mp3file=new Mp3File(sdata);

            if(mp3file.hasId3v2Tag()){
                Log.i("fgdd"," have id3v2 tag");

            }else if(mp3file.hasId3v1Tag()){
                Log.i("fgdd"," have id3v1 tag");

            }else if(mp3file.hasCustomTag()){
                Log.i("fgdd"," have custom tag");
            }
            ID3v2 id3v2Tag;
            if(mp3file.hasId3v2Tag()){
                //have id3v2tag ,add image to it
                id3v2Tag = mp3file.getId3v2Tag();
                byte[] imagearray= new byte[0];
                try {
                    imagearray = readBytes(newImage);
                    id3v2Tag.setAlbumImage(imagearray,getContentResolver().getType(newImage));
                } catch (IOException e) {
                    Log.i("fgdd","IOException read bytes");
                    Log.i("fgdd",e.toString());
                    e.printStackTrace();
                }
                try {
                    mp3file.save(name.getText().toString().trim());
                } catch (IOException e) {
                    Log.i("fgdd","IOException save");
                    Log.i("fgdd",e.toString());

                    e.printStackTrace();
                }

                Log.i("fgdd","album art changed success");
            }else{
                //dont have id3v2
                id3v2Tag = new ID3v24Tag();

                if(mp3file.hasId3v1Tag()){
                    //copy from id3v1 to new id3v2
                    Log.i("fgdd"," dont have id3v2 but have id3v1 tag");
                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                    mp3file.setId3v2Tag(id3v2Tag);


                    id3v2Tag.setTrack(id3v1Tag.getTrack());
                    id3v2Tag.setArtist(id3v1Tag.getArtist());
                    id3v2Tag.setTitle(id3v1Tag.getTitle());
                    id3v2Tag.setAlbum(id3v1Tag.getAlbum());
                    id3v2Tag.setYear(id3v1Tag.getYear());
                    id3v2Tag.setGenre(id3v1Tag.getGenre());
                    id3v2Tag.setComment(id3v1Tag.getComment());
                    id3v2Tag.setGenreDescription(id3v1Tag.getGenreDescription());
                    //id3v2Tag.setLyrics("Some lyrics");
                    //id3v2Tag.setComposer("The Composer");
                    //id3v2Tag.setPublisher("A Publisher");
                    //id3v2Tag.setOriginalArtist("Another Artist");
                    //id3v2Tag.setAlbumArtist("An Artist");
                    //id3v2Tag.setCopyright("Copyright");
                    //id3v2Tag.setUrl("http://foobar");
                    //id3v2Tag.setEncoder("The Encoder");
                    byte[] imagearray= new byte[0];

                    try {
                        imagearray = readBytes(newImage);
                        id3v2Tag.setAlbumImage(imagearray,getContentResolver().getType(newImage));
                    } catch (IOException e) {
                        Log.i("fgdd","IOException read bytes");
                        Log.i("fgdd",e.toString());
                        e.printStackTrace();
                    }
                    try {
                        mp3file.save(name.getText().toString().trim());
                    } catch (IOException e) {
                        Log.i("fgdd","IOException save");
                        Log.i("fgdd",e.toString());

                        e.printStackTrace();
                    }

                }else{
                    Log.i("fgdd"," dont have id3v2 creating new id3v2 tag from edittext values");

                    //create new id3v2 tag and add editext values to it
                    mp3file.setId3v2Tag(id3v2Tag);
                    id3v2Tag.setTitle(name.getText().toString().trim());
                    id3v2Tag.setAlbum(album.getText().toString().trim());
                    id3v2Tag.setArtist(artist.getText().toString().trim());
                    id3v2Tag.setYear(year.getText().toString().trim());

                    byte[] imagearray= new byte[0];
                    try {
                        imagearray = readBytes(newImage);
                        id3v2Tag.setAlbumImage(imagearray,getContentResolver().getType(newImage));
                    } catch (IOException e) {
                        Log.i("fgdd","IOException read bytes");
                        Log.i("fgdd",e.toString());
                        e.printStackTrace();
                    }
                    try {
                        mp3file.save(name.getText().toString().trim());
                    } catch (IOException e) {
                        Log.i("fgdd","IOException save");
                        Log.i("fgdd",e.toString());

                        e.printStackTrace();
                    }


                }
            }
        } catch (UnsupportedTagException e) {
            Log.i("fgdd","UnsupportedTagException");
            Log.i("fgdd",e.toString());
            e.printStackTrace();
        } catch (InvalidDataException e) {
            Log.i("fgdd","InvalidDataException");
            Log.i("fgdd",e.toString());
            e.printStackTrace();
        } catch (NotSupportedException e) {
            Log.i("fgdd","NotSupportedException");
            Log.i("fgdd",e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("fgdd",e.toString());
            e.printStackTrace();
        }
    }
*/
}
