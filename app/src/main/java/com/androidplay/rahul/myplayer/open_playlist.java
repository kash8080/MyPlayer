    package com.androidplay.rahul.myplayer;

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
    import android.widget.FrameLayout;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.RelativeLayout;
    import android.widget.SeekBar;
    import android.widget.TextView;

    import com.sothree.slidinguppanel.SlidingUpPanelLayout;
    import com.squareup.picasso.Callback;
    import com.squareup.picasso.Picasso;
    import com.transitionseverywhere.Crossfade;
    import com.transitionseverywhere.Fade;
    import com.transitionseverywhere.Recolor;
    import com.transitionseverywhere.TransitionManager;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Random;

    import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class open_playlist extends AppCompatActivity implements recycler_adapter.playlist_data,View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,SlidingUpPanelLayout.PanelSlideListener,Toolbar_ActionMode_Callback.open_playlist_interface{

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
    boolean previousplayingstate=false;
    int current_image_no=1;
    CardView method_card;
    TextView methodText;
    RelativeLayout.LayoutParams params;
    ItemTouchHelper ith;
    ImageView main_backgroundimage;

    //slidinglayout
    Toolbar card;
    LinearLayout slidercontrolcolour;
    String path;
    boolean canrun=false;
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total,songname,artistname;
    SeekBar seekBar;
    String Current_time;
    ImageButton button;
    ImageView imageslide,imagebar;
    updateseekbar1 seekbarasync;
    songs current_song;
    Long setmax=0L;
    public boolean isshuffle;
    int isrepeat;
    boolean isplaying=false;
    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;
    Random rand=new Random();
    LinearLayout multipleimages;
    delayedimagchange del;

    //actionmode
    boolean canremoveSelection=false;
    public ActionMode mActionMode;
    RecyclerTouchListener recyclerTouchListener;


    SharedPreferences sharedPref;
    String theme_no;
    Boolean dark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            Log.i("activitystate","oncreate");
        con=new ApplicationController(this.getApplicationContext(),this);
        list=new ArrayList<>();
        albumartlist=new ArrayList<>();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        theme_no=sharedPref.getString("THEME_LIST","1") ;
        dark = sharedPref.getBoolean("check", false);
        setthemecolours();

        if(!handlepermissions(savedInstanceState)){
            return;
        };

        doasync inback=new doasync();
        inititalise();
        setthemeAndBackground();

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

        //slider
        imageslide.setOnTouchListener(new OnSwipeTouchListener(this) {
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
        set_card_visibility();
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
        seekBar.setOnSeekBarChangeListener(this);
        imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.setOnClickListener(this);
        button.setOnClickListener(this);
        slider.addPanelSlideListener(this);

        resolver = getContentResolver();

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
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
                enter.setDuration(150);
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
    private void setthemecolours(){
        if(dark){
            setTheme(R.style.AppThemeDark_night);
        }else {
            switch (theme_no) {
                case "1":
                    setTheme(R.style.AppTheme);
                    break;
                case "2":
                    setTheme(R.style.AppTheme_Purple);
                    break;
                case "3":
                    setTheme(R.style.AppTheme_Red);
                    break;
                case "4":
                    setTheme(R.style.AppTheme_orange);
                    break;
                case "5":
                    setTheme(R.style.AppTheme_indigo);
                    break;
                case "6":
                    setTheme(R.style.AppTheme_brown);
                    break;
                default:
                    setTheme(R.style.AppTheme);
                    break;
            }
        }
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
    private void setthemeAndBackground() {
        Boolean dark = sharedPref.getBoolean("check", false);
        int img_no = sharedPref.getInt("image_chooser", 0);
        if (dark) {
            Log.i("settn", "dark");
            main_backgroundimage.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDarknight));
        } else {

            if (img_no >= 1) {
                Log.i("settn", "main act current value=" + img_no);
                DisplayMetrics displaymetrics = new DisplayMetrics();
                this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width=displaymetrics.widthPixels;
                int height=displaymetrics.heightPixels;
                switch (img_no) {
                    case 1: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            //set height of cardview=width
                            Picasso.with(this)
                                    .load(R.drawable.coffee_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        } else {

                            // create the animation (the final radius is zero)
                            main_backgroundimage.setVisibility(View.INVISIBLE);
                            Picasso.with(this)
                                    .load(R.drawable.coffee)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                    case 2: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            Picasso.with(this)
                                    .load(R.drawable.presents_land)
                                    .error(R.drawable.coffee_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        } else {
                            Picasso.with(this)
                                    .load(R.drawable.presents_port)
                                    .error(R.drawable.coffee)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                    case 3: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            Picasso.with(this)
                                    .load(R.drawable.leaves_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        } else {
                            Picasso.with(this)
                                    .load(R.drawable.leaves_port)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                    case 4: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            Picasso.with(this)
                                    .load(R.drawable.leaves2_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        } else {
                            Picasso.with(this)
                                    .load(R.drawable.leaves2_port)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                    case 5: {
                        if (getResources().getBoolean(R.bool.is_landscape)) {
                            Picasso.with(this)
                                    .load(R.drawable.bloom_land)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);

                        } else {
                            Picasso.with(this)
                                    .load(R.drawable.bloom_port)
                                    .resize(width, height)
                                    .centerCrop()
                                    .into(main_backgroundimage,picassocallback);
                        }
                        break;
                    }
                }
            } else {
                Log.i("settn", "not dark");

                main_backgroundimage.setBackground(null);
                main_backgroundimage.setBackgroundColor(0xffffffff);
            }
        }
    }
    public Callback picassocallback=new Callback() {
        @Override
        public void onSuccess() {
            Log.i("picss","onsuccess");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && main_backgroundimage.isAttachedToWindow()){
                int cx = main_backgroundimage.getWidth();
                int cy = (main_backgroundimage.getHeight() );

                // get the initial radius for the clipping circle
                float finalradius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(main_backgroundimage, 0, cy, 0,finalradius);
                anim.setDuration(400);
                // start the animation
                main_backgroundimage.setVisibility(View.VISIBLE);
                anim.start();
            }else{
                main_backgroundimage.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onError() {
            Log.i("picss","onerror");
            main_backgroundimage.setVisibility(View.VISIBLE);

        }
    };

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
        over_image.setImageBitmap(bmp);
        image.setVisibility(View.VISIBLE);
        image.setImageBitmap(bmp);
        setPalettecolour(bmp);
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
            MediaStore.Audio.Media.DATA
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
                songs song =new songs(id,name,artist,"",album_id,thisdata);
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
        if(method.equals("album") && playall){
            play_all_songs();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshfab();
                }
            });

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

    public void refreshfab() {
    if(current_id.equals(con.open_playlist_id)&&con.isPlaying()){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !previousplayingstate) {
            fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_play_to_pause_white));
            Drawable drawable = fab1.getDrawable();
            ((Animatable)drawable).start();
        }else{
            fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pause_white));
        }
        previousplayingstate=true;

    }else{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && previousplayingstate) {
            fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avd_pause_to_play_white));
            Drawable drawable = fab1.getDrawable();
            ((Animatable)drawable).start();
        }else{
            fab1.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.play_white));
        }
        previousplayingstate=false;

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

        if(havemultipleimages && !canrun){
            canrun=true;
            del=new delayedimagchange();
            del.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        refreshfab();

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
        seticon(false);

    }
    @Override
    protected void onPause() {
        if(isplaylist) {
            overridePendingTransition(R.anim.slide_in_fromleft, R.anim.slide_out_toright);
        }
        canrun=false;
        super.onPause();

        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
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

                //TransitionManager.beginDelayedTransition(fab1);
                if (current_id.equals(con.open_playlist_id) && con.isPlaying()) {
                    //pause the button
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avd_pause_to_play_white));
                    } else {
                        fab1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.play_white));
                    }
                    previousplayingstate = false;
                    con.pause();
                } else if (current_id.equals(con.open_playlist_id) && !con.isPlaying()) {
                    //pause the button
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause_white));
                    } else {
                        fab1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pause_white));
                    }
                    previousplayingstate = true;
                    con.resume();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause_white));
                    } else {
                        fab1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pause_white));
                    }

                    //play all songs
                    play_all_songs();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable drawable = fab1.getDrawable();
                    ((Animatable) drawable).start();
                }
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
                artistname.setText(artist);
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
        adapter.notifyDataSetChanged();

    }
    public void setstate(PlaybackStateCompat stateCompat){
        if(stateCompat!=null) {
            Log.i("mjkl", "setstate state is not null");
            switch (stateCompat.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    Log.i("mjkl", "set state playing state ");
                    seticon(true);
                    refreshfab();
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.i("mjkl", "set state paused state ");
                    seticon(true);
                    refreshfab();
                    break;
                }
                default:
            }
        }else{
            Log.i("mjkl", "setstate state is null");
            seticon(false);
            refreshfab();
        }
            adapter.notifyDataSetChanged();
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



