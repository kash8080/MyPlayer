package com.androidplay.rahul.myplayer;

import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Inflater;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class SearchableActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,SlidingUpPanelLayout.PanelSlideListener {

    List<songs> search_result_list;
    Toolbar toolbar ;
    RecyclerView rec_view;
    RecyclerView.LayoutManager linearmanager;
    Recycleradapter adapter;
    ApplicationController con;
    PopupMenu popup;
    AlertDialog.Builder builder;
    ImageView main_backgroundimage;
    FrameLayout mainframe;

    VerticalRecyclerViewFastScroller fastScroller;

    //slidinglayout
    Toolbar card;
    LinearLayout slidercontrolcolour;
    String path;
    boolean canrun=true;
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total,songname,artistnamebar;
    SeekBar seekBar;
    String Current_time;
    ImageButton button;
    Bitmap bitmap;
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

    SharedPreferences sharedPref;
    String theme_no;
    Boolean dark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        con=new ApplicationController(this.getApplicationContext(),this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        theme_no=sharedPref.getString("THEME_LIST","1") ;
        dark = sharedPref.getBoolean("check", false);
        setthemecolours();

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
                super.onCreate(savedInstanceState);
            }
        }

        setContentView(R.layout.activity_now_playing);
        initialise();
        setthemeAndBackground();

        handleIntent(getIntent());

        toolbar.setBackgroundColor(con.getPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(String.valueOf(search_result_list.size())+" songs found");
        refreshview();

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

    public void initialise(){
        toolbar=(Toolbar)findViewById(R.id.now_toolbar);
        fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller3);
        rec_view=(RecyclerView)findViewById(R.id.now_recview);
        main_backgroundimage = (ImageView) findViewById(R.id.main_background_image);
        mainframe=(FrameLayout)findViewById(R.id.Main_frame);

        //slider
        card = (Toolbar) findViewById(R.id.controller_bar);
        slidercontrolcolour=(LinearLayout) findViewById(R.id.grad_bottom_slide);
        songname = (TextView) findViewById(R.id.bar_name);
        artistnamebar = (TextView) findViewById(R.id.bar_artist);
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

        builder=new AlertDialog.Builder(this);
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

    private void setthemeAndBackground() {
        Boolean dark = sharedPref.getBoolean("check", false);
        int img_no = sharedPref.getInt("image_chooser", 0);
        if (dark) {
            Log.i("settn", "dark");
            main_backgroundimage.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDarknight));
            toolbar.setAlpha(1);
        } else {

            if (img_no >= 1) {
                Log.i("settn", "main act current value=" + img_no);
                DisplayMetrics displaymetrics = new DisplayMetrics();
                this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                mainframe.setBackgroundColor(ContextCompat.getColor(this,R.color.colorbackgroundgrey));

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
                //make toolbar and tablayout semitransparent for grey theme and opaque for others
                // if (theme_no.equals("1")) {
                toolbar.setAlpha(0.7f);
               /* } else {
                    appBarLayout.setAlpha(1);
                    tablayout.setAlpha(1);
                }*/
            } else {
                Log.i("settn", "not dark");

                main_backgroundimage.setBackground(null);
                main_backgroundimage.setBackgroundColor(0xffffffff);
                toolbar.setAlpha(1);
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
        linearmanager=new LinearLayoutManager(this);
        adapter=new Recycleradapter();

        rec_view.setLayoutManager(linearmanager);
        rec_view.setAdapter(adapter);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(rec_view);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        rec_view.addOnScrollListener(fastScroller.getOnScrollListener());

        if(search_result_list!=null && search_result_list.size()>15){
            fastScroller.setVisibility(View.VISIBLE);
        }else{
            fastScroller.setVisibility(View.INVISIBLE);
        }
        try{
            getSupportActionBar().setTitle(String.valueOf(search_result_list.size())+" songs found");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            fetchsimilarsonglist(query);

        }
    }

    public void fetchsimilarsonglist(String str){
        search_result_list=new ArrayList<>();
        Log.i("llllp","fetch list");
        String[] proj={android.provider.MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media._ID,
                android.provider.MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,

        };
        //using mediaplayer


        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection=" TITLE LIKE ?";
        String args[]=new String[]{"%"+str+"%"};
        Cursor musicCursor;
        try {
            Log.i("llllp","try");

            musicCursor = this.getContentResolver().query(musicUri, proj, selection, new String[]{"%"+str+"%"},null);
        }catch(Exception ee){
            ee.printStackTrace();
            return;
        }
        Log.i("llllp","done");

        if(musicCursor!=null && musicCursor.moveToFirst()){
            Log.i("llllp","done2");

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

                search_result_list.add(new songs(thisId, thisTitle, thisArtist,"",albumid,thisdata));

            }
            while (musicCursor.moveToNext());
        }
        try{
            Log.i("llllp","end size="+String.valueOf(search_result_list.size()));

            musicCursor.close();}catch (Exception e){e.printStackTrace();}

        refreshview();
    }

    public class Recycleradapter extends RecyclerView.Adapter<Recycleradapter.viewholder>{


        public Recycleradapter() {
            Log.i("llllp","rec adapter size="+String.valueOf(search_result_list.size()));

        }

        @Override
        public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v=LayoutInflater.from(SearchableActivity.this).inflate(R.layout.custom_row,parent,false);
            viewholder vh=new viewholder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(viewholder holder, int position) {

            holder.imageView.setImageDrawable(ContextCompat.getDrawable(SearchableActivity.this,R.drawable.mp3));
            holder.title.setText(search_result_list.get(position).getName());
            holder.artist.setText(search_result_list.get(position).getArtist());

            songs ss=con.getsong();
            try {
                if (ss != null) {
                    if (search_result_list.get(position).getId().equals(ss.getId())) {
                        if(con.isPlaying()) {
                            holder.giff.setVisibility(View.VISIBLE);
                            holder.equaliser.setVisibility(View.INVISIBLE);

                        }else{
                            holder.giff.setVisibility(View.INVISIBLE);
                            holder.equaliser.setVisibility(View.VISIBLE);
                        }
                    } else {
                        holder.giff.setVisibility(View.INVISIBLE);
                        holder.equaliser.setVisibility(View.INVISIBLE);
                    }
                } else {
                    holder.giff.setVisibility(View.INVISIBLE);
                    holder.equaliser.setVisibility(View.INVISIBLE);

                }
            }catch (Exception e){}

            SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(SearchableActivity.this);
            int img_no=sharedPreferences.getInt("image_chooser",0);
            Boolean night=sharedPreferences.getBoolean("check",false);
            Boolean dark=false;
            if(img_no>=1 || night){
                dark=true;
            }
            //setting dark theme or light theme based on setting
            if (dark) {
                holder.title.setTextColor(ContextCompat.getColor(SearchableActivity.this,R.color.colorPrimaryTextNight));
                holder.artist.setTextColor(ContextCompat.getColor(SearchableActivity.this,R.color.colorSecondaryTextNight));
                holder.options.setImageDrawable(ContextCompat.getDrawable(SearchableActivity.this, R.drawable.options_white));
            } else {
                holder.title.setTextColor(ContextCompat.getColor(SearchableActivity.this,R.color.colorPrimaryText));
                holder.artist.setTextColor(ContextCompat.getColor(SearchableActivity.this,R.color.colorSecondaryText));
                holder.options.setImageDrawable(ContextCompat.getDrawable(SearchableActivity.this, R.drawable.options));
            }
        }

        @Override
        public int getItemCount() {
            return search_result_list.size();
        }

        public class viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{

            ImageView imageView;
            TextView title;
            TextView artist;
            ImageView options,equaliser;
            pl.droidsonroids.gif.GifTextView giff;
            ContentResolver resolver=SearchableActivity.this.getContentResolver();
            public viewholder(View itemView) {
                super(itemView);
                title=(TextView)itemView.findViewById(R.id.songs_name);
                artist=(TextView)itemView.findViewById(R.id.songs_artist);
                imageView=(ImageView)itemView.findViewById(R.id.songs_image);
                options=(ImageView)itemView.findViewById(R.id.options);
                options.setOnClickListener(this);
                itemView.setOnClickListener(this);
                equaliser=(ImageView)itemView.findViewById(R.id.giff_pause);

                giff=(pl.droidsonroids.gif.GifTextView)itemView.findViewById(R.id.giff_id);
            }

            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case (R.id.options): {
                        handleOptions(view);
                        break;
                    }
                    default:{
                        playsong();
                    }
                }

            }
            public void handleOptions(final View v){
                popup=new PopupMenu(SearchableActivity.this,v);
                popup.getMenuInflater().inflate(R.menu.songs_options,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int ids=item.getItemId();
                        switch (ids){
                            case R.id.psongs_play :{
                                playsong();
                                return true;
                            }
                            case R.id.psongs_add_to_playlist: {
                                PopupMenu popup1=new PopupMenu(SearchableActivity.this,v);
                                /// getplaylist to populate popupmenu
                                Log.i("popo","addtoplaylist");
                                final ArrayList<songs> list;
                                list=get_playlist();
                                for(songs song :list ){
                                    popup1.getMenu().add(song.getName());
                                    Log.i("popo",song.getName());
                                }
                                popup1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    public boolean onMenuItemClick(MenuItem item) {
                                        for(songs playlist : list){
                                            if(playlist.getName().equals(item.getTitle())) {
                                                if (playlist.getId().equals(0L)) {
                                                    // add new playlist and add song to tht playlist
                                                    addnewPlaylistwithSongs(search_result_list.get(getLayoutPosition()));
                                                } else {
                                                    addTracksToPlaylist(playlist.getId(), search_result_list.get(getLayoutPosition()));
                                                    Toast.makeText(SearchableActivity.this, "added " + search_result_list.get(getLayoutPosition()).getName() + " to " + playlist.getName(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                        return true;
                                    }
                                });
                                popup1.show();
                                return true;
                            }
                            case R.id.psongs_delete: {
                                builder=new AlertDialog.Builder(SearchableActivity.this);
                                builder.setMessage("are you sure you want to delete "+search_result_list.get(getLayoutPosition()).getName());
                                builder.setCancelable(true) ;
                                builder.setPositiveButton(
                                        "yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                deletesong(search_result_list.get(getLayoutPosition()).getId());

                                            }
                                        });

                                builder.setNegativeButton(
                                        "no",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alert = builder.create();
                                alert.show();
                                return true;
                            }
                            case R.id.psongs_Rename: {
                                renameSong();
                                return true;
                            }
                            case R.id.psongs_moreFromArtist: {
                                open_artist(false,search_result_list.get(getLayoutPosition()).getArtist());
                                return true;
                            }
                            case R.id.psongs_OpenAlbum: {
                                openAlbumforSong();
                                return true;
                            }
                            case R.id.psongs_playnext: {
                                con.addSongtoNextPos(search_result_list.get(getLayoutPosition()));
                                return true;
                            }
                            case R.id.psongs_addtoqueue: {
                                add_to_queue(search_result_list.get(getLayoutPosition()));
                                return true;
                            }
                            case R.id.psongs_share: {
                                shareSong(search_result_list.get(getLayoutPosition()).getData());
                                return true;
                            }
                        }
                        return true;
                    }
                });
                popup.show();


            }
            public void playsong(){
                Long id=search_result_list.get(getLayoutPosition()).getId();
                songs song=ApplicationController.getSongById(id);
                if(song.getId().equals(0L)){
                    Toast.makeText(SearchableActivity.this,"Error!",Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    con.pause();
                    ArrayList<songs> list = new ArrayList<>();
                    list.add(song);
                    ApplicationController.setMylist(list, "", false);
                    ApplicationController.playsong(0);
                    startActivity(new Intent(SearchableActivity.this, playerr.class));

                }
            }
            public ArrayList<songs> get_playlist(){
                ArrayList<songs> playlist_list=new ArrayList<>();
                playlist_list.add(new songs(0L,"Add New Playlist",""));
                final ContentResolver resolver = SearchableActivity.this.getContentResolver();
                final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
                final String idKey = MediaStore.Audio.Playlists._ID;
                final String nameKey = MediaStore.Audio.Playlists.NAME;
                final String songs= MediaStore.Audio.Playlists._COUNT;


                final String[] columns = { idKey, nameKey };
                final Cursor playLists = resolver.query(uri, columns, null, null, null);
                if (playLists == null) {

                }else {
                    // Log a list of the playlists.
                    String playListName = null;
                    String playlist_id = null;

                    for (boolean hasItem = playLists.moveToFirst(); hasItem; hasItem = playLists.moveToNext()) {
                        playListName = playLists.getString(playLists.getColumnIndex(nameKey));
                        playlist_id = playLists.getString(playLists.getColumnIndex(idKey));
                        songs playlist =new songs(Long.parseLong(playlist_id),playListName,"");
                        playlist_list.add(playlist);
                    }
                }
                // Close the cursor.
                if (playLists != null) {
                    try{ playLists.close();}catch (Exception e){e.printStackTrace();}
                }
                return playlist_list;

            }
            public  String addTracksToPlaylist(final long id,songs track) {
                int count = getplaylistsize(id);
                ContentValues values ;

                values = new ContentValues();
                values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, count + 1);
                values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, track.getId());

                Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);

                resolver.insert(uri, values);
                resolver.notifyChange(Uri.parse("content://media"), null);
                return "";
            }
            public int getplaylistsize(Long ids){

                int i=0;
                final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", ids);
                final String idd=MediaStore.Audio.Playlists.Members._ID;
                Cursor tracks = resolver.query(uri,new String[]{idd}, null, null, null);
                if (tracks != null) {

                    while(tracks.moveToNext()){
                        i++;
                    }
                }
                try{
                    tracks.close();
                }catch (Exception e){e.printStackTrace();}
                return i;
            }
            public void deletesong(Long audioid){
                int i = 0;

                try {
                    String where = MediaStore.Audio.Playlists.Members._ID + "=?";
                    String sid = String.valueOf(audioid);
                    String[] whereVal = {sid};
                    String paths=search_result_list.get(getLayoutPosition()).getData();

                    i = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            where, whereVal);
                    Log.i("uiii",String.valueOf(i));
                    if(i!=0){
                        try{
                            File file=new File(paths);
                            file.delete();
                        }catch (Exception e){
                            Log.e("dlt","error in deleting song from sd card");

                        }
                    }


                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("uiii",e.toString());
                }
                if(i>0){
                    Toast.makeText(SearchableActivity.this, "songs removed: " +search_result_list.get(getLayoutPosition()).getName(), Toast.LENGTH_SHORT).show();
                    search_result_list.remove(getLayoutPosition());
                    notifyItemRemoved(getLayoutPosition());
                }
            }
            public void add_to_queue(songs song){
                ArrayList<songs> listt =new ArrayList<>();
                listt.add(song);
                con.addSongToList(listt);
            }
            public void addnewPlaylistwithSongs(final songs s){
                builder=new AlertDialog.Builder(SearchableActivity.this);
                builder.setTitle("Playlist name");
                builder.setCancelable(true);
                final EditText input = new EditText(SearchableActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT );
                builder.setView(input);
                builder.setPositiveButton(
                        "Create",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                createnewplaylist(input.getText().toString());
                                addTracksToPlaylist(findPlaylistIdByName(input.getText().toString()),s);
                            }
                        });

                builder.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
            public Long findPlaylistIdByName(String name){

                Long id;
                final ContentResolver resolver = SearchableActivity.this.getContentResolver();
                final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
                final String idKey = MediaStore.Audio.Playlists._ID;
                final String nameKey = MediaStore.Audio.Playlists.NAME;

                final String[] columns = { idKey, nameKey };
                final Cursor playLists = resolver.query(uri, columns,nameKey +" = ?", new String[]{name}, null);
                if (playLists == null) {
                    return null;
                }else {
                    String playlist_id = null;

                    for (boolean hasItem = playLists.moveToFirst(); hasItem; hasItem = playLists.moveToNext()) {
                        playlist_id = playLists.getString(playLists.getColumnIndex(idKey));
                        return Long.valueOf(playlist_id);
                    }
                }
                return null;
            }
            public void createnewplaylist(String playlistname) {
                ContentValues mInserts = new ContentValues();
                mInserts.put(MediaStore.Audio.Playlists.NAME, playlistname);
                mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
                mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
                SearchableActivity.this.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
            }
            public void shareSong(String data){
                Uri uri= Uri.parse("file:///"+data);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/mp3");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(share, "Share Sound File"));
            }
            public void renameSong(){
                builder=new AlertDialog.Builder(SearchableActivity.this);
                builder.setTitle("Playlist name");
                builder.setCancelable(true);
                // Set up the input
                final EditText input = new EditText(SearchableActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT );
                String s=search_result_list.get(getLayoutPosition()).getName();
                input.setText(s);
                input.setSelection(0,s.length());
                builder.setView(input);
                builder.setPositiveButton(
                        "Rename",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String str=input.getText().toString();
                                int pos=getLayoutPosition();
                                Long idd=search_result_list.get(pos).getId();
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Audio.Media.TITLE,str);
                                search_result_list.get(pos).setName(str);
                                resolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        values, "_id=" + idd, null);
                                notifyItemChanged(pos);
                            }
                        });

                builder.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                try {
                    alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }catch (Exception E){
                    E.printStackTrace();
                }
                alert.show();
            }
            public void open_artist(Boolean playall,String artistname){

                final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                final String _id = MediaStore.Audio.Albums._ID;
                final String album_name = MediaStore.Audio.Albums.ALBUM;
                final String artist = MediaStore.Audio.Albums.ARTIST;
                final String albumart = MediaStore.Audio.Albums.ALBUM_ART;

                final String[] columns = { _id, album_name, artist, albumart};
                String selection=artist +" = ? ";
                String[] args=new String[]{artistname};
                Cursor cursor=null;
                try {
                    cursor = SearchableActivity.this.getContentResolver().query(uri, columns, selection,
                            args,null);
                }catch (Exception e){}
                if(cursor!=null) {
                    int size = cursor.getCount();
                    if (size == 0) {
                        Log.i("artistadaptr", "cursor==null or no album");
                        Intent intent2 = new Intent(SearchableActivity.this, Now_playing.class);
                        intent2.putExtra("artistname", artistname);
                        intent2.putExtra("method", "artistsongs");
                        try {
                            cursor.close();
                        }catch (Exception e ){
                            e.printStackTrace();
                        }
                        SearchableActivity.this.startActivity(intent2);
                    }else if(size==1){
                        if( cursor.moveToFirst()){
                            Log.i("artistadaptr","cursor!=null");
                            Intent intent = new Intent(SearchableActivity.this, open_playlist.class);

                            Long id=Long.parseLong(cursor.getString(cursor.getColumnIndex(_id)));
                            String name=cursor.getString(cursor.getColumnIndex(album_name));
                            String artistt=cursor.getString(cursor.getColumnIndex(artist));
                            String pic=cursor.getString(cursor.getColumnIndex(albumart));

                            intent.putExtra("method", "album");
                            intent.putExtra("album_art",pic);
                            intent.putExtra("album_name", name);
                            intent.putExtra("album_playall", playall);
                            intent.putExtra("album_id",id);////////////////////
                            try {
                                cursor.close();
                            }catch (Exception e ){
                                e.printStackTrace();
                            }
                            SearchableActivity.this.startActivity(intent);

                        }
                    }else{
                        Intent intent = new Intent(SearchableActivity.this, ChooseArtistAlbum.class);
                        intent.putExtra("artistname",artistname);
                        try {
                            cursor.close();
                        }catch (Exception e ){
                            e.printStackTrace();
                        }
                        SearchableActivity.this.startActivity(intent);
                    }
                }
            }
            public void openAlbumforSong(){
                final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                final String _id = MediaStore.Audio.Albums._ID;
                final String album_name = MediaStore.Audio.Albums.ALBUM;
                final String artist = MediaStore.Audio.Albums.ARTIST;
                final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
                final String tracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS;

                final String[] columns = { _id, album_name, artist, albumart, tracks };
                Cursor cursor=null;
                String Selection=_id+ " = ? ";
                String[] args=new String[]{String.valueOf(search_result_list.get(getLayoutPosition()).getAlbum_id())};
                try {
                    cursor = resolver.query(uri, columns, Selection, args, album_name);
                }catch (java.lang.SecurityException e){
                    e.printStackTrace();
                }
                if(cursor!=null && cursor.moveToFirst()){

                    Long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(_id)));
                    String name = cursor.getString(cursor.getColumnIndex(album_name));
                    String artistt = cursor.getString(cursor.getColumnIndex(artist));
                    String pic = cursor.getString(cursor.getColumnIndex(albumart));

                    Intent intent=new Intent(SearchableActivity.this,open_playlist.class);
                    intent.putExtra("method", "album");
                    intent.putExtra("album_art",pic);
                    intent.putExtra("album_name", name);
                    intent.putExtra("album_playall", false);
                    intent.putExtra("album_id", id);
                    try {
                        cursor.close();
                    }catch (Exception e ){
                        e.printStackTrace();
                    }
                    SearchableActivity.this.startActivity(intent);
                }else{
                    Toast.makeText(SearchableActivity.this,"No Album Found",Toast.LENGTH_SHORT).show();
                    try {
                        cursor.close();
                    }catch (Exception e ){
                        e.printStackTrace();
                    }
                }

            }


        }
    }


    @Override
    protected void onResume() {
        super.onResume();

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
        refreshPanel();
        seticon(false);
    }
    @Override
    protected void onPause() {
        super.onPause();
        canrun=false;
        super.onPause();

        seekbarasync.cancel(true);
        seekbarasync.canrun=false;
        controllerCompat.unregisterCallback(callback);

    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
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
                artistnamebar.setText(artist);
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
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.i("mjkl", "set state paused state ");
                    seticon(true);
                    break;
                }
                default:
            }
        }else{
            Log.i("mjkl", "setstate state is null");
            seticon(false);
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
    public class updateseekbar1 extends AsyncTask<Void,Void,Void> {

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


}
