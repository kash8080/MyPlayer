    package com.androidplay.rahul.myplayer;

    import android.Manifest;
    import android.animation.Animator;
    import android.animation.AnimatorListenerAdapter;
    import android.animation.ObjectAnimator;
    import android.content.ContentResolver;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.res.ColorStateList;
    import android.database.Cursor;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.Color;
    import android.graphics.drawable.Animatable;
    import android.graphics.drawable.Drawable;
    import android.net.Uri;
    import android.os.AsyncTask;
    import android.os.Build;
    import android.preference.PreferenceManager;
    import android.provider.MediaStore;
    import android.support.design.widget.AppBarLayout;
    import android.support.design.widget.CollapsingToolbarLayout;
    import android.support.design.widget.FloatingActionButton;
    import android.support.v4.content.ContextCompat;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.graphics.Palette;
    import android.support.v7.widget.LinearLayoutManager;
    import android.support.v7.widget.RecyclerView;
    import android.support.v7.widget.Toolbar;
    import android.transition.Transition;
    import android.transition.TransitionInflater;
    import android.util.Log;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewAnimationUtils;
    import android.view.ViewGroup;
    import android.widget.FrameLayout;
    import android.widget.ImageView;
    import android.widget.RelativeLayout;
    import android.widget.TextView;

    import com.transitionseverywhere.Crossfade;
    import com.transitionseverywhere.Fade;
    import com.transitionseverywhere.Recolor;
    import com.transitionseverywhere.TransitionManager;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Random;

    import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class open_playlist extends AppCompatActivity implements recycler_adapter.playlist_data,View.OnClickListener{

    RecyclerView rec_view;
    RecyclerView.LayoutManager mlayoutmanager;
    recycler_adapter adapter;
    ContentResolver resolver=null;
    ArrayList<songs> list;
    Long playlist_id,current_id;
    ApplicationController con;
    String tag="tstngss";
    String tag1="tstngsss";
    Palette.Swatch swatch;
    Palette.Swatch swatchaccent;
    String method,album_art=null;
    Long album_id;
    boolean listset=false;
    Toolbar toolbar,desc_toolbar;
    String title="";
    ImageView image,over_image,image1,image2,image3,image4;
    TextView over_title,over_artist,numberofsongs;
    String playall="false";
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Bitmap bitmap;
    FloatingActionButton fab1;
    ArrayList<String> albumartlist;
    FrameLayout framelayout;
    boolean isplaylist=false;
        boolean hassavedlist=false;
        View image_foreground_colour;
        VerticalRecyclerViewFastScroller fastScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            Log.i("activitystate","oncreate");
        con=new ApplicationController(this.getApplicationContext(),this);
        list=new ArrayList<>();
        albumartlist=new ArrayList<>();
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
            if(con.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
                startActivity(new Intent(this, MainActivity.class));
                //finish called to stop further proccess of this activity
                finish();
            }else{
                String s=(String)savedInstanceState.get("instancesaved");
                if(s!=null && s.equals("true")){
                    Log.i("activitystate","restored instance state");
                    hassavedlist=true;
                    list=con.currentactivitySavedList;
                    albumartlist=con.currentactivityalbumartlist;
                    Log.i("activitystate","list.size()="+String.valueOf(list.size()));
                    Log.i("activitystate","albumartlist.size()="+String.valueOf(albumartlist.size()));

                }

                super.onCreate(savedInstanceState);
            }
        }
        doasync inback=new doasync();
        inititalise();

        try{
            method=getIntent().getStringExtra("method");
            if(method.equals("playlist")){
                isplaylist=true;
                playlist_id=getIntent().getLongExtra("playlist_id",0);
                title=getIntent().getStringExtra("playlist_name");
                inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,1);
                //get_playlist();
                //to animate sliding of the activity
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                current_id=playlist_id;
            }else if(method.equals("album")) {
                album_art=getIntent().getStringExtra("album_art");
                title=getIntent().getStringExtra("album_name");
                playall=getIntent().getStringExtra("album_playall");
                album_id=getIntent().getLongExtra("album_id",0);
                current_id=album_id;
                inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,0);
                //getalbum();
            }
        }catch (Exception e){}

        over_title.setText(title);
        String string=list.size()+" songs";
        numberofsongs.setText(string);
            desc_toolbar.setVisibility(View.INVISIBLE);
        if(method.equals("playlist")){
            adapter=new recycler_adapter(this,list,"open_playlist");
        }else{
            if(playall.equals("true")){
                adapter=new recycler_adapter(this,list,"open_album_true");
            }else{
                adapter=new recycler_adapter(this,list,"open_album");
            }
            rec_view.setAdapter(adapter);
        }

            Log.i(tag,"adapter done in oncreate");
            rec_view.setLayoutManager(mlayoutmanager);
            Log.i(tag,"setting adapter");
            rec_view.setAdapter(adapter);

             fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller2);

            // Connect the recycler to the scroller (to let the scroller scroll the list)
            fastScroller.setRecyclerView(rec_view);

            // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
            rec_view.addOnScrollListener(fastScroller.getOnScrollListener());
            if(!isplaylist){
                fastScroller.setVisibility(View.INVISIBLE);
            }else{
                fastScroller.setVisibility(View.VISIBLE);
            }

        refreshview();
        framelayout=(FrameLayout) findViewById(R.id.multiple_image_frame);
        framelayout.setBackgroundColor(con.getPrimary());
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        fab1.setOnClickListener(this);
        appBarLayout= (AppBarLayout) findViewById(R.id.MyAppbar);

        handle_enter_transition();
        addListener_to_appbar();

    }
    public void inititalise(){
            setContentView(R.layout.activity_open_playlist);

            rec_view=(RecyclerView)findViewById(R.id.rec_view2);
            mlayoutmanager=new LinearLayoutManager(this);

            toolbar=(Toolbar)findViewById(R.id.MyToolbar);
            collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
            image=(ImageView)findViewById(R.id.album_image);
            image1=(ImageView)findViewById(R.id.album_image1);
            image2=(ImageView)findViewById(R.id.album_image2);
            image3=(ImageView)findViewById(R.id.album_image3);
            image4=(ImageView)findViewById(R.id.album_image4);
            desc_toolbar=(Toolbar)findViewById(R.id.desc_bar);
            over_image=(ImageView)findViewById(R.id.over_image);
            over_title=(TextView)findViewById(R.id.overtitle);
            over_artist=(TextView)findViewById(R.id.over_artist);
            numberofsongs=(TextView)findViewById(R.id.numberofsongs);
            fab1=(FloatingActionButton)findViewById(R.id.fab1);
            image_foreground_colour=findViewById(R.id.image_foreground_colour);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            resolver = getContentResolver();

        }

    public void handle_enter_transition(){

        try {
            if (Build.VERSION.SDK_INT >= 21) {
                Transition enter;
                try {
                    enter = TransitionInflater.from(this).inflateTransition(R.transition.fade_edited);
                } catch (Exception e) {
                    enter = TransitionInflater.from(this).inflateTransition(R.transition.fade_edited_1);
                }
                getWindow().setEnterTransition(enter);
                enter.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                        fab1.animate().scaleX(0f).scaleY(0f).setDuration(1);
                        rec_view.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        fab1.animate().scaleX(1f).scaleY(1f).setDuration(100);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().getEnterTransition().removeListener(this);
                        }
                        rec_view.setVisibility(View.VISIBLE);
                        ObjectAnimator object1 = ObjectAnimator.ofFloat(rec_view, "translationY", (200), 0);
                        object1.setDuration(300);
                        object1.start();
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
            }
        }catch (Exception e){
            Log.i("",e.toString());
            e.printStackTrace();
        }
    }

    public void addListener_to_appbar(){
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollrange=-1;
            boolean isShow=false;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.i("ddddff","vertical offset="+String.valueOf(verticalOffset));
                if (scrollrange == -1) {
                    collapsingToolbarLayout.setTitle(" ");
                    scrollrange = appBarLayout.getTotalScrollRange();
                    Log.i("ddddff","scroll range="+String.valueOf(scrollrange));
                }
                if (scrollrange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(title);
                    numberofsongs.setTextColor(Color.TRANSPARENT);
                    //collapsingToolbarLayout.setCollapsedTitleTextColor(Color.GRAY);
                    isShow = true;
                } else if(isShow) {
                    numberofsongs.setTextColor(Color.WHITE);
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }

    public void refreshview(){
        Log.i("activitystate","refreshview ");
        if(list!=null && albumartlist!=null) {
            Log.i("activitystate", "list.size()=" + String.valueOf(list.size()));
            Log.i("activitystate", "albumartlist.size()=" + String.valueOf(albumartlist.size()));
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        if(list.size()>15){
            fastScroller.setVisibility(View.VISIBLE);
            int end_margin=getResources().getInteger(R.integer.Recview_endmargin);
            params.setMargins(0,0,end_margin,0);
            rec_view.setLayoutParams(params);

        }else{
            params.setMargins(0,0,0,0);
            rec_view.setLayoutParams(params);
            fastScroller.setVisibility(View.INVISIBLE);
        }
         desc_toolbar.setVisibility(View.VISIBLE);
         over_title.setVisibility(View.VISIBLE);
            image_foreground_colour.setVisibility(View.VISIBLE);

            over_title.setText(title);
        String string=list.size()+" songs";
        numberofsongs.setText(string);
        Log.i("lkll","pop1");
        if(album_art!=null){
            bitmap = BitmapFactory.decodeFile(album_art);
            if(bitmap!=null){
                over_image.setVisibility(View.VISIBLE);
                over_image.setImageBitmap(bitmap);
                image.setVisibility(View.VISIBLE);
                image.setImageBitmap(bitmap);
            }else{
                Log.i("lkll","pop");
                over_image.setVisibility(View.INVISIBLE);
            }
        }else{

            desc_toolbar.setBackgroundColor(con.getPrimary());
            Log.i("lkll","pop2");
            image_foreground_colour.setVisibility(View.INVISIBLE);
            numberofsongs.setText(title);
            image.setVisibility(View.VISIBLE);
            over_title.setVisibility(View.INVISIBLE);
            over_image.setVisibility(View.INVISIBLE);
        }
        // for getting colors from bitmap  using pallete lbrary
        if(bitmap!=null) {

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    // access palette colors here
                    swatch = palette.getDarkMutedSwatch();
                    swatchaccent = palette.getVibrantSwatch();

                    //Log.i("lkll"," swatch values are "+String.valueOf(vibrant)+"\n"+String.valueOf(c)+"\n"+String.valueOf(d)+"\n"+
                    //String.valueOf(f)+"\n"+String.valueOf(a)+"\n"+String.valueOf(b));
                    if (swatch == null) {
                        swatch = palette.getMutedSwatch();
                    }
                    try {
                        Log.i("lkll", "pop3");
                        framelayout.setBackgroundColor(con.getPrimaryLight());

                        //toolbar.setBackgroundColor(swatch.getRgb());
                        //TransitionManager.beginDelayedTransition(desc_toolbar,new Recolor());
                        try {
                            desc_toolbar.setBackgroundColor(swatch.getRgb());
                            collapsingToolbarLayout.setContentScrimColor(swatch.getRgb());
                            fab1.setBackgroundTintList(ColorStateList.valueOf(swatchaccent.getRgb()));
                        }catch (Exception e){
                            desc_toolbar.setBackgroundColor(con.getPrimary());
                            collapsingToolbarLayout.setContentScrimColor(con.getPrimary());
                            fab1.setBackgroundTintList(ColorStateList.valueOf(con.getAccent()));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            //to darken the status ba colour
                            float[] hsv = new float[3];
                            int color = swatch.getRgb();
                            Color.colorToHSV(color, hsv);
                            hsv[2] *= 0.6f; // value component
                            color = Color.HSVToColor(hsv);
                            getWindow().setStatusBarColor(color);
                            Log.i("lkll", "pop3");
                        }

                    } catch (Exception e) {
                        Log.i("lkll", String.valueOf(e.toString()));
                    }
                }
            });
        }
    }

    public void refreshNoOfSongs() {
        if (bitmap != null) {
            String string = list.size() + " songs";
            numberofsongs.setText(string);
        }
    }
    public void getalbum(){
    String selection = "is_music != 0";

    if (album_id > 0) {
        selection = selection + " and album_id = " + album_id;
    }

    String[] projection = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media._ID
    };
    final String sortOrder = MediaStore.Audio.Media.TITLE ;

    Cursor cursor = null;
    try {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        cursor = resolver.query(uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            Log.i(tag1,"nnnnnnnnnnn cursor!=null");
            while (cursor.moveToNext()) {
                Log.i(tag1,"move to next");
               String name=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                //song.setDuration(cursor.getLong(4));
                String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                //String path=cursor.getString(2);
                //song.setAlbumId(cursor.getLong(6));
                long id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                songs song =new songs(id,name,artist,"",album_id);
                list.add(song);Log.i(tag1,"song added");
                Log.i(tag1,name+" "+artist);
            }
        }

    } catch (Exception e) {
        Log.i(tag1,"catch");
        Log.e("Media", e.toString());
    } finally {
        if (cursor != null) {
            try{cursor.close();}catch (Exception e){e.printStackTrace();}
        }
    }
    Log.i(tag1,"list: "+list.size());
    new doasync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,2);

    }
    public ArrayList<songs> get_playlist(){
            Log.i(tag,"getting playlistg :"+playlist_id);
            Log.i("activitystate","getplayllist");
        if(hassavedlist){
            if(albumartlist!=null && albumartlist.size()>0){
                album_art=albumartlist.get(0);
            }
            new doasync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,2);
            return list;
        }
        songs song;Log.i(tag,"--");
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);


        Log.i(tag,"--");
        String[] projection = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.ALBUM_ID


        };
        Cursor tracks = resolver.query(uri,projection, null, null, null);Log.i(tag,"--");
            if(tracks!=null){
                Log.i(tag,"not null");
                while(tracks.moveToNext()) {
                    Log.i(tag,"ccc");
                    String name =tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                    Long id=Long.parseLong(tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
                    String artist=tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                    Long albumid=tracks.getLong(tracks.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                    song =new songs(id,name,artist,"",albumid);
                    Log.i(tag,"--");
                    list.add(song);

                }
                try{Log.i(tag,"try");tracks.close();}catch (Exception e){e.printStackTrace();}
            }

        Log.i(tag,"returning list of size"+list.size());
        new doasync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,2);
        Log.i("lkll","getplaylist end");

        return list;

    }
    public void setalbumartfromsongs(){
        Log.i("activitystate","setalbumart ");

        if(hassavedlist){
            if(albumartlist!=null && albumartlist.size()>0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshview();
                    }
                });
            }
           return;
        }
        albumartlist=new ArrayList<>();
        songs current;
        Cursor cursor;
        boolean first=true;
        final String _id = MediaStore.Audio.Albums._ID;
        final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
        String[] projection={ _id,albumart};

        String path=null;

        for(int i=0;i<list.size();i++){

            current=list.get(i);
            try {
                cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, "_ID=" + current.getAlbum_id(),
                        null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                        current.setImagepath(path);
                        Log.i("albmart",path);
                       if(path!=null){
                          if(first){
                              album_art=path;
                              runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                        refreshview();
                                  }
                              });

                              first=false;
                           }
                           albumartlist.add(path);
                           //return;
                       }
                    }
                    cursor.close();
                }
            }catch (Exception e){Log.i("aaaa","fffff");}

        }
        Log.i("lkll","setalbumart end");

    }

    @Override
    public void onBackPressed() {

    fab1.animate().scaleX(0f).scaleY(0f).setDuration(50).withEndAction(new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //getWindow().setExitTransition(null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    supportFinishAfterTransition();

                }else{
                    finish();
                    open_playlist.super.onBackPressed();
                }

                //finish();
            }

        }
    });

    }

    @Override
    protected void onPause() {
    if(isplaylist) {
        overridePendingTransition(R.anim.slide_in_fromleft, R.anim.slide_out_toright);
    }
    canrun=false;
    super.onPause();
    }

    @Override
    protected void onStop() {
    super.onStop();
    listset=false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    int id =item.getItemId();
    switch (id){

        case android.R.id.home:{
            fab1.animate().scaleX(0f).scaleY(0f).setDuration(50).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        supportFinishAfterTransition();
                    }else{
                        finish();
                    }

                }
            });
            //finish();
            return true;
        }
        default:return super.onOptionsItemSelected(item);
    }
    }

    public void refreshfab() {
    if(current_id.equals(con.open_playlist_id)&&con.isPlaying()){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause_white));
        }else{
            fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause_white));
        }


    }else{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play_white));
        }else{
            fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play_white));
        }        }
    Drawable drawable = fab1.getDrawable();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        ((Animatable)drawable).start();
    }

    }

    @Override
    public Long getplaylist_id() {
    return playlist_id;
    }

    public void play_all_songs(){
    con.setMylist(list,"open_playlist",false);
    con.open_playlist_id=current_id;
    con.playsong(0);
    }
    @Override
    public void onClick(View v) {
    if(v.getId()==R.id.fab1){
        //TransitionManager.beginDelayedTransition(fab1);


        if(current_id.equals(con.open_playlist_id) && con.isPlaying()){
            //pause the button
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play_white));
            }else{
                fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play_white));
            }
            con.pause();
        }else if(current_id.equals(con.open_playlist_id) && !con.isPlaying()){
            //pause the button
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause_white));
            }else{
                fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause_white));
            }
            con.resume();
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause_white));
            }else{
                fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause_white));
            }

            //play all songs
            play_all_songs();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = fab1.getDrawable();
            ((Animatable)drawable).start();
        }

    }
    }
    public class doasync extends AsyncTask<Integer,Void,Void>{
    int i;
    @Override
    protected Void doInBackground(Integer... integers) {
        i=integers[0];
        if(i==0){
            getalbum();
        }else if(i==1){
            get_playlist();
        }else if(i==2){
            setalbumartfromsongs();
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        //if album or after downloading images of playlist
        //rec_view.setAdapter(adapter);
        if(i==2 && isplaylist && albumartlist.size()>=4){
            make_multiple_images();
        }
        if(i==1 || i==2){
            adapter.notifyDataSetChanged();
        }
        //refreshview();
        super.onPostExecute(aVoid);
    }
    }

    public void checkmultipleimages(){
    if(albumartlist.size()>=4){
        make_multiple_images();
    }
    }
    int current_image_no=1;
    boolean canrun=true;

    public void make_multiple_images(){

        //--
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            try {
                // get the center for the clipping circle
                int cx = image.getWidth() / 2;
                int cy = image.getHeight() / 2;

                // get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(cx, cy);

                // create the animation (the final radius is zero)
                Animator anim = ViewAnimationUtils.createCircularReveal(image, cx, cy, initialRadius, 0);

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        image.setVisibility(View.INVISIBLE);
                    }
                });

            // start the animation
                anim.start();
            }catch (Exception e){
                image.setVisibility(View.INVISIBLE);
            }
        }else{
            TransitionManager.beginDelayedTransition(collapsingToolbarLayout);
            image.setVisibility(View.INVISIBLE);
        }
        //--
    changeImages(1);
    changeImages(2);
    changeImages(3);
    changeImages(4);
    delayedimagchange del=new delayedimagchange();
    del.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public void changeImages(int i){
    switch (i){
        case 1:{
            set_Image(image1,albumartlist.get(getnextImageNo()));
            current_image_no++;
            break;
        }
        case 2:{
            set_Image(image2,albumartlist.get(getnextImageNo()));
            current_image_no++;
            break;
        }
        case 3:{
            set_Image(image3,albumartlist.get(getnextImageNo()));
            current_image_no++;
            break;
        }
        case 4:{
            set_Image(image4,albumartlist.get(getnextImageNo()));
            current_image_no++;
            break;
        }

    }




    }

    public int getnextImageNo(){
    if(current_image_no<albumartlist.size()){
        return current_image_no;
    }else{
        current_image_no=0;
        return 0;
    }
    }

    public void set_Image(ImageView img ,String path){
    if(path!=null){
        bitmap = BitmapFactory.decodeFile(path);
        if(bitmap!=null) {
            img.setVisibility(View.INVISIBLE);
            com.transitionseverywhere.TransitionManager.beginDelayedTransition(collapsingToolbarLayout,new com.transitionseverywhere.Slide());
            img.setVisibility(View.VISIBLE);
            img.setImageBitmap(bitmap);
        }
    }
    }
    Random rand=new Random();
    public class delayedimagchange extends AsyncTask<Void,Void,Void>{
    int i=3;
    @Override
    protected Void doInBackground(Void... voids) {
        while (canrun) {
            try {
                Log.i("bckg","doinback");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress();

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        int t=rand.nextInt(4);
        while(t==i){
            t=rand.nextInt(4);
        }
        i=t;
        changeImages(i+1);

    }
    }

    @Override
    protected void onResume() {
        Log.i("activitystate","onresume");
    super.onResume();
    refreshfab();
    }

    @Override
    protected void onStart() {
    super.onStart();
    canrun=true;
    }

    @Override
    protected void onRestart() {
    super.onRestart();
    canrun=true;
    checkmultipleimages();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i("activitystate","onsaveinstance");
        if(list!=null && list.size()>0) {
            outState.putString("instancesaved", "true");
            con.currentactivitySavedList=list;
            con.currentactivityalbumartlist=albumartlist;

        }
        super.onSaveInstanceState(outState);

    }

        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            Log.i("activitystate","onrestoreinstance");
            super.onRestoreInstanceState(savedInstanceState);
        }
}



