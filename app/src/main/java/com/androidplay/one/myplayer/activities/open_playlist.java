    package com.androidplay.one.myplayer.activities;

    import android.Manifest;
    import android.animation.Animator;
    import android.animation.AnimatorListenerAdapter;
    import android.animation.ObjectAnimator;
    import android.app.AlertDialog;
    import android.content.ContentResolver;
    import android.content.ContentValues;
    import android.content.DialogInterface;
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
    import android.support.v4.view.ViewPager;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.graphics.Palette;
    import android.support.v7.view.ActionMode;
    import android.support.v7.widget.CardView;
    import android.support.v7.widget.LinearLayoutManager;
    import android.support.v7.widget.RecyclerView;
    import android.support.v7.widget.Toolbar;
    import android.support.v7.widget.helper.ItemTouchHelper;
    import android.transition.Transition;
    import android.transition.TransitionInflater;
    import android.util.DisplayMetrics;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewAnimationUtils;
    import android.view.ViewGroup;
    import android.view.animation.AccelerateInterpolator;
    import android.widget.FrameLayout;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.RelativeLayout;
    import android.widget.SeekBar;
    import android.widget.TextView;

    import com.androidplay.one.myplayer.ApplicationController;
    import com.androidplay.one.myplayer.DataFetch;
    import com.androidplay.one.myplayer.SliderImagePagerAdapter;
    import com.androidplay.one.myplayer.R;
    import com.androidplay.one.myplayer.helper_classes.SliderHelper;
    import com.androidplay.one.myplayer.interfaces.RecyclerClick_Listener;
    import com.androidplay.one.myplayer.helper_classes.RecyclerTouchListener;
    import com.androidplay.one.myplayer.helper_classes.ThemeHelper;
    import com.androidplay.one.myplayer.helper_classes.Toolbar_ActionMode_Callback;
    import com.androidplay.one.myplayer.recycler_adapter;
    import com.androidplay.one.myplayer.songs;
    import com.sothree.slidinguppanel.SlidingUpPanelLayout;
    import com.squareup.picasso.Picasso;
    import com.transitionseverywhere.TransitionManager;

    import java.util.ArrayList;
    import java.util.Random;

    import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class open_playlist extends AppCompatActivity implements recycler_adapter.playlist_data,View.OnClickListener,
        Toolbar_ActionMode_Callback.open_playlist_interface {

    RecyclerView rec_view;
    RecyclerView.LayoutManager mlayoutmanager;
    recycler_adapter adapter;
    ContentResolver resolver=null;
    ArrayList<songs> list;
    public Long playlist_id,current_id;
    ApplicationController con;
    String tag="tstngss";
    String tag1="tstngsss";
    Palette.Swatch swatch;
    Palette.Swatch swatchaccent;
    String method,album_art=null;
    Long album_id;
    boolean listset=false;
    boolean havemultipleimages=false;
    Toolbar toolbar,desc_toolbar;
    String title="";
    ImageView image,over_image,image1,image2,image3,image4;
    TextView over_title,numberofsongs;
    Boolean playall=false;
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
    int current_image_no=1;
    CardView method_card;
    TextView methodText;
    RelativeLayout.LayoutParams params;
    ItemTouchHelper ith;
    ImageView main_backgroundimage;

    //slidinglayout
    ViewPager sliderviewpager;
    SliderImagePagerAdapter sliderImagePagerAdapter;
    Toolbar card;
    LinearLayout slidercontrolcolour;
    boolean canrun=false;
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total,songname,artistname;
    SeekBar seekBar;
    ImageButton button;
    ImageView imageslide,imagebar;
    boolean isplaying=false;
    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;
    Random rand=new Random();
    LinearLayout multipleimages;
    delayedimagchange del;

    SliderHelper sliderHelper;

    //actionmode
    boolean canremoveSelection=false;
    public ActionMode mActionMode;
    RecyclerTouchListener recyclerTouchListener;


    SharedPreferences sharedPref;
    String theme_no;
    Boolean dark;
    ThemeHelper themeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            Log.i("activitystate","oncreate");
        con=new ApplicationController(this.getApplicationContext(),this);
        list=new ArrayList<>();
        albumartlist=new ArrayList<>();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        theme_no=sharedPref.getString("THEME_LIST","1") ;
        dark = sharedPref.getBoolean("check", false);
        themeHelper=new ThemeHelper(this);
        themeHelper.setthemecolours();

        if(!handlepermissions(savedInstanceState)){
            return;
        };

        doasync inback=new doasync();
        inititalise();
        themeHelper.setthemeAndBackground(main_backgroundimage,null);

        multipleimages.setVisibility(View.INVISIBLE);
        try{
            method=getIntent().getStringExtra("method");
            if(method.equals("playlist")){
                methodText.setText("PLAYLIST");
                isplaylist=true;
                playlist_id=getIntent().getLongExtra("playlist_id",0);
                title=getIntent().getStringExtra("playlist_name");
                inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,1);
                //get_playlist();
                //to animate sliding of the activity
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                current_id=playlist_id;
            }else if(method.equals("album")) {
                methodText.setText("ALBUM");

                album_art=getIntent().getStringExtra("album_art");
                title=getIntent().getStringExtra("album_name");
                playall=getIntent().getBooleanExtra("album_playall",false);
                album_id=getIntent().getLongExtra("album_id",0);
                current_id=album_id;
                inback.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,0);
                //getalbum();
            }
        }catch (Exception e){}

        over_title.setText(title);
        refreshNoOfSongs();

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
        framelayout.setBackgroundColor(con.getPrimary());
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        fab1.setOnClickListener(this);

        handle_enter_transition();
        addListener_to_appbar();

        //set height of appbarlayout=width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = displaymetrics.heightPixels;
        ViewGroup.LayoutParams params = appBarLayout.getLayoutParams();
        Log.i("hgt",String.valueOf(width));
        int ans=Math.min((3*height)/4,width);
        params.height = ans;
        appBarLayout.setLayoutParams(params);

        if(isplaylist){

            ItemTouchHelper.SimpleCallback _ithCallback=new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,ItemTouchHelper.LEFT ){
                //and in your imlpementaion of

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    // get the viewHolder's and target's positions in your adapter data, swap them
                    int from=viewHolder.getAdapterPosition();
                    int to=target.getAdapterPosition();
                    int last=list.get(to).getSortorder();
                    int start=list.get(from).getSortorder();
                    Log.i("sorder","from="+from+" to= "+to+" order oflast="+last+" start="+start);
                    ContentValues values ;

                    values = new ContentValues();
                    values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,start);
                    Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",playlist_id);

                    int i=resolver.update(uri, values,MediaStore.Audio.Playlists.Members.AUDIO_ID+" = ? ",new String[]{String.valueOf(list.get(to).getId())});
                    Log.i("sorder",to+" set "+start);
                    if(i==1){
                        list.get(to).setSortorder(start);
                    }
                    values = new ContentValues();
                    values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,last);
                    uri = MediaStore.Audio.Playlists.Members.getContentUri("external",playlist_id);
                    Log.i("sorder",from+" set "+last);

                    i=resolver.update(uri, values,MediaStore.Audio.Playlists.Members.AUDIO_ID+" = ? ",new String[]{String.valueOf(list.get(from).getId())});
                    if(i==1){
                        list.get(from).setSortorder(last);
                    }
                    resolver.notifyChange(Uri.parse("content://media"), null);

                    songs song=list.get(from);
                    list.set(from,list.get(to));
                    list.set(to,song);
                    adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());


                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                }

                @Override
                public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    return 0;
                }
            };
            ith = new ItemTouchHelper(_ithCallback);
            ith.attachToRecyclerView(rec_view);

        }

        //slider viewpager setup
        sliderImagePagerAdapter=new SliderImagePagerAdapter(getSupportFragmentManager());
        sliderviewpager.setAdapter(sliderImagePagerAdapter);
        //sliderviewpager.addOnPageChangeListener(pageChangeListener);

        sliderHelper=new SliderHelper(this,sliderviewpager,sliderImagePagerAdapter,slider);
        sliderHelper.setImagebuttons(play_pause,repeat,shuffle,button,previous,next);
        sliderHelper.setImageViews(imageslide,imagebar);
        sliderHelper.setTextViews(current,total,songname,artistname);
        sliderHelper.setRest(slidercontrolcolour,seekBar);
        sliderHelper.set_card_visibility();

        sliderHelper.set_card_visibility();
        card.setContentInsetsAbsolute(0, 0);
        connectControllerToSession(con.getMediaSessionToken());

    }


    //on create functions
    public void inititalise(){
        setContentView(R.layout.activity_open_playlist);

        rec_view=(RecyclerView)findViewById(R.id.rec_view2);
        mlayoutmanager=new LinearLayoutManager(this);

        toolbar=(Toolbar)findViewById(R.id.MyToolbar);
        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
        appBarLayout=(AppBarLayout)findViewById(R.id.MyAppbar);
        image=(ImageView)findViewById(R.id.album_image);
        image1=(ImageView)findViewById(R.id.album_image1);
        image2=(ImageView)findViewById(R.id.album_image2);
        image3=(ImageView)findViewById(R.id.album_image3);
        image4=(ImageView)findViewById(R.id.album_image4);
        desc_toolbar=(Toolbar)findViewById(R.id.desc_bar);
        over_image=(ImageView)findViewById(R.id.over_image);
        over_title=(TextView)findViewById(R.id.overtitle);
        //over_artist=(TextView)findViewById(R.id.over_artist);
        numberofsongs=(TextView)findViewById(R.id.numberofsongs);
        fab1=(FloatingActionButton)findViewById(R.id.fab1);
        image_foreground_colour=findViewById(R.id.image_foreground_colour);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        method_card=(CardView)findViewById(R.id.open_playlist_method_card);
        methodText=(TextView) findViewById(R.id.open_playlist_method);
        framelayout=(FrameLayout) findViewById(R.id.multiple_image_frame);
        appBarLayout= (AppBarLayout) findViewById(R.id.MyAppbar);
        multipleimages=(LinearLayout)findViewById(R.id.multiple_images);
        main_backgroundimage = (ImageView) findViewById(R.id.main_background_image);

        //slider
        sliderviewpager=(ViewPager)findViewById(R.id.sliderviewpager);
        card = (Toolbar) findViewById(R.id.controller_bar);
        slidercontrolcolour=(LinearLayout) findViewById(R.id.grad_bottom_slide);
        songname = (TextView) findViewById(R.id.bar_name);
        artistname = (TextView) findViewById(R.id.bar_artist);
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
        imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.setOnClickListener(this);
        button.setOnClickListener(this);

        resolver = getContentResolver();

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
    }
    public void handle_enter_transition(){

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Transition enter;
                try {
                    enter = TransitionInflater.from(this).inflateTransition(R.transition.fade_edited);
                } catch (Exception e) {
                    enter = TransitionInflater.from(this).inflateTransition(R.transition.fade_edited_1);
                }

                enter.setDuration(300);
                getWindow().setEnterTransition(enter);
                getWindow().setExitTransition(enter);


                getWindow().getSharedElementEnterTransition().setDuration(300);
                    getWindow().getSharedElementReturnTransition().setDuration(300)
                            .setInterpolator(new AccelerateInterpolator());


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
    public Boolean handlepermissions(Bundle savedInstanceState){
        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
        }else{
            if(con.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
                startActivity(new Intent(this, PermissionActivity.class));
                //finish called to stop further proccess of this activity
                finish();
                return false;
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
        return true;
    }

    public void refreshview(){
        Log.i("activitystate","refreshview ");

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

        image_foreground_colour.setVisibility(View.VISIBLE);
        refreshNoOfSongs();

        Log.i("lkll","pop1");
        if(album_art!=null){
            bitmap = BitmapFactory.decodeFile(album_art);
            if(bitmap!=null){
                modeSingleImage(bitmap);
            }
        }else{
            modenoImage();

        }
    }

    public void modenoImage(){
        image_foreground_colour.setVisibility(View.VISIBLE);
        multipleimages.setVisibility(View.INVISIBLE);
        image.setVisibility(View.VISIBLE);
        over_image.setVisibility(View.VISIBLE);
        //desc_toolbar.setBackgroundColor(0xff999999);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0xff333333);
        }
        image_foreground_colour.setVisibility(View.INVISIBLE);
        toolbar.setBackgroundColor(0x00ffffff);
        collapsingToolbarLayout.setContentScrimColor(0xff999999);
        fab1.setBackgroundTintList(ColorStateList.valueOf(con.getAccent()));
    }

    public void modeSingleImage(Bitmap bmp){
        image_foreground_colour.setVisibility(View.VISIBLE);
        toolbar.setBackground(ContextCompat.getDrawable(this,R.drawable.grad_rev));
        multipleimages.setVisibility(View.INVISIBLE);
        over_image.setVisibility(View.VISIBLE);
        over_image.setImageBitmap(DataFetch.decodeSampledBitmapFrompath(getResources(),album_art,360,360));
        image.setVisibility(View.VISIBLE);
        //image.setImageBitmap(bmp);
        setPalettecolour(bmp);
        Log.i("ertr","width="+image.getWidth()+" height"+image.getHeight()+"");
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int sw=displaymetrics.widthPixels;
        int sh=displaymetrics.heightPixels;
        int finalw=image.getWidth();
        int finalh=image.getHeight();

        if(image.getHeight()==0||image.getWidth()==0){
            finalh=sh;
            finalw=sw;
        }

        Picasso.with(this)
                .load(Uri.parse("file://"+album_art))
                .resize(finalw, finalh)
                .centerCrop()
                .into(image);

    }

    public void setPalettecolour(Bitmap bitmap){
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(con.getPrimaryDark());
                        }
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

    public void refreshNoOfSongs() {
            String string = String.valueOf(list.size());
        if(list.size()==1){
            string=string.concat(" song");
        }else{
            string=string.concat(" songs");
        }
            numberofsongs.setText(string);
    }
    public void getalbum(){
    String selection = "is_music != 0";

    if (album_id > 0) {
        selection = selection + " and album_id = " + album_id;
    }

    String[] projection = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR
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
                String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String thisdata=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String year=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
                songs song =new songs(id,name,artist,"",album_id,thisdata);
                song.setYear(year);
                song.setAlbumName(title);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshNoOfSongs();
            }
        });
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
                MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.ALBUM_ID,
                MediaStore.Audio.Playlists.Members.DATA,

        };
        Cursor tracks = resolver.query(uri,projection, null, null,MediaStore.Audio.Playlists.Members.PLAY_ORDER);Log.i(tag,"--");
            if(tracks!=null){
                Log.i(tag,"not null");
                while(tracks.moveToNext()) {
                    Log.i(tag,"ccc");
                    String name =tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                    Long id=Long.parseLong(tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
                    String artist=tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                    String playorder=tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER));
                    Long albumid=tracks.getLong(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID));
                    String thisdata=tracks.getString(tracks.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA));
                    song =new songs(id,name,artist,"",albumid,thisdata);
                    song.setSortorder(Integer.valueOf(playorder));
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
            final int ss=i;
            current=list.get(i);
            try {
                //cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, "_ID=" + current.getAlbum_id(),
                  //      null, null);
                cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, _id +"=" + current.getAlbum_id(),
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
                           //-----
                           current.setImagepath(path);
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   adapter.notifyItemChanged(ss);
                               }
                           });
                           //return;
                       }
                    }
                    cursor.close();
                }
            }catch (Exception e){Log.i("aaaa","fffff");}

        }
        Log.i("lkll","setalbumart end");
    }

    public void setnewMainImage(){
        /*String path=albumartlist.get(getnextImageNo());
        current_image_no++;
        Bitmap bmp = BitmapFactory.decodeFile(path);
        if(bmp!=null){
            over_image.setImageBitmap(bmp);
            //s
            etPalettecolour(bmp);
        }
        */
    }
    @Override
    public void onBackPressed() {

        if(slider.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED){
            slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab1.animate().scaleX(0f).scaleY(0f).setDuration(50).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        //getWindow().setExitTransition(null);
                        supportFinishAfterTransition();
                        open_playlist.super.onBackPressed();
                    }
                }).start();
            }else{
                super.onBackPressed();
                finish();
            }

        }

    }

    @Override
    protected void onStop() {
    super.onStop();
    listset=false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isplaylist) {
            getMenuInflater().inflate(R.menu.open_playlist_main, menu);
        }
        return super.onCreateOptionsMenu(menu);
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
        }case R.id.open_playlist_edit:{
            if(isplaylist){
                mActionMode = this.startSupportActionMode(
                        new Toolbar_ActionMode_Callback(this,"open_playlist"));

                adapter.mActionmodeset(true);
                implementRecyclerViewListeners();
            }
            return true;
        }
        default:return super.onOptionsItemSelected(item);
    }
    }


    @Override
    public Long getplaylist_id() {
    return playlist_id;
    }

    public void play_all_songs(){
    con.setMylist(list,"open_playlist",false);
    con.playsong(0);
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

    public void make_multiple_images(){
        havemultipleimages=true;
        //--
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            try {
                Log.i("reveala","try");
                // get the center for the clipping circle
                int cx = multipleimages.getWidth() / 8;
                int cy = (3*multipleimages.getHeight() )/ 4;

                // get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(7*cx, cy);

                // create the animation (the final radius is zero)
                Animator anim = ViewAnimationUtils.createCircularReveal(multipleimages, cx, cy, 0,initialRadius);
                anim.setDuration(600);
                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        image.setVisibility(View.INVISIBLE);

                    }
                });

            // start the animation
                over_image.setVisibility(View.INVISIBLE);
                multipleimages.setVisibility(View.VISIBLE);
                image_foreground_colour.setVisibility(View.INVISIBLE);
                anim.start();
            }catch (Exception e){
                Log.i("reveala","exception");

                over_image.setVisibility(View.INVISIBLE);
                multipleimages.setVisibility(View.VISIBLE);
            }
        }else{
            Log.i("reveala","lower api");


            over_image.setVisibility(View.INVISIBLE);
            TransitionManager.beginDelayedTransition(collapsingToolbarLayout);
            multipleimages.setVisibility(View.VISIBLE);
        }
        image.setVisibility(View.INVISIBLE);

            //--
        changeImages(1);
        changeImages(2);
        changeImages(3);
        changeImages(4);

        canrun=true;
        del=new delayedimagchange();
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
        case 5:{
            setnewMainImage();
            break;
        }

    }




    }

    public int getnextImageNo(){
        int size=albumartlist.size();
        return rand.nextInt(size-1);
      /*  if(current_image_no<albumartlist.size()){
            return current_image_no;
        }else{
            current_image_no=0;
            return 0;
        }
        */
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
        con.activityOnResume();

        if(havemultipleimages && !canrun){
            canrun=true;
            del=new delayedimagchange();
            del.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        sliderHelper.setrepeatbutton(false);
        sliderHelper.setshufflebutton(false);
        controllerCompat.registerCallback(callback);
        currentPlaybackstate=controllerCompat.getPlaybackState();
        currentmetadata=controllerCompat.getMetadata();
        sliderHelper.set_card_visibility();

        try {
            if (con.isPlaying()) {
                sliderHelper.setcard(true, con.getsong());
            } else {
                songs song = con.getsong();
                if (song != null) {
                    sliderHelper.setcard(false, song);
                } else {
                    sliderHelper.setcard(false, null);
                }
            }
        } catch (Exception e) {}

        sliderHelper.startseekbarasync();
        isplaying=con.isPlaying();
        sliderHelper.seticon(false);

    }
    @Override
    protected void onPause() {
        if(isplaylist) {
            overridePendingTransition(R.anim.slide_in_fromleft, R.anim.slide_out_toright);
        }
        canrun=false;
        super.onPause();

        sliderHelper.cancelSeekbarAsync();
        controllerCompat.unregisterCallback(callback);


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
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab1: {
                Log.i("huty","fab1");
                    con.setShuffle(true);
                    //play all songs
                    con.setMylist(list,"open_playlist",false);
                    con.playnext();
                    sliderHelper.setshufflebutton(false);
                return;

            }
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
                sliderHelper.repeatButtonClicked();
                return;
            }
            case R.id.shuffle :{
                Log.i("llll","shuffle");
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
        sliderHelper.refreshPanel();
        adapter.notifyDataSetChanged();

    }
    public void setstate(PlaybackStateCompat stateCompat){
        if(stateCompat!=null) {
            Log.i("mjkl", "setstate state is not null");
            switch (stateCompat.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    Log.i("mjkl", "set state playing state ");
                    sliderHelper.seticon(true);
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.i("mjkl", "set state paused state ");
                    sliderHelper.seticon(true);
                    break;
                }
                default:
            }
        }else{
            Log.i("mjkl", "setstate state is null");
            sliderHelper.seticon(false);
        }
            adapter.notifyDataSetChanged();
    }



    //actionmode functions
    public void implementRecyclerViewListeners(){
        recyclerTouchListener=new RecyclerTouchListener(this, rec_view, new RecyclerClick_Listener() {
            @Override
            public void onClick(View view, int position) {
                //Log.i("contxt","home recycler listener on single tap");
                //If ActionMode not null select item
                Log.i("clickedd","home on click");

                if (mActionMode != null)
                    onListItemSelect(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                //Log.i("contxt","home recycler listener on long tap");
                Log.i("clickedd","home on long click");

                //Select item on long click
                onListItemSelect(position);
            }
        });

        rec_view.addOnItemTouchListener(recyclerTouchListener);
    }
    //List item select method
    private void onListItemSelect(int position) {
        adapter.toggleSelection(position);//Toggle the selection

        boolean hasCheckedItems = adapter.getSelectedCount() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null){
            // there are some selected items, start the actionMode
            mActionMode = this.startSupportActionMode(
                    new Toolbar_ActionMode_Callback(this,"open_playlist"));

            adapter.mActionmodeset(true);

            //to change status bar colour in action mode
       /* if (Build.VERSION.SDK_INT >= 21) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.rgb(69,90,100));
        }*/
        }else if (!hasCheckedItems && mActionMode != null)

            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(adapter.getSelectedCount()) + " selected");
    }
    //Set action mode null after use

    @Override
    public void setNullToActionMode() {
        if (mActionMode != null) {
            Log.i("animt","null to action mode");

            mActionMode = null;
            removeSelection();
            adapter.mActionmodeset(false);
       /* if (Build.VERSION.SDK_INT >= 21) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.rgb(38,50,56));
        }*/
            if(recyclerTouchListener!=null) {
                rec_view.removeOnItemTouchListener(recyclerTouchListener);
                recyclerTouchListener=null;
            }
        }
    }
    @Override
    public void removeSelection(){
        if(canremoveSelection){
            adapter.removeSelection();
        }
    }

    @Override
    public void addtoplaylist_contextual(){
        Log.i("contxt1","add to playlist");
        adapter.addtoplaylist_contextual();
    }

    @Override
    public void remove_from_playlist() {
        adapter.removeFromPlaylist_contextual();
    }

    @Override
    public void addtoqueue_contextual(){
        Log.i("contxt1","add to queue");
        adapter.addtoqueue_contextual();
    }
    @Override
    public void delete_contextual(){
        Log.i("contxt1","delete");
        AlertDialog.Builder builder;
        builder=new AlertDialog.Builder(this);
        builder.setMessage("are you sure you want to delete "+String.valueOf(adapter.getSelectedCount())+" selected songs");
        builder.setCancelable(false) ;
        builder.setPositiveButton(
                "yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        canremoveSelection=true;
                        adapter.delete_contextual();
                        removeSelection();
                    }
                });

        builder.setNegativeButton(
                "no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        canremoveSelection=true;
                        removeSelection();
                        if(mActionMode!=null) {
                            mActionMode.finish();
                        }
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void share_contextual(){
        adapter.share_contextual();
    }


    @Override
    public void canremoveSelection(boolean g) {
        canremoveSelection=g;
    }
}



