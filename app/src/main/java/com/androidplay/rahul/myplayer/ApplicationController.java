package com.androidplay.rahul.myplayer;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Base64;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import android.Manifest;
/**
 * Created by Rahul on 16-07-2016.
 */
public class ApplicationController extends Application {
    public static Intent playIntent = null;
    public static Context applicationcontext;
    public static Context activitycontext;

    public static String currenntlistof="" ;
    public static boolean withimages=false;
    private static ApplicationController thiscontext;
    private static playerservice musicSrv;
    public static ArrayList<songs> allsonglist ;

    //to restore list
    public static ArrayList<songs> currentactivitySavedList;
    public static ArrayList<String> currentactivityalbumartlist;

    private static ContentResolver resolver;
    background loadimagesforallsongs;
    public static Long open_playlist_id=0L;
    static boolean serviceconnected=false;
    ////////------------------------------------------

    public ApplicationController() {

    }
    public ApplicationController(Context applicationcontext, Context Activitycontext) {
        Log.i("qwsd","con constructor");

        this.applicationcontext = Activitycontext.getApplicationContext();
        this.activitycontext = Activitycontext;

        if (musicSrv != null) {
            musicSrv.setcontext(applicationcontext);
        }
    }

    public MediaSessionCompat.Token getMediaSessionToken(){
        if (musicSrv != null) {
            return musicSrv.msession.getSessionToken();
        }
        else return null;
    }
    @Override
    public void onCreate() {
        Log.i("qwsd","con oncreate");
        thiscontext=this;
        super.onCreate();
        allsonglist=new ArrayList<>();
        resolver=this.getContentResolver();

        //music service setup start
        if(playIntent==null) {
            playIntent = new Intent(this, playerservice.class);
            Log.i("qwsd", "con try");
            startService(playIntent);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
        //music service setup end

        universl_image_loader();
        // printhashkey();
        Log.i("llllp","con on create end");

    }

    // when user kills the task with swiping from recent tasks
    @Override
    public void onTerminate() {
        Log.i("qwsd","con onterminate");
        super.onTerminate();
        loadimagesforallsongs.cancel(true);
    }

    ///for connectiong to the service
    public static   ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerservice.MusicBinder binder = (playerservice.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            //musicSrv.setMylist(mylist);
            if(activitycontext!=null){
                musicSrv.setcontext(activitycontext);
            }
            //musicSrv.removefromforeground();
            serviceconnected=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("qwsd","con service unbinded");
            //Log.i("qwsd","con service data="+musicSrv.currentsongno);
            serviceconnected=false;
            musicSrv=null;

        }
    };


    public void loadsongswithimages(){
        Log.i("llllp","load with images");

        if(needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)){
            startpermissionactivity();

        }else {
            fetchallsonglist();
            loadimagesforallsongs = new background();
            loadimagesforallsongs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    public boolean needforpermissions(String permission){

        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1){
            Log.i("llllp","sdk1");

            if(ContextCompat.checkSelfPermission(activitycontext,permission)!=PackageManager.PERMISSION_GRANTED){
                Log.i("llllp","sdk2");

                return true;
            }
        }
        return false;
    }
    public void startpermissionactivity(){
        Log.i("llllp","start activity");

        Intent permissionactivity = new Intent("com.rahul.permission");//this has to match your intent filter
        permissionactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(activitycontext, 22, permissionactivity, 0);
        try {
            pendingIntent.send();
        }
        catch (PendingIntent.CanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Log.i("llllp","start activity end");

    }

    private void universl_image_loader(){

// UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.ic_launcher) // resource or drawable
                .showImageForEmptyUri(R.mipmap.ic_launcher) // resource or drawable
                .showImageOnFail(R.mipmap.ic_launcher) // resource or drawable
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default

                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .handler(new Handler()) // default
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(options)
                .memoryCache(new WeakMemoryCache())
                .build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

    }

    public void printhashkey() {
        try {
            Log.i("KeyHash1", "printhashkey");
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.rahul.myplayer",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash1", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("KeyHash1", "error 1");
            e.printStackTrace();

            Log.i("KeyHash1", e.toString());


        } catch (NoSuchAlgorithmException e) {
            Log.i("KeyHash1", "error 2");

        }
    }
    private boolean isWifiDirectSupported(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo info : features) {
            if (info != null && info.name != null && info.name.equalsIgnoreCase("android.hardware.wifi.direct")) {
                return true;
            }
        }
        return false;
    }

    //to update current list of songs of current activity(playlist,album etc) .
    public static void setMylist(ArrayList<songs> list,String from,boolean withimg) {

        ApplicationController.currenntlistof=from;
        ApplicationController.withimages=withimg;
        musicSrv.setMylist(list);
        Log.i("qwsd","set my list --current list of- "+currenntlistof);

        //to reset current playlist or album id
        open_playlist_id=0L;
    }
    public void addSongToList(ArrayList<songs> songlist){
        musicSrv.addsongstolist(songlist);
    }
    //functions for the service and activity interaction
    public static void setplayer() {

        musicSrv.setplayer();
    }
    public static int getCurrentPosition() {

        return musicSrv.getCurrentPosition();
    }
    public static void setCurrent_pos(int current_pos1) {

        musicSrv.setCurrent_pos(current_pos1);
    }
    public static void pause() {

        musicSrv.pause();
    }
    public static songs getsong() {

        return musicSrv.getsong();
    }
    public static void resume() {

        musicSrv.resume();
    }
    public void seekTo(Long positionms) {

         musicSrv.seekTo(positionms);
    }
    public static Long getDuration() {

        return musicSrv.getDuration();

    }
    public static void playsong(int pos) {
        Log.i("clist","current list of- "+currenntlistof);
        musicSrv.playsong(pos);


    }
    public static boolean isPlaying() {

    return musicSrv.isPlaying();

    }
    public static void setnull() {

        musicSrv.setnull();
    }
    public static void playnext() {

        musicSrv.playnext();
    }
    public static void playprev() {

        musicSrv.playprev();
    }
    public Long getcurrentplaybacktime(){

        return  musicSrv.getcurrentplaybacktime();
    }
    public static ArrayList<songs> getlist() {

        return musicSrv.getlist();
    }
    public static void notifydatachange(int id, int from , int to){

        musicSrv.notifydatachange(id,from,to);
    }
    public static void remove_song(int i){
        musicSrv.remove_song(i);
    }
    public static boolean isnull() {

        return musicSrv.isnull();
    }
    public static boolean isRepeat() {
        return musicSrv.isRepeat();
    }
    public void setRepeat(boolean repeat) {
        musicSrv.setRepeat(repeat);

    }
    public static boolean isShuffle() {
        return musicSrv.isShuffle();
    }
    public void setShuffle(boolean shuffle) {
        musicSrv.setShuffle(shuffle);

    }
    public void addSongtoNextPos(songs s){
        musicSrv.addSongToNextPos(s);
    }

    public static songs getSongById(Long id){
        for (songs s:allsonglist){
            if(id.equals(s.getId())){
                return s;
            }
        }
        return new songs(0L,"","");
    }

    public int getPrimary(){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activitycontext);
        String thme=sharedPref.getString("THEME_LIST","1") ;
        switch (thme){
            case "1":return ContextCompat.getColor(activitycontext,R.color.colorPrimary);
            case "2":return ContextCompat.getColor(activitycontext,R.color.colorPrimarypurple);
            case "3":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryred);
            case "4":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryorange);
            case "5":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryindigo);
            case "6":return ContextCompat.getColor(activitycontext,R.color.colorPrimarybrown);
            default:return ContextCompat.getColor(activitycontext,R.color.colorPrimary);
        }

    }
    public int getPrimaryDark(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activitycontext);
        String thme=sharedPref.getString("THEME_LIST","1") ;
        switch (thme){
            case "1":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryDark);
            case "2":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryDarkpurple);
            case "3":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryDarkred);
            case "4":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryDarkorange);
            case "5":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryDarkindigo);
            case "6":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryDarkbrown);
            default:return ContextCompat.getColor(activitycontext,R.color.colorPrimaryDark);
        }
    }
    public int getAccent(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activitycontext);
        String thme=sharedPref.getString("THEME_LIST","1") ;
        switch (thme){
            case "1":return ContextCompat.getColor(activitycontext,R.color.colorAccent);
            case "2":return ContextCompat.getColor(activitycontext,R.color.colorAccentpurple);
            case "3":return ContextCompat.getColor(activitycontext,R.color.colorAccentred);
            case "4":return ContextCompat.getColor(activitycontext,R.color.colorAccentorange);
            case "5":return ContextCompat.getColor(activitycontext,R.color.colorAccentindigo);
            case "6":return ContextCompat.getColor(activitycontext,R.color.colorAccentbrown);
            default:return ContextCompat.getColor(activitycontext,R.color.colorAccent);
        }
    }
    public int getPrimaryLight(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activitycontext);
        String thme=sharedPref.getString("THEME_LIST","1") ;
        switch (thme){
            case "1":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryLight);
            case "2":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryLightpurple);
            case "3":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryLightred);
            case "4":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryLightorange);
            case "5":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryLightindigo);
            case "6":return ContextCompat.getColor(activitycontext,R.color.colorPrimaryLightbrown);
            default:return ContextCompat.getColor(activitycontext,R.color.colorPrimaryLight);
        }
    }


    //functions for list of all songs
    public static ArrayList<songs> getAllsonglist() {
        return allsonglist;
    }
    public static void setAllsonglist(ArrayList<songs> allsonglist) {
        ApplicationController.allsonglist = allsonglist;
    }

    public String getimagepathforsong(int i){
        return allsonglist.get(i).getImagepath();
    }

    //to load all the songs
    public void fetchallsonglist(){
        allsonglist=new ArrayList<>();
        Log.i("llllp","fetch list");
        String[] proj={android.provider.MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media._ID,
                android.provider.MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                };


        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor;
        try {
            Log.i("llllp","try");

            musicCursor = activitycontext.getContentResolver().query(musicUri, proj, null, null, null);
        }catch(java.lang.SecurityException e){
            e.printStackTrace();
            loadsongswithimages();
            return;
        }catch(Exception ee){
            ee.printStackTrace();
            loadsongswithimages();
            return;
        }
        Log.i("llllp","done");

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns

            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int datacolumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);


            //add songs to list
            do {
                Long albumid=musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisdata = musicCursor.getString(datacolumn);

                allsonglist.add(new songs(thisId, thisTitle, thisArtist,"",albumid,thisdata));

            }
            while (musicCursor.moveToNext());
        }
        try{

            musicCursor.close();}catch (Exception e){e.printStackTrace();}
        Collections.sort(allsonglist, new Comparator<songs>(){
            public int compare(songs a, songs b){
                return a.getName().compareTo(b.getName());
            }
        });
    }

    //to load album images of songs
    public class background extends AsyncTask<Void,Void,Void> {

        boolean canrun=true;

        @Override
        protected void onCancelled() {
            super.onCancelled();
            canrun=false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            songs current;
            Cursor cursor;


            final String _id = MediaStore.Audio.Albums._ID;
            final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
            String[] projection={ _id,albumart};

            String path=null;

            for(int i=0;i<allsonglist.size();i++){

                if(!canrun){
                    // if cancelled
                    break;
                }
                Log.i("pwpw",String.valueOf(i)+"th song image loading..");
                current=allsonglist.get(i);
                try {
                    cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, "_ID=" + current.getAlbum_id(),
                            null, null);

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                            current.setImagepath(path);
                            if(path!=null) {
                                Log.i("pwpw", String.valueOf(i) + "th song image loaded..");
                            }

                        }
                        cursor.close();
                    }
                }catch (Exception e){Log.i("pwpw","fffff");}

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void songses) {
            super.onPostExecute(songses);

        }
    }

}
