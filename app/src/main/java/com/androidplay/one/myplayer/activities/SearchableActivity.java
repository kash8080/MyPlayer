package com.androidplay.one.myplayer.activities;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SectionIndexer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.MySuggestionProvider;
import com.androidplay.one.myplayer.SliderImagePagerAdapter;
import com.androidplay.one.myplayer.fast_rec_view.AllCharacterDetails;
import com.androidplay.one.myplayer.fast_rec_view.char_group;
import com.androidplay.one.myplayer.helper_classes.OnSwipeTouchListener;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.helper_classes.SliderHelper;
import com.androidplay.one.myplayer.helper_classes.ThemeHelper;
import com.androidplay.one.myplayer.ringdroid_classes.RingdroidEditActivity;
import com.androidplay.one.myplayer.songs;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class SearchableActivity extends AppCompatActivity implements View.OnClickListener{

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
    SectionTitleIndicator sectionTitleIndicator;

    //slidinglayout
    ViewPager sliderviewpager;
    SliderImagePagerAdapter sliderImagePagerAdapter;
    Toolbar card;
    LinearLayout slidercontrolcolour;
    boolean canrun=true;
    SlidingUpPanelLayout slider;
    ImageButton previous,next,play_pause,repeat,shuffle;
    TextView current,total,songname,artistnamebar;
    SeekBar seekBar;
    ImageButton button;
    ImageView imageslide,imagebar;
    public boolean isshuffle;
    int isrepeat;
    boolean isplaying=false;
    MediaControllerCompat controllerCompat;
    PlaybackStateCompat currentPlaybackstate ;
    MediaMetadataCompat currentmetadata ;

    SharedPreferences sharedPref;
    String theme_no;
    Boolean dark;
    ThemeHelper themeHelper;
    SliderHelper sliderHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        con=new ApplicationController(this.getApplicationContext(),this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        theme_no=sharedPref.getString("THEME_LIST","1") ;
        dark = sharedPref.getBoolean("check", false);
        themeHelper=new ThemeHelper(this);
        themeHelper.setthemecolours();

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
        themeHelper.setthemeAndBackground(main_backgroundimage,toolbar);

        handleIntent(getIntent());

        toolbar.setBackgroundColor(con.getPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(String.valueOf(search_result_list.size())+" songs found");
        refreshview();

        //slider viewpager setup
        sliderImagePagerAdapter=new SliderImagePagerAdapter(getSupportFragmentManager());
        sliderviewpager.setAdapter(sliderImagePagerAdapter);
        //sliderviewpager.addOnPageChangeListener(pageChangeListener);

        sliderHelper=new SliderHelper(this,sliderviewpager,sliderImagePagerAdapter,slider);
        sliderHelper.setImagebuttons(play_pause,repeat,shuffle,button,previous,next);
        sliderHelper.setImageViews(imageslide,imagebar);
        sliderHelper.setTextViews(current,total,songname,artistnamebar);
        sliderHelper.setRest(slidercontrolcolour,seekBar);
        sliderHelper.set_card_visibility();

        sliderHelper.set_card_visibility();
        card.setContentInsetsAbsolute(0, 0);
        connectControllerToSession(con.getMediaSessionToken());

    }

    public void initialise(){
        toolbar=(Toolbar)findViewById(R.id.now_toolbar);
        fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller3);
        rec_view=(RecyclerView)findViewById(R.id.now_recview);
        main_backgroundimage = (ImageView) findViewById(R.id.main_background_image);
        mainframe=(FrameLayout)findViewById(R.id.Main_frame);
        sectionTitleIndicator =(SectionTitleIndicator)
                findViewById(R.id.fast_scroller_section_title_indicator);

        //slider
        sliderviewpager=(ViewPager)findViewById(R.id.sliderviewpager);
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
        imageslide.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.setOnClickListener(this);
        button.setOnClickListener(this);

        builder=new AlertDialog.Builder(this);
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


    public void refreshview(){
        linearmanager=new LinearLayoutManager(this);
        adapter=new Recycleradapter();

        rec_view.setLayoutManager(linearmanager);
        rec_view.setAdapter(adapter);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(rec_view);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        rec_view.addOnScrollListener(fastScroller.getOnScrollListener());
        // Connect the section indicator to the scroller
        fastScroller.setSectionIndicator(sectionTitleIndicator);

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

    public class Recycleradapter extends RecyclerView.Adapter<Recycleradapter.viewholder> implements SectionIndexer {

        AllCharacterDetails allCharacterDetails;
        @Override
        public Object[] getSections() {
            Log.i("sectionn","getSections=");
            return allCharacterDetails.getchargroup();
            //return new Object[0];
        }

        @Override
        public int getPositionForSection(int i) {
            Log.i("sectionn","getPositionForSection i="+i);
            return 0;
        }

        @Override
        public int getSectionForPosition(int i) {
            if(search_result_list==null || search_result_list.size()<1){
                return 0;
            }
            if(i>=search_result_list.size()){
                i=i-1;
            }
            String name=search_result_list.get(i).getName();
            char c=name.charAt(0);
            String ss=String.valueOf(c);
            ss=ss.toLowerCase();
            c=ss.charAt(0);
            char_group[] grp=allCharacterDetails.getchargroup();
            for(int j=0;j<27;j++){
                if((grp[j].getaChar())==c){
                    return j;
                }
            }
            //return 0;
            return 0;
        }
        public Recycleradapter() {
            Log.i("llllp","rec adapter size="+String.valueOf(search_result_list.size()));
            allCharacterDetails=new AllCharacterDetails();

        }

        @Override
        public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v=LayoutInflater.from(SearchableActivity.this).inflate(R.layout.custom_row,parent,false);
            viewholder vh=new viewholder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(viewholder holder, int position) {

            holder.imageView.setImageDrawable(ContextCompat.getDrawable(SearchableActivity.this,R.drawable.testalbum));
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
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
                            case R.id.psongs_setasringtone: {
                                Log.i("notiff","set as ringtone");
                                builder=new AlertDialog.Builder(SearchableActivity.this);
                                builder.setTitle(R.string.SettingAsRingtone)
                                        .setMessage(R.string.askingtocutorset)
                                        .setPositiveButton(R.string.alert_yes_button,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int whichButton) {
                                                        Intent intent = new Intent(SearchableActivity.this,RingdroidEditActivity.class);
                                                        intent.putExtra("was_get_content_intent", false);
                                                        intent.putExtra("data",Uri.parse(search_result_list.get(getLayoutPosition()).getData()).toString());
                                                        SearchableActivity.this.startActivity(intent);
                                                    }
                                                })
                                        .setNegativeButton(
                                                R.string.alert_no_button,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {

                                                        setAsRingtone(search_result_list.get(getLayoutPosition()));
                                                        dialog.dismiss();
                                                        Toast.makeText(SearchableActivity.this,R.string.default_ringtone_success_message,Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                        .setCancelable(true)
                                        .show();


                                return true;
                            }
                            case R.id.psongs_edit_info: {
                                Log.i("notiff","set as ringtone");
                                songs song=search_result_list.get(getLayoutPosition());
                                Intent intent=new Intent(SearchableActivity.this,Edit_Info_songs.class);

                                intent.putExtra("song_id",String.valueOf(song.getId()));
                                intent.putExtra("album_id",String.valueOf(song.getAlbum_id()));
                                if(song.getImagepath()!=null) {
                                    intent.putExtra("song_image", song.getImagepath());
                                }
                                intent.putExtra("song_title",song.getName());
                                intent.putExtra("song_artist",song.getArtist());
                                intent.putExtra("song_albumname",song.getAlbumName());
                                intent.putExtra("song_year",song.getYear());
                                intent.putExtra("song_pos",getLayoutPosition());

                                SearchableActivity.this.startActivity(intent);
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
            public void setAsRingtone(songs Curr_song){
                boolean permission;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permission = Settings.System.canWrite(SearchableActivity.this);
                } else {
                    permission = ContextCompat.checkSelfPermission(SearchableActivity.this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
                }
                if (permission) {
                    //do your code

                    Log.i("notiff","has permission");
                    songs song=Curr_song;
                    String path=song.getData();

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Audio.Media.IS_RINGTONE,true);

                    int i=SearchableActivity.this.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values,
                            MediaStore.Audio.Media._ID+" = ? ",new String[]{String.valueOf(song.getId())});
                    Uri newUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
                    Log.i("ringg","updated="+i);
                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(
                                SearchableActivity.this, RingtoneManager.TYPE_RINGTONE,
                                newUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("ringg","exception="+e.toString());

                    }

                }else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + SearchableActivity.this.getPackageName()));
                        ((Activity)SearchableActivity.this).startActivityForResult(intent,12);
                    } else {
                        ActivityCompat.requestPermissions(((Activity)SearchableActivity.this), new String[]{Manifest.permission.WRITE_SETTINGS}, 12);
                    }
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
        con.activityOnResume();

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
        sliderHelper.refreshPanel();
        sliderHelper.seticon(false);
    }
    @Override
    protected void onPause() {
        super.onPause();
        canrun=false;
        super.onPause();

        sliderHelper.cancelSeekbarAsync();
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
                isrepeat=con.isRepeat();
                if(isrepeat==0){
                    isrepeat=1;
                    con.setRepeat(1);
                    sliderHelper.setrepeatbutton(true);
                }else if(isrepeat==1){
                    isrepeat=2;
                    con.setRepeat(2);
                    sliderHelper.setrepeatbutton(true);
                }else{
                    isrepeat=0;
                    con.setRepeat(0);
                    sliderHelper.setrepeatbutton(true);
                }
                return;
            }
            case R.id.shuffle :{
                Log.i("llll","shuffle");
                if(isshuffle){
                    isshuffle=false;
                    con.setShuffle(false);
                    sliderHelper.setshufflebutton(true);
                }else{
                    isshuffle=true;
                    con.setShuffle(true);
                    sliderHelper.setshufflebutton(true);
                }
                return;
            }
            default: return;

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



}
