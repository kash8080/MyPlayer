package com.androidplay.one.myplayer;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplay.one.myplayer.activities.ChooseArtistAlbum;
import com.androidplay.one.myplayer.activities.Edit_Info_songs;
import com.androidplay.one.myplayer.activities.MainActivity;
import com.androidplay.one.myplayer.activities.Now_playing;
import com.androidplay.one.myplayer.activities.open_playlist;
import com.androidplay.one.myplayer.fast_rec_view.AllCharacterDetails;
import com.androidplay.one.myplayer.fast_rec_view.char_group;
import com.androidplay.one.myplayer.ringdroid_classes.RingdroidEditActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul on 14-07-2016.
 */

public class recycler_adapter extends RecyclerView.Adapter<recycler_adapter.viewholder> implements SectionIndexer{
    public Context context;
    public ArrayList<songs> songs_list;
    private PopupMenu popup;
    ApplicationController con;
    String id;
    //adaptr face;
    ContentResolver resolver;
    AlertDialog.Builder builder;

    private String sallsongs="allsongs";
    private String splaylist="playlist";
    private String sartist="artist";
    private String salbum="album";
    private String sopen_playlist="open_playlist";
    private String snow_playing="now_playing";
    private String sopen_album="open_album";
    private String sfolders="folders";

    //for animation set
    int startpos=0;

    //for ActionMode
    private SparseBooleanArray mSelectedItemsIds;
    private boolean mActionModeSet =false;
    private songs currentPlayingSong;
    private boolean imagesset=false;
    private Long playlistIdForMultipleAdd;
    private DisplayMetrics displaymetrics;

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
        if(songs_list==null || songs_list.size()<1){
            return 0;
        }
        if(i>=songs_list.size()){
            i=i-1;
        }
        String name=songs_list.get(i).getName();
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

    /*
    public interface adaptr{
        public void setcardss(songs song);
    }
*/
    public interface playlist_data{
        public Long getplaylist_id();
    }
    public interface Folderfrag{
        public void getrootfolders();
        public void openFolder(String parent,String filename);
        public void openbackfolder();
    }
    //id for artist all songs and all songs are same so check it by variable to set current list
    private boolean artist_songs=false;
    private playlist_data plylst;
    private Folderfrag folderfrag;

    private SharedPreferences sharedPref;

    public recycler_adapter(Context context,ArrayList<songs> list,String id) {

        allCharacterDetails=new AllCharacterDetails();
        mSelectedItemsIds = new SparseBooleanArray();
        Log.i("llll", "construcor adapter"+id);
        this.context = context;
        this.songs_list=list;
        builder=new AlertDialog.Builder(context);
        con=new ApplicationController(context.getApplicationContext(),context);Log.i("llll", "--");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        if(id.equals("open_album_true")){
            this.id="open_album";
            con.setMylist(songs_list,"open_album",imagesset);

        }else {
            this.id = id;
        }
        Log.i("llll", "--");

        if(id.equals("allsongs_noanimation")){
            artist_songs=true;
            id="allsongs";
            this.id="allsongs";
        }
        if(id.equals(sopen_playlist)  ) {
            plylst = (playlist_data) context;
            Log.i("llll", "--");
        }
        if(id.equals(sfolders)  ) {
            folderfrag = (Folderfrag) context;
            Log.i("llll", "--");
        }

        displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        Log.i("llll", "construcor adapter end"+id);
        resolver=context.getContentResolver();
    }

    //folders
    public recycler_adapter(Context context,Folderfrag folderfrag,ArrayList<songs> list,String id) {

        allCharacterDetails=new AllCharacterDetails();
        mSelectedItemsIds = new SparseBooleanArray();
        Log.i("llll", "construcor adapter"+id);
        this.context = context;
        this.songs_list=list;
        this.id=id;
        builder=new AlertDialog.Builder(context);
        con=new ApplicationController(context.getApplicationContext(),context);Log.i("llll", "--");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        this.folderfrag = folderfrag;
        Log.i("llll", "--");

        displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        Log.i("llll", "construcor adapter end"+id);
        resolver=context.getContentResolver();
    }



    @Override
    public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=null;
         if(id.equals(salbum)){
             Log.i("llll",salbum);
            view= LayoutInflater.from(context).inflate(R.layout.album_row,parent,false);

             //set height of cardview=width
             int width = displaymetrics.widthPixels;
             int n=context.getResources().getInteger(R.integer.columncount);
             width=width/n;
             ViewGroup.LayoutParams params = view.getLayoutParams();
             params.height = (int) (width * 1.3);
             view.setLayoutParams(params);
         }else if(id.equals(sfolders) ){
             view= LayoutInflater.from(context).inflate(R.layout.circular_custom_row_2,parent,false);
         }else if(id.equals(sartist) || id.equals(splaylist)){
             view= LayoutInflater.from(context).inflate(R.layout.circular_custom_row,parent,false);
         }else{
             Log.i("llll","else");
             view= LayoutInflater.from(context).inflate(R.layout.custom_row,parent,false);
         }

        viewholder vh= new viewholder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final viewholder holder, final int position) {
        Log.i("animtaa","onBindView at pos"+String.valueOf(position));

        songs ss=con.getsong();

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        int img_no=sharedPreferences.getInt("image_chooser",0);
        Boolean night=sharedPreferences.getBoolean("check",false);
        Boolean dark=false;
        if(img_no>=1){
            dark=true;
        }
        //setting dark theme or light theme based on setting
        if(night){
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryTextNight));
            holder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryTextNight));
            holder.options.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.options_white));
        }else {
            if (dark || id.equals(salbum)) {
                holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryLightText));
                holder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryLightText));
                holder.options.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.options_white));
            } else {
                holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryText));
                holder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                holder.options.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.options));
            }
        }

        //action mode
        if(id.equals("album")) {
            if(mSelectedItemsIds.get(position)) {
                holder.check.setVisibility(View.VISIBLE);
            }else{
                holder.check.setVisibility(View.INVISIBLE);
            }
        }else{
            holder.contextual_colour.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x55aaaaaa: Color.TRANSPARENT);
            if(mActionModeSet) {
                holder.options.setVisibility(View.INVISIBLE);
            }else{
                holder.options.setVisibility(View.VISIBLE);
            }
        }

        //resetting recycled holder values
        if(holder.giff!=null) {
            holder.giff.setVisibility(View.INVISIBLE);
            holder.equaliser.setVisibility(View.INVISIBLE);
        }
        holder.image.setImageDrawable(null);

        try {
            if ((id.equals(sallsongs)||id.equals(snow_playing)||id.equals(sopen_album)||id.equals(sopen_playlist)) && ss != null) {
                Log.i("check","current pos:"+con.getCurrentPosition());
                if (songs_list.get(position).getId().equals(ss.getId())) {
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
        }catch (Exception e){   Log.i("hygh","exceptio ");
        }

        songs current=songs_list.get(position);
        String path= current.getImagepath();

        try {
            if(path!=null && path.length()>0 && !id.equals(sopen_album)){
                Log.i("dfrr", "bitmap!null");
                //dont assign images to songs in open_album
                Picasso.with(context)
                        .load(Uri.parse("file://"+path))
                        .error(R.drawable.testalbum)
                        .into(holder.image);

                //bottom bar colour in case of album
                if(id.equals(salbum)){
                   Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if(bitmap!=null) {
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette palette) {
                                // access palette colors here
                                Palette.Swatch swatch = palette.getDarkMutedSwatch();

                                //Log.i("lkll"," swatch values are "+String.valueOf(vibrant)+"\n"+String.valueOf(c)+"\n"+String.valueOf(d)+"\n"+
                                //String.valueOf(f)+"\n"+String.valueOf(a)+"\n"+String.valueOf(b));
                                if (swatch == null) {
                                    swatch = palette.getMutedSwatch();
                                }
                                try {
                                    holder.bottom_colour_album.setBackgroundColor(swatch.getRgb());
                                } catch (Exception e) {

                                    holder.bottom_colour_album.setBackgroundColor(0xff666666);
                                }
                            }
                        });
                    }
                }
            }else {
                Log.i("dfrr", "bitmap null");

                if(id.equals(splaylist)){
                    Log.i("dfrr", "playlist");
                    //holder.image.setImageResource(R.drawable.playlist1);
                }else if(id.equals(salbum)){
                    Log.i("dfrr", "album");
                    holder.image.setImageResource(R.drawable.testalbum);
                    holder.bottom_colour_album.setBackgroundColor(0xff666666);
                }else if(id.equals(sopen_album)){
                    Log.i("dfrr", "open_album");
                    holder.image.setImageResource(R.drawable.album);
                }else if(id.equals(sartist)){
                    Log.i("dfrr", "artist");
                    holder.circularimage.setImageResource(R.drawable.artist);
                }else if(id.equals(sfolders)){
                    Log.i("dfrr", "folders");
                    holder.circularimage.setImageResource(R.drawable.artist);
                    songs song=songs_list.get(position);
                    if(song.isbackFolder()){
                        holder.options.setVisibility(View.INVISIBLE);
                        holder.circularimage.setImageResource(R.drawable.return_folder);
                    }else if(song.isfolder()){
                        holder.circularimage.setImageResource(R.drawable.folder);
                        if(song.isrootfolder()){
                            holder.options.setVisibility(View.INVISIBLE);
                        }
                    }else {
                        holder.circularimage.setImageResource(R.drawable.album_white);
                    }

                }else {
                    //holder.image.setImageResource(R.drawable.mp3);
                    Log.i("dfrr", "else");
                    Picasso.with(context)
                            .load(R.drawable.testalbum)
                            .into(holder.image);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.name.setText(current.getName());
        holder.artist.setText(current.getArtist());

        if(id.equals(sfolders)){
            songs s=songs_list.get(position);
            if(s.isfolder() && !s.isbackFolder()) {
                String ser = current.getArtist() + " tracks";
                holder.artist.setText(ser);
            }
        }


        Log.i("animt","on bind view ... mActionmode="+String.valueOf(mActionModeSet));

        /*
        if(id.equals("allsongs") && !mActionModeSet &&cananimate) {
            Log.i("animt","animating view.....");
            boolean goesdown = (position >= startpos);
            Animationclass anim = new Animationclass();
            anim.animate(holder, goesdown);
            startpos = position;
        }
        */

    }
    @Override
    public int getItemCount() {
        if(songs_list!=null){
            return songs_list.size();
        }else{
            Log.i("sectionn","getitemcount="+songs_list.size());
            songs_list=new ArrayList<>();
            return 0;
        }

    }

    class viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView image;
        TextView name;
        TextView artist;
        ImageView options;
        LinearLayout contextual_colour;
        View item;
        CardView card;
        ImageView check,equaliser;
        FrameLayout bottom_colour_album;
        pl.droidsonroids.gif.GifTextView giff;
        de.hdodenhof.circleimageview.CircleImageView circularimage;
        public viewholder(View itemView) {
            super(itemView);
            item=itemView;

            if(id.equals("album")){
                name=(TextView)itemView.findViewById(R.id.album_name);
                artist=(TextView)itemView.findViewById(R.id.album_artist);
                options=(ImageView)itemView.findViewById(R.id.album_options);
                image=(ImageView)itemView.findViewById(R.id.album_image);
                image.setOnClickListener(this);
                card=(CardView) itemView.findViewById(R.id.card_view);
                check=(ImageView)itemView.findViewById(R.id.album_check);
                bottom_colour_album=(FrameLayout)itemView.findViewById(R.id.album_bottom_colour);
            }else{

                name=(TextView)itemView.findViewById(R.id.songs_name);
                artist=(TextView)itemView.findViewById(R.id.songs_artist);
                options=(ImageView)itemView.findViewById(R.id.options);
                image=(ImageView)itemView.findViewById(R.id.songs_image);
                itemView.setOnClickListener(this);
                contextual_colour=(LinearLayout)itemView.findViewById(R.id.backgroundcolour);

                if(id.equals(sartist) || id.equals(splaylist) || id.equals(sfolders) ){
                    circularimage=(de.hdodenhof.circleimageview.CircleImageView)itemView.findViewById(R.id.cicular_drawable);
                }
                if(id.equals(sallsongs)||id.equals(snow_playing)||id.equals(sopen_album)||id.equals(sopen_playlist)){
                    Log.i("hygh","set");
                    equaliser=(ImageView)itemView.findViewById(R.id.giff_pause);
                    giff=(pl.droidsonroids.gif.GifTextView)itemView.findViewById(R.id.giff_id);
                }
            }
                options.setOnClickListener(this);

        }

        @Override
        public void onClick(final View v) {
            Log.i("hjgh","onclick id="+id);

            if(mActionModeSet){
                //this prevents song playing on double tapping in actionmode
                return;
            }
            if(v.getId()==R.id.options){
                if(id.equals("song") || id.equals("open_album")|| id.equals("allsongs")){
                    handleSongsOptions(v);
                }else if(id.equals("playlist")){
                    /// ppopup menu for playlists options
                    handlePlaylistOptions(v);
                }else if(id.equals("open_playlist")){
                    /// ppopup menu for playlists options
                    handle_openPlaylist_options(v);
                }else if(id.equals("now_playing")){
                    /// ppopup menu for playlists options
                    handle_nowPlaying_options(v);
                }else if(id.equals("artist")){
                    /// ppopup menu for playlists options
                    handle_artist_options(v);
                }else if(id.equals(sfolders)){
                    /// ppopup menu for playlists options
                    if(songs_list.get(getLayoutPosition()).isfolder()){
                        handlefolderOptions(v);
                    }else{
                        handleSongsOptions(v);
                    }
                }

            }else if(v.getId()==R.id.album_options) {
                handle_album_options(v);
            }else if(v.getId()==R.id.album_image){
                open_album(false,v);
            }else
            // whole item click for song selection
                handleClick(v);
            }

        /////-----------
        private void handleClick(View v){
            if(id.equals("song")|| id.equals("allsongs")){
                playsong();
            }else if(id.equals("playlist")){
                openplaylist();
            }else if(id.equals("now_playing")){
                playsong_nowplaying();
            }else if(id.equals("open_album")){
                playsong();
            }else if(id.equals("open_playlist")){
                playsong_openplaylist();
            }else if(id.equals("artist")){
                open_artist(false,songs_list.get(getLayoutPosition()).getName());
            }else if(id.equals(sfolders)){
                handle_folderclick();
            }
        }

        private void handleSongsOptions(final View v){
            Log.i("hjgh","handle songs options");
            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.songs_options,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int ids=item.getItemId();
                    switch (ids){
                        case R.id.psongs_add_to_playlist: {
                            addSongtoPlaylist(v);
                            return true;
                        }
                        case R.id.psongs_delete: {
                            builder.setMessage("are you sure you want to delete "+songs_list.get(getLayoutPosition()).getName());
                            builder.setCancelable(true) ;
                            builder.setPositiveButton(
                                    "yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            deletesong(songs_list.get(getLayoutPosition()).getId());
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
                        case R.id.psongs_playnext: {
                            con.addSongtoNextPos(songs_list.get(getLayoutPosition()));
                            return true;
                        }
                        case R.id.psongs_addtoqueue: {
                            add_to_queue(songs_list.get(getLayoutPosition()));
                            return true;
                        }
                        case R.id.psongs_moreFromArtist: {
                            open_artist(false,songs_list.get(getLayoutPosition()).getArtist());
                            return true;
                        }
                        case R.id.psongs_OpenAlbum: {
                            openAlbumforSong();
                            return true;
                        }
                        case R.id.psongs_setasringtone: {
                            Log.i("notiff","set as ringtone");
                            builder=new AlertDialog.Builder(context);
                            builder.setTitle(R.string.SettingAsRingtone)
                                    .setMessage(R.string.askingtocutorset)
                                    .setPositiveButton(R.string.alert_yes_button,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {
                                                    Intent intent = new Intent(context,RingdroidEditActivity.class);
                                                    intent.putExtra("was_get_content_intent", false);
                                                    intent.putExtra("data",Uri.parse(songs_list.get(getLayoutPosition()).getData()).toString());
                                                    context.startActivity(intent);
                                                }
                                            })
                                    .setNegativeButton(
                                            R.string.alert_no_button,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {

                                                    setAsRingtone(songs_list.get(getLayoutPosition()));
                                                    dialog.dismiss();
                                                    Toast.makeText(context,R.string.default_ringtone_success_message,Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                    .setCancelable(true)
                                    .show();


                            return true;
                        }
                        case R.id.psongs_edit_info: {
                            edit_songinfo();
                            return true;
                        }
                        case R.id.psongs_cut: {
                            Intent intent = new Intent(context,RingdroidEditActivity.class);
                            intent.putExtra("was_get_content_intent", false);
                            intent.putExtra("data",Uri.parse(songs_list.get(getLayoutPosition()).getData()).toString());
                            context.startActivity(intent);
                            return true;
                        }
                        case R.id.psongs_share: {
                            shareSong(songs_list.get(getLayoutPosition()).getData());
                            return true;
                        }
                    }
                    return true;
                }
            });
            popup.show();


        }
        private void handlePlaylistOptions(View v){
            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.playlist_options,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==R.id.Oplaylist_open){
                        openplaylist();
                    }else if(item.getItemId()==R.id.Oplaylist_delete){
                        builder.setMessage("Are you sure you want to delete : "+songs_list.get(getLayoutPosition()).getName());
                        builder.setCancelable(true);
                        builder.setPositiveButton(
                                "yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        deleteplaylist(songs_list.get(getLayoutPosition()).getId());
                                        songs_list.remove(getLayoutPosition());
                                        notifyItemRemoved(getLayoutPosition());
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

                    }
                    else if(item.getItemId()==R.id.Oplaylist_rename){
                        builder.setTitle("Playlist name");
                        builder.setCancelable(true);
                        // Set up the input
                        final EditText input = new EditText(context);
                        input.setInputType(InputType.TYPE_CLASS_TEXT );
                        String s=songs_list.get(getLayoutPosition()).getName();
                        input.setText(s);
                        input.setSelection(0,s.length());
                        builder.setView(input);
                        builder.setPositiveButton(
                                "Rename",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String str=input.getText().toString();
                                        int pos=getLayoutPosition();
                                        Long playlistid=songs_list.get(pos).getId();
                                        ContentValues values = new ContentValues();
                                        values.put(MediaStore.Audio.Playlists.NAME,str);
                                        songs_list.get(pos).setName(str);
                                        resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                                                values, "_id=" + playlistid, null);
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
                    }if(item.getItemId()==R.id.Oplaylist_addtoQueue){
                        new addPlaylistToQueue().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,songs_list.get(getLayoutPosition()).getId());
                    }
                    return true;
                }
            });
            popup.show();
        }
        private void handle_openPlaylist_options(View v){
            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.open_playlist,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==R.id.open_Remove){
                        // remove song from playlist ... plylst is the interface with activity openplaylist
                        Log.i("popop","playlist id="+String.valueOf(plylst.getplaylist_id()));

                        removesongfromplaylist(songs_list.get(getLayoutPosition()).getId(),plylst.getplaylist_id());
                        ((open_playlist)context).refreshNoOfSongs();
                    } else if(item.getItemId()==R.id.open_play){
                        playsong_openplaylist();
                    }else if(item.getItemId()==R.id.open_PlayNext){
                        con.addSongtoNextPos(songs_list.get(getLayoutPosition()));
                        return true;
                    }else if(item.getItemId()==R.id.open_AddToQueue){
                        add_to_queue(songs_list.get(getLayoutPosition()));
                    }else if(item.getItemId()==R.id.open_share){
                        shareSong(songs_list.get(getLayoutPosition()).getData());
                    }
                    return true;
                }
            });
            popup.show();
        }
        private void handle_nowPlaying_options(View v){
            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.now_playing_options,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==R.id.now_playing_play){
                        // play song
                        playsong_nowplaying();
                    } else if(item.getItemId()==R.id.now_playing_removefromqueue){
                        // remove from queue i.e from arreylist of service
                        con.remove_song(getLayoutPosition());
                        notifyItemRemoved(getLayoutPosition());
                    }else if(item.getItemId()==R.id.now_playing_share){
                        shareSong(songs_list.get(getLayoutPosition()).getData());

                    }
                    return true;
                }
            });
            popup.show();
        }
        private void handle_album_options(final View v){

            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.album,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    builder=new AlertDialog.Builder(context);
                    if(item.getItemId()==R.id.album_edit_info){
                        edit_albuminfo();
                    }else if(item.getItemId()==R.id.album_playall){
                        albumplayall(songs_list.get(getLayoutPosition()).getId());
                    }else if(item.getItemId()==R.id.album_addtoqueue){
                        addAlbumtoqueue(songs_list.get(getLayoutPosition()).getId());
                    }else if(item.getItemId()==R.id.album_morefromartist){
                        open_artist(false,songs_list.get(getLayoutPosition()).getArtist());
                    }else if(item.getItemId()==R.id.Album_addToPlaylist){
                        addAlbumtoPlaylist(songs_list.get(getLayoutPosition()).getId());
                    }else if(item.getItemId()==R.id.album_delete){
                        builder.setMessage("are you sure you want to delete "+songs_list.get(getLayoutPosition()).getName());
                        builder.setCancelable(true) ;
                        builder.setPositiveButton(
                                "yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        deleteAlbum(songs_list.get(getLayoutPosition()).getId());
                                        notifyItemRemoved(getLayoutPosition());
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

                    }
                    return true;
                }
            });
            popup.show();
        }
        private void handle_artist_options(final View v){

            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.artist_options,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    builder = new AlertDialog.Builder(context);
                    if (item.getItemId() == R.id.artist_open) {
                        open_artist(false,songs_list.get(getLayoutPosition()).getName());;
                    }else if (item.getItemId() == R.id.artist_rename) {
                        rename_artist();
                    }else if (item.getItemId() == R.id.artist_delete) {
                        delete_artist();
                    }
                    return true;
                }
            });
            popup.show();
        }
        private void handlefolderOptions(View v) {
            popup = new PopupMenu(context, v);
            popup.getMenuInflater().inflate(R.menu.folder_options, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int ids = item.getItemId();
                    switch (ids) {
                        case R.id.folder_play: {
                            folder_play();
                            return true;
                        }
                        case R.id.folder_addtoqueue: {
                            folder_addtoqueue();
                            return true;
                        }
                        case R.id.folder_addtoplaylist: {
                            folder_addtoplaylist();
                            return true;
                        }
                        case R.id.folder_delete: {
                            folder_delete();
                            return true;
                        }

                    }
                    return true;
                }
            });
            popup.show();
        }

        //song option methods +openalbum
        private void playsong(){
            int pos=con.getCurrentPosition();
            if(id.equals("allsongs")){
                if(artist_songs){
                    //id for atist all songs and all songs is same
                    con.setMylist(songs_list, "artistsongs", true);
                    con.playsong(getLayoutPosition());
                }else {
                    con.setMylist(con.allsonglist, "allsongs", true);
                    con.playsong(getLayoutPosition());
                    //face.setcardss(con.getsong());
                }
            }if(id.equals("open_album")){
                con.setMylist(songs_list,"open_album",imagesset);
                con.playsong(getLayoutPosition());
            }
            notifyItemChanged(getLayoutPosition());
            notifyItemChanged(pos);

        }
        private void openAlbumforSong(){
            final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            final String _id = MediaStore.Audio.Albums._ID;
            final String album_name = MediaStore.Audio.Albums.ALBUM;
            final String artist = MediaStore.Audio.Albums.ARTIST;
            final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
            final String tracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS;

            final String[] columns = { _id, album_name, artist, albumart, tracks };
            Cursor cursor=null;
            String Selection=_id+ " = ? ";
            String[] args=new String[]{String.valueOf(songs_list.get(getLayoutPosition()).getAlbum_id())};
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

                Intent intent=new Intent(context,open_playlist.class);
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
                context.startActivity(intent);
            }else{
                Toast.makeText(context,"No Album Found",Toast.LENGTH_SHORT).show();
                try {
                    cursor.close();
                }catch (Exception e ){
                    e.printStackTrace();
                }
            }

        }
        private void deletesong(Long audioid){
            int i = 0;
            try {
                String where = MediaStore.Audio.Playlists.Members._ID + "=?";
                String sid = String.valueOf(audioid);
                String[] whereVal = {sid};
                String path=songs_list.get(getLayoutPosition()).getData();
                Log.i("dlt","path="+path);

                i = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        where, whereVal);
                Log.i("uiii",String.valueOf(i));
                if(i!=0){
                    try{
                        File file=new File(path);
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
                if(con.getsong()!=null &&(con.getsong().getId()).equals(audioid)){
                    Toast.makeText(context, "songs removed: " + songs_list.get(getLayoutPosition()).getName(), Toast.LENGTH_SHORT).show();
                    Log.i("gtht","same deleted");
                    con.pause();
                    con.getlist().remove(con.getCurrentPosition());
                    con.playnext();
                    notifyItemRemoved(getLayoutPosition());
                }else {
                    Toast.makeText(context, "songs removed: " + songs_list.get(getLayoutPosition()).getName(), Toast.LENGTH_SHORT).show();
                    songs_list.remove(getLayoutPosition());
                    notifyItemRemoved(getLayoutPosition());
                    Log.i("gtht", "con.getsong().getId()=" + con.getsong().getId());
                    Log.i("gtht", "audioid=" + audioid);
                }

            }

        }
        private void shareSong(String data){
            Uri uri= Uri.parse("file:///"+data);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/mp3");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(share, "Share Sound File"));
        }
        private void add_to_queue(songs song){
            ArrayList<songs> listt =new ArrayList<>();
            listt.add(song);
            con.addSongToList(listt);
        }
        private void setAsRingtone(songs Curr_song){
            boolean permission;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permission = Settings.System.canWrite(context);
            } else {
                permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
            }
            if (permission) {
                //do your code

                Log.i("notiff","has permission");
                songs song=Curr_song;
                String path=song.getData();

                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.IS_RINGTONE,true);

                int i=context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values,
                        MediaStore.Audio.Media._ID+" = ? ",new String[]{String.valueOf(song.getId())});
                Uri newUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
                Log.i("ringg","updated="+i);
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                            context, RingtoneManager.TYPE_RINGTONE,
                            newUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("ringg","exception="+e.toString());

                }

            }else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    ((Activity)context).startActivityForResult(intent,12);
                } else {
                    ActivityCompat.requestPermissions(((Activity)context), new String[]{Manifest.permission.WRITE_SETTINGS}, 12);
                }
            }
        }
        private void edit_songinfo(){
            Log.i("notiff","set as ringtone");
            songs song=songs_list.get(getLayoutPosition());
            Intent intent=new Intent(context,Edit_Info_songs.class);

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
            intent.putExtra("song_data",song.getData());

            context.startActivity(intent);
        }
        private void addSongtoPlaylist(View v){
            PopupMenu popup1=new PopupMenu(context,v);
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
                                addnewPlaylistwithSongs(songs_list.get(getLayoutPosition()));
                            } else {
                                addTracksToPlaylist(playlist.getId(), songs_list.get(getLayoutPosition()));
                                Toast.makeText(context, "added " + songs_list.get(getLayoutPosition()).getName() + " to " + playlist.getName(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    return true;
                }
            });
            popup1.show();
        }

        //album option methods
        private void open_album(Boolean playall,View clickedView){
            Intent intent =new Intent(context,open_playlist.class);
            //using same activity to open playlists and albums
            Log.i("checkalbum","openalbum id="+id+" playall="+playall);
            // for shared transitoin
            // create the transition animation - the images in the layouts
            // of both activities are defined with android:transitionName="playlistTransition"

            boolean cananimatealbum=sharedPref.getBoolean("album_transition",true) ;
            try {
                ActivityOptions options = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&cananimatealbum) {
                    try {
                        Pair<View,String> first;
                        Pair<View,String> second;

                        try{
                            Log.i("checkalbum","main");
                            ((MainActivity) context).getWindow().setExitTransition(TransitionInflater.from(context).inflateTransition(R.transition.fade_edited));
                            ((MainActivity) context).getWindow().setEnterTransition(TransitionInflater.from(context).inflateTransition(R.transition.fade_edited));
                            first=Pair.create(((MainActivity) context).findViewById(android.R.id.navigationBarBackground), Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                            second=Pair.create(((MainActivity) context).findViewById(android.R.id.navigationBarBackground), Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                        }catch (Exception e){
                            Log.i("checkalbum","main catch");
                            first=Pair.create(((ChooseArtistAlbum) context).findViewById(android.R.id.navigationBarBackground), Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                            second=Pair.create(((ChooseArtistAlbum) context).findViewById(android.R.id.navigationBarBackground), Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                            ((ChooseArtistAlbum) context).getWindow().setExitTransition(TransitionInflater.from(context).inflateTransition(R.transition.fade_edited));
                            ((ChooseArtistAlbum) context).getWindow().setEnterTransition(TransitionInflater.from(context).inflateTransition(R.transition.fade_edited));
                        }

                        options = ActivityOptions.makeSceneTransitionAnimation((AppCompatActivity) context,
                                Pair.create((View) image, "albumTransition"),first,second
                                //Pair.create(((MainActivity) context).findViewById(android.R.id.navigationBarBackground), Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME),
                                //Pair.create(((MainActivity) context).findViewById(android.R.id.statusBarBackground), Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)
                        );
                    } catch (Exception e) {
                        Log.i("checkalbum","catch no anim");
                        Pair<View,String> first1;

                        try{
                            Log.i("checkalbum","main");
                            first1=Pair.create(((MainActivity) context).findViewById(android.R.id.statusBarBackground), Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                        }catch (Exception et){
                            Log.i("checkalbum","main catch");
                            first1=Pair.create(((ChooseArtistAlbum) context).findViewById(android.R.id.statusBarBackground), Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                        }
                        e.printStackTrace();
                        // for phones which do not have a navigaotion bar
                        options = ActivityOptions.makeSceneTransitionAnimation((AppCompatActivity) context,
                                Pair.create((View) image, "albumTransition"),first1
                                //Pair.create(((MainActivity) context).findViewById(android.R.id.statusBarBackground), Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)
                        );
                    }
                }
                intent.putExtra("method", "album");
                intent.putExtra("album_art", songs_list.get(getLayoutPosition()).getImagepath());
                intent.putExtra("album_name", songs_list.get(getLayoutPosition()).getName());
                intent.putExtra("album_playall", playall);
                intent.putExtra("album_id", songs_list.get(getLayoutPosition()).getId());////////////////////
                if (options != null) {

                    context.startActivity(intent, options.toBundle());
                } else {

                    context.startActivity(intent);
                }
            }catch (Exception e){
                Log.i("checkalbum","outer catch");

                e.printStackTrace();
                intent=new Intent(context,open_playlist.class);
                intent.putExtra("method", "album");
                intent.putExtra("album_art", songs_list.get(getLayoutPosition()).getImagepath());
                intent.putExtra("album_name", songs_list.get(getLayoutPosition()).getName());
                intent.putExtra("album_playall", playall);
                intent.putExtra("album_id", songs_list.get(getLayoutPosition()).getId());////////////////////
                context.startActivity(intent);
            }
        }
        private void albumplayall(Long _id){
            ArrayList<songs> curlist=DataFetch.getSongsOfAlbum(context,_id);
            con.setMylist(curlist,"",true);
            con.playsong(0);
        }
        private void deleteAlbum(Long _id) {
            ArrayList<songs> curlist=DataFetch.getSongsOfAlbum(context,_id);
            for(songs song:curlist) {
                Long audioid = song.getId();
                int i = 0;
                try {
                    String where = MediaStore.Audio.Playlists.Members._ID + "=?";
                    String sid = String.valueOf(audioid);
                    String[] whereVal = {sid};
                    String path = songs_list.get(getLayoutPosition()).getData();
                    Log.i("dlt", "path=" + path);

                    i = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            where, whereVal);
                    Log.i("uiii", String.valueOf(i));
                    if (i != 0) {
                        try {
                            File file = new File(path);
                            file.delete();
                        } catch (Exception e) {
                            Log.e("dlt", "error in deleting song from sd card");

                        }
                    }
                    songs_list.remove(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("uiii", e.toString());
                }
            }
        }
        private void addAlbumtoPlaylist(Long _id){
            final ArrayList<songs> playlist_list=get_playlist();
            //show a dialog

            CharSequence playlistts[]=new String[playlist_list.size()];
            for(int i=0;i<playlist_list.size();i++){
                String s=playlist_list.get(i).getName();
                playlistts[i]=(CharSequence) s;
            }
            builder = new AlertDialog.Builder(context);
            if(playlist_list.size()==0){
                builder.setTitle("No Playlists found");
            }else{
                builder.setTitle("Choose a Playlist");

            }
            final ArrayList<Long> selectedsong_ids ;
            selectedsong_ids=DataFetch.getSongIdsOfAlbum(context,_id);

            builder.setItems(playlistts, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on playlistts[which]
                    playlistIdForMultipleAdd=playlist_list.get(which).getId();

                    if (playlistIdForMultipleAdd.equals(0L)) {
                        // add new playlist and add songs to tht playlist
                        dialog.dismiss();
                        addnewPlaylistwithSongsAsync(selectedsong_ids);
                    } else {
                        addToPaylistMultiple addtoPlaylist=new addToPaylistMultiple();
                        addtoPlaylist.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);

                    }
                }
            });
            builder.setCancelable(false);
            builder.setPositiveButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog =builder.create();
            dialog.show();

        }
        private void addAlbumtoqueue(Long _id){
            ArrayList<songs> curlist=DataFetch.getSongsOfAlbum(context,_id);
            con.addSongToList(curlist);
        }
        private void edit_albuminfo(){
            Log.i("notiff","set as ringtone");
            songs song=songs_list.get(getLayoutPosition());
            Intent intent=new Intent(context,Edit_Info_songs.class);

            if(song.getImagepath()!=null) {
                intent.putExtra("song_image", song.getImagepath());
            }
            intent.putExtra("is_album",true);

            intent.putExtra("album_title",song.getName());
            intent.putExtra("album_artist",song.getArtist());
            intent.putExtra("album_image",song.getImagepath());
            intent.putExtra("album_year",song.getYear());
            intent.putExtra("album_id",String.valueOf(song.getId()));

            context.startActivity(intent);
        }

        //playlist option methods
        private void openplaylist(){
            Long id = songs_list.get(getLayoutPosition()).getId();
            Intent intent =new Intent(context,open_playlist.class);
            intent.putExtra("method","playlist");
            intent.putExtra("playlist_name",songs_list.get(getLayoutPosition()).getName());
            intent.putExtra("playlist_id",id);
            context.startActivity(intent);

        }
        private void deleteplaylist(Long playlistid){
            String playlistids = String.valueOf(playlistid);
            String where = MediaStore.Audio.Playlists._ID + "=?";
            String[] whereVal = {playlistids};
            resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
            Toast.makeText(context,songs_list.get(getLayoutPosition()).getName() + " Deleted", Toast.LENGTH_SHORT).show();
            return ;

        }

        //artist option methods
        private void open_artist(Boolean playall,String artistname){

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
                cursor = context.getContentResolver().query(uri, columns, selection,
                        args,null);
            }catch (Exception e){}
            if(cursor!=null) {
                int size = cursor.getCount();
                if (size == 0) {
                    Log.i("artistadaptr", "cursor==null or no album");
                    Intent intent2 = new Intent(context, Now_playing.class);
                    intent2.putExtra("artistname", artistname);
                    intent2.putExtra("method", "artistsongs");
                    try {
                        cursor.close();
                    }catch (Exception e ){
                        e.printStackTrace();
                    }
                    context.startActivity(intent2);
                }else if(size==1){
                    if( cursor.moveToFirst()){
                        Log.i("artistadaptr","cursor!=null");
                        Intent intent = new Intent(context, open_playlist.class);

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
                        context.startActivity(intent);

                    }
                }else{
                    Intent intent = new Intent(context, ChooseArtistAlbum.class);
                    intent.putExtra("artistname",artistname);
                    try {
                        cursor.close();
                    }catch (Exception e ){
                        e.printStackTrace();
                    }
                    context.startActivity(intent);
                }
            }
        }
        private void rename_artist(){
            builder.setTitle("Playlist name");
            builder.setCancelable(true);
            // Set up the input
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT );
            String s=songs_list.get(getLayoutPosition()).getName();
            input.setText(s);
            input.setSelection(0,s.length());
            final int pos=getLayoutPosition();
            final Long idd=songs_list.get(pos).getId();

            builder.setView(input);
            builder.setPositiveButton(
                    "Rename",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String str=input.getText().toString();

                            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            String artist_id = MediaStore.Audio.Media.ARTIST_ID;

                            ContentValues values = new ContentValues();
                            values.put(android.provider.MediaStore.Audio.Media.ARTIST,str);
                            songs_list.get(pos).setName(str);
                            int i=resolver.update(uri,
                                    values, artist_id +" = "+ idd, null);
                            notifyItemChanged(pos);
                            Log.i("artistedit","renamed result="+String.valueOf(i));
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
        private void delete_artist(){

            int ik=getLayoutPosition();
            //delete all songs of artist
            String[] artistid =new String[]{String.valueOf(songs_list.get(getLayoutPosition()).getId())};
            ArrayList<songs> curlist=DataFetch.getSongsOfArtist(context, MediaStore.Audio.Media.ARTIST_ID + " = ? ",artistid);
            for(songs song:curlist) {
                Long audioid = song.getId();
                int i = 0;
                try {
                    String where = MediaStore.Audio.Playlists.Members._ID + "=?";
                    String sid = String.valueOf(audioid);
                    String[] whereVal = {sid};
                    String path = songs_list.get(getLayoutPosition()).getData();
                    Log.i("dlt", "path=" + path);

                    i = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            where, whereVal);
                    Log.i("uiii", String.valueOf(i));
                    if (i != 0) {
                        try {
                            File file = new File(path);
                            file.delete();
                        } catch (Exception e) {
                            Log.e("dlt", "error in deleting song from sd card");

                        }
                    }
                    songs_list.remove(ik);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("uiii", e.toString());
                }
            }
        }

        //open_playlist option methods
        private void removesongfromplaylist(Long song_id,Long playlist_id){
            Log.i("popop","remove song");
            int i=0;
            try {
                Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                        "external", playlist_id);
                String where = MediaStore.Audio.Playlists.Members.AUDIO_ID  + "=?" ;

                String audioId1 = Long.toString(song_id);
                String[] whereVal = { audioId1 };
                i=resolver.delete(uri, where,whereVal);

            } catch (Exception e) {
                e.printStackTrace();
            }
            if(i>0) {
                //Toast.makeText(context, "songs removed: " + songs_list.get(getLayoutPosition()).getName(), Toast.LENGTH_SHORT).show();
                songs_list.remove(getLayoutPosition());
                notifyItemRemoved(getLayoutPosition());
            }
        }
        private void playsong_openplaylist(){
            int pos=con.getCurrentPosition();

            con.setMylist(songs_list,"open_playlist",imagesset);
            con.playsong(getLayoutPosition());

            //prevent animating of rows while adding or remving gif
            notifyItemChanged(getLayoutPosition());
            notifyItemChanged(pos);
        }
        private void playsong_nowplaying(){
            int pos=con.getCurrentPosition();

            con.playsong(getLayoutPosition());
            notifyItemChanged(getLayoutPosition());
            notifyItemChanged(pos);

        }

        private void handle_folderclick(){
            songs s=songs_list.get(getLayoutPosition());
            if(s.isfolder()) {
                if (s.isbackFolder()) {
                    folderfrag.openbackfolder();
                } else {
                    folderfrag.openFolder(s.getData(), s.getName());
                }
            }else{
                ArrayList<songs> curlist=new ArrayList<>();
                curlist.add(s);
                con.setMylist(curlist,"",true);
                con.playsong(0);
            }
        }
        private void folder_play(){
            ProgressDialog progress=new ProgressDialog(context);
            progress.setMessage("PLease wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(0);
            progress.setIndeterminate(true);
            progress.show();
            songs s=songs_list.get(getLayoutPosition());
            ArrayList<songs> list=fileUtil.getSongsInFoldersAsync(context,s.getData()+s.getName()+"/");
            Log.i("hjui","list.size()="+list.size());
            con.setMylist(list,"",true);
            con.playsong(0);
            progress.dismiss();
        }
        private void folder_addtoqueue(){
            songs s=songs_list.get(getLayoutPosition());
            fetchasyncMultipleFromFolder fetch=new fetchasyncMultipleFromFolder(s.getData()+s.getName()+"/");
            fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,0);

        }
        private void folder_addtoplaylist(){
            songs s=songs_list.get(getLayoutPosition());
            fetchasyncMultipleFromFolder fetch=new fetchasyncMultipleFromFolder(s.getData()+s.getName()+"/");
            fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,1);

        }
        private void folder_rename(){
            builder.setTitle("Playlist name");
            builder.setCancelable(true);
            // Set up the input
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT );
            songs s=songs_list.get(getLayoutPosition());
            final String folderparent=s.getData();
            final String folderpath=folderparent+s.getName();
            final File file=new File(folderpath);

            input.setText(s.getName());
            input.setSelection(0,s.getName().length());
            final int pos=getLayoutPosition();

            builder.setView(input);
            builder.setPositiveButton(
                    "Rename",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String str=input.getText().toString();
                            boolean success=file.renameTo(new File(folderparent+str));
                            if(success) {
                                songs_list.get(pos).setName(str);
                                notifyItemChanged(pos);

                            }
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
        private void folder_delete(){
            int i=getLayoutPosition();

            songs s=songs_list.get(i);
            fetchasyncMultipleFromFolder fetch=new fetchasyncMultipleFromFolder(s.getData()+s.getName()+"/");
            fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,2);
            songs_list.remove(i);
            notifyItemRemoved(i);

        }


    }

    private class Animationclass{
        public void animate(RecyclerView.ViewHolder holder,boolean goesdown){
            //AnimatorSet set=new AnimatorSet();
            int size=context.getResources().getInteger(R.integer.animationsize);
            ObjectAnimator object1=ObjectAnimator.ofFloat(holder.itemView,"translationY",goesdown?(size):(-size),0);
            object1.setDuration(300);
            object1.start();
            //set.playTogether(object1);
            //set.start();
        }
    }


    private  void addTracksToPlaylist(final long id,songs track) {
        int count = getplaylistsize(id);
        ContentValues values ;

        values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, count + 1);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, track.getId());

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);

        resolver.insert(uri, values);
        resolver.notifyChange(Uri.parse("content://media"), null);
    }
    private  void addMultipleTracksToPlaylist(final ArrayList<Long> selectedsong_ids) {
        final ArrayList<songs> playlist_list=get_playlist();
        //show a dialog

        CharSequence playlistts[]=new String[playlist_list.size()];
        for(int i=0;i<playlist_list.size();i++){
            String s=playlist_list.get(i).getName();
            playlistts[i]=(CharSequence) s;
        }
        builder = new AlertDialog.Builder(context);
        if(playlist_list.size()==0){
            builder.setTitle("No Playlists found");
        }else{
            builder.setTitle("Choose a Playlist");

        }

        builder.setItems(playlistts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on playlistts[which]
                playlistIdForMultipleAdd=playlist_list.get(which).getId();

                if (playlistIdForMultipleAdd.equals(0L)) {
                    // add new playlist and add songs to tht playlist
                    dialog.dismiss();
                    addnewPlaylistwithSongsAsync(selectedsong_ids);
                } else {
                    addToPaylistMultiple addtoPlaylist=new addToPaylistMultiple();
                    addtoPlaylist.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);

                }
            }
        });
        builder.setCancelable(true);
        builder.setPositiveButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog =builder.create();
        dialog.show();
    }

    private int getplaylistsize(Long ids){

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

    private ArrayList<songs> get_playlist(){
        ArrayList<songs> playlist_list=new ArrayList<>();
        playlist_list.add(new songs(0L,"Add New Playlist",""));
        final ContentResolver resolver = context.getContentResolver();
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

    private void addnewPlaylistwithSongs(final songs s){
        builder.setTitle("Playlist name");
        builder.setCancelable(true);
        final EditText input = new EditText(context);
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
    private Long findPlaylistIdByName(String name){

        Long id;
        final ContentResolver resolver = context.getContentResolver();
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
    private void createnewplaylist(String playlistname) {
        ContentValues mInserts = new ContentValues();
        mInserts.put(MediaStore.Audio.Playlists.NAME, playlistname);
        mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        context.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
    }
    private void addnewPlaylistwithSongsAsync(final ArrayList<Long> selectedsong_ids){
        builder=new AlertDialog.Builder(context);
        builder.setTitle("Playlist name");
        builder.setCancelable(true);
        final EditText input = new EditText(context);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);
        builder.setPositiveButton(
                "Create",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String s=input.getText().toString();
                        createnewplaylist(s);
                        playlistIdForMultipleAdd=findPlaylistIdByName(s);
                        addToPaylistMultiple addtoPlaylist=new addToPaylistMultiple();
                        addtoPlaylist.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);

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



    // action Mode Methods

    public void toggleSelection(int position){
        Log.i("animt","toggle selection");
        selectView(position, !mSelectedItemsIds.get(position));
    }
    public int getSelectedCount(){
    Log.i("contxt","getselectedcount"+String.valueOf(mSelectedItemsIds.size()));
        return mSelectedItemsIds.size();
    }
    //Put or delete selected position into SparseBooleanArray
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyItemChanged(position);
        //notifyDataSetChanged();
    }

    //Remove selected selections
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        Log.i("animt","remove selection");

       notifyDataSetChanged();
    }


    public void mActionmodeset(boolean b){
        mActionModeSet=b;
        notifyDataSetChanged();
        Log.i("animt","actionmodeset");
    }

    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
        }

    //songs and open_playlist action mode methods
    public void addtoplaylist_contextual(){
        final ArrayList<Long> selectedsong_ids ;
        selectedsong_ids=makeArrayOfidsFromSparseArray(false);
        addMultipleTracksToPlaylist(selectedsong_ids);
        removeSelection();

    }
    public void addtoqueue_contextual(){
        //Log.i("contxt1","add to queue");
        //Loop all selected ids

        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<songs> songlist=new ArrayList<>();
                for (int i = 0;i<(mSelectedItemsIds.size());i++) {
                    if (mSelectedItemsIds.valueAt(i)) {
                        int position=mSelectedItemsIds.keyAt(i);
                        //add the song from adapter songlist at this position to the current list of songs
                        songlist.add(songs_list.get(position));
                    }
                }
                con.addSongToList(songlist);
            }
        });
        t.run();
        removeSelection();
    }
    public void delete_contextual(){
        ArrayList<songs> selectedsong_ids =new ArrayList<>();
        currentPlayingSong=con.getsong();
        Log.i("contxt2","delete");
        selectedsong_ids=makeArrayOfsongsFromSparseArray(true);

        deletemultiple delete=new deletemultiple();
        delete.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);

    }
    public void share_contextual(){
        ArrayList<Uri> selectedsong_pathuri;
        selectedsong_pathuri=makeArrayOfPathUriFromSparseArray();
        mSelectedItemsIds=new SparseBooleanArray();
        notifyDataSetChanged();
        Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
        share.setType("audio/mp3");
        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, selectedsong_pathuri);
        context.startActivity(Intent.createChooser(share, "Share Sound File"));
    }
    public void removeFromPlaylist_contextual(){

        ArrayList<Long> selectedsong_ids=makeArrayOfidsFromSparseArray(true);

        removeFromPaylistMultiple remove=new removeFromPaylistMultiple();
        Log.i("popop","playlist id="+String.valueOf(plylst.getplaylist_id()));

        playlistIdForMultipleAdd=plylst.getplaylist_id();
        remove.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);
        removeSelection();
    }

    //albums actionmodemethods
    public void context_albumdelete(){
        ArrayList<Long> selectedsong_ids =new ArrayList<>();
        currentPlayingSong=con.getsong();
        Log.i("contxt2","delete");
        selectedsong_ids=makeArrayOfidsFromSparseArray(true);
        Log.i("deletem","selectedsong_ids size="+selectedsong_ids.size());

        deletemultiplealbum delete=new deletemultiplealbum();
        delete.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);

    }
    public void album_addtoplaylist_contextual(){
        final ArrayList<Long> selectedsong_ids ;
        selectedsong_ids=makeArrayOfSongidsFromSparseArrayMultipleAlbums(false);
        addMultipleTracksToPlaylist(selectedsong_ids);
        removeSelection();

    }
    public void album_addtoqueue_contextual(){
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<songs> songlist=new ArrayList<>();
                ArrayList<songs> currentsonglist=new ArrayList<>();
                for (int i = 0;i<(mSelectedItemsIds.size());i++) {
                    if (mSelectedItemsIds.valueAt(i)) {
                        int position=mSelectedItemsIds.keyAt(i);
                        currentsonglist=DataFetch.getSongsOfAlbum(context,songs_list.get(position).getId());
                        songlist.addAll(currentsonglist);
                    }
                }
                con.addSongToList(songlist);
            }
        });
        t.run();
        removeSelection();

    }

    //albums actionmodemethods
    public void context_artistdelete(){
        ArrayList<Long> selectedsong_ids =new ArrayList<>();
        currentPlayingSong=con.getsong();
        Log.i("contxt2","delete");
        selectedsong_ids=makeArrayOfidsFromSparseArray(true);

        deletemultipleartist delete=new deletemultipleartist();
        delete.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);

    }
    public void context_artistaddtoPlaylist(){
        // Log.i("contxt1","add to playlist");
        //fetch all the playlist to choose from
        final ArrayList<songs> playlist_list=get_playlist();
        //show a dialog

        CharSequence playlistts[]=new String[playlist_list.size()];
        for(int i=0;i<playlist_list.size();i++){
            String s=playlist_list.get(i).getName();
            playlistts[i]=(CharSequence) s;
        }
        builder = new AlertDialog.Builder(context);
        if(playlist_list.size()==0){
            builder.setTitle("No Playlists found");
        }else{
            builder.setTitle("Choose a Playlist");

        }
        final ArrayList<Long> selectedsong_ids ;
        selectedsong_ids=makeArrayOfSongidsFromSparseArrayMultipleArtist(false);

        builder.setItems(playlistts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on playlistts[which]
                playlistIdForMultipleAdd=playlist_list.get(which).getId();

                if (playlistIdForMultipleAdd.equals(0L)) {
                    // add new playlist and add songs to tht playlist
                    dialog.dismiss();
                    addnewPlaylistwithSongsAsync(selectedsong_ids);
                } else {
                    addToPaylistMultiple addtoPlaylist=new addToPaylistMultiple();
                    addtoPlaylist.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);

                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog =builder.create();
        dialog.show();
        removeSelection();



    }


    private ArrayList<Long> makeArrayOfidsFromSparseArray(boolean removeFromRecyclerView){
        ArrayList<Long> selectedsong_ids =new ArrayList<>();
        //Loop all selected ids
        for (int i = (mSelectedItemsIds.size() - 1); i >= 0; i--) {
            if (mSelectedItemsIds.valueAt(i)) {
                int position=mSelectedItemsIds.keyAt(i);

                //If current id is selected remove the item via key
                selectedsong_ids.add(songs_list.get(position).getId());
                if(removeFromRecyclerView){
                    songs_list.remove(mSelectedItemsIds.keyAt(i));
                    notifyItemRemoved(position);//notify adapter
                }

            }
        }
        return selectedsong_ids;
    }
    private ArrayList<songs> makeArrayOfsongsFromSparseArray(boolean removeFromRecyclerView){
        ArrayList<songs> selectedsong_ids =new ArrayList<>();
        //Loop all selected ids
        for (int i = (mSelectedItemsIds.size() - 1); i >= 0; i--) {
            if (mSelectedItemsIds.valueAt(i)) {
                int position=mSelectedItemsIds.keyAt(i);

                //If current id is selected remove the item via key
                selectedsong_ids.add(songs_list.get(position));
                if(removeFromRecyclerView){
                    songs_list.remove(mSelectedItemsIds.keyAt(i));
                    notifyItemRemoved(position);//notify adapter
                }

            }
        }
        return selectedsong_ids;
    }
    private ArrayList<Long> makeArrayOfSongidsFromSparseArrayMultipleAlbums(boolean removeFromRecyclerView){
        ArrayList<Long> selectedsong_ids =new ArrayList<>();
        ArrayList<Long> currentAlbumSongIds=new ArrayList<>();
        //Loop all selected ids
        Long CurrentAlbumid;
        for (int i = (mSelectedItemsIds.size() - 1); i >= 0; i--) {
            if (mSelectedItemsIds.valueAt(i)) {
                int position=mSelectedItemsIds.keyAt(i);

                CurrentAlbumid=songs_list.get(position).getId();
                //selectedsong_ids.add(songs_list.get(position).getId());
                currentAlbumSongIds=DataFetch.getSongIdsOfAlbum(context,CurrentAlbumid);
                selectedsong_ids.addAll(currentAlbumSongIds);
                if(removeFromRecyclerView){
                    songs_list.remove(mSelectedItemsIds.keyAt(i));
                    notifyItemRemoved(position);//notify adapter
                }

            }
        }
        return selectedsong_ids;
    }
    private ArrayList<Long> makeArrayOfSongidsFromSparseArrayMultipleArtist(boolean removeFromRecyclerView){
        ArrayList<Long> selectedsong_ids =new ArrayList<>();
        ArrayList<Long> currentAlbumSongIds=new ArrayList<>();
        //Loop all selected ids
        Long CurrentArtistid;
        for (int i = (mSelectedItemsIds.size() - 1); i >= 0; i--) {
            if (mSelectedItemsIds.valueAt(i)) {
                int position=mSelectedItemsIds.keyAt(i);

                CurrentArtistid=songs_list.get(position).getId();
                //selectedsong_ids.add(songs_list.get(position).getId());
                currentAlbumSongIds=DataFetch.getSongIdsOfArtist(context,CurrentArtistid);
                selectedsong_ids.addAll(currentAlbumSongIds);
                if(removeFromRecyclerView){
                    songs_list.remove(mSelectedItemsIds.keyAt(i));
                    notifyItemRemoved(position);//notify adapter
                }

            }
        }
        return selectedsong_ids;
    }
    private ArrayList<Uri> makeArrayOfPathUriFromSparseArray(){
        ArrayList<Uri> selectedsong_ids =new ArrayList<>();
        //Loop all selected ids
        for (int i = (mSelectedItemsIds.size() - 1); i >= 0; i--) {
            if (mSelectedItemsIds.valueAt(i)) {
                int position=mSelectedItemsIds.keyAt(i);

                String data=(songs_list.get(position).getData());
                Uri uri= Uri.parse("file:///"+data);
                selectedsong_ids.add(uri);
            }
        }
        return selectedsong_ids;
    }

    private class deletemultiple extends AsyncTask<ArrayList<songs>,Void,Void>{
        private boolean currentdeleted=false;
        //songs current_song;

        ProgressDialog progress=new ProgressDialog(context);
        int typeBar=1;        // Determines type progress bar: 0 = spinner, 1 = horizontal
        int songsdeleted=0;
        int failed=0;
        @Override
        protected void onPreExecute() {
            Log.i("contxt2","delete async");
            //current_song=con.getsong();

            progress.setMessage("PLease wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(typeBar);
            progress.setIndeterminate(false);
            progress.setMax(getSelectedCount());
            progress.setProgress(songsdeleted);
            progress.show();
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(ArrayList<songs>... params) {
            Log.i("contxt2","current song with id= "+String.valueOf(currentPlayingSong.getId()) );

            ArrayList<songs> selectedsongs=params[0];
            Log.i("contxt2","delete async size"+String.valueOf(selectedsongs.size()));

            for(songs ss:selectedsongs){
                Long lid=ss.getId();
                Log.i("contxt2","deleting song with id= "+String.valueOf(lid) );

                if(currentPlayingSong.getId().equals(lid)){
                        Log.i("contxt2","current deleted");

                        currentdeleted=true;
                        publishProgress();
                        //stopcurrent song paying and start from beginning
                    }
                    int i = 0;
                    try {
                        String where = MediaStore.Audio.Playlists.Members._ID + "=?";
                        String sid = String.valueOf(lid);
                        String[] whereVal = {sid};

                        i=resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                where, whereVal);
                        Log.i("uiii",String.valueOf(i));


                    }catch (Exception e){
                        e.printStackTrace();
                        Log.i("uiii",e.toString());
                    }
                if(i!=0){
                    songsdeleted++;

                }else{
                    failed++;
                }
                    publishProgress();
            }

           return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(currentdeleted){
                con.pause();
            }
            progress.setProgress(songsdeleted);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void songses) {
            super.onPostExecute(songses);
            Log.i("contxt2","delete async total songs deleted "+String.valueOf(songsdeleted));

            if(currentdeleted){
                con.setMylist(songs_list,id,true);
                con.playsong(0);
                con.pause();
            }
            progress.dismiss();

            CoordinatorLayout m;
            try {
                m = ((MainActivity) context).coordinatorlayout;
                Snackbar snack = Snackbar.make(m, "Success :"+String.valueOf(songsdeleted)+"     Failed:"+String.valueOf(failed), Snackbar.LENGTH_LONG);
                snack.show();
            }catch (Exception e){
             Toast.makeText(context,"Success :"+String.valueOf(songsdeleted)+"     Failed:"+String.valueOf(failed),Toast.LENGTH_SHORT).show();
            }

        }
    }
    private class deletemultiplealbum extends AsyncTask<ArrayList<Long>,Void,Void>{
        public boolean currentdeleted=false;
        //songs current_song;

        ProgressDialog progress=new ProgressDialog(context);
        int typeBar=1;        // Determines type progress bar: 0 = spinner, 1 = horizontal
        int songsdeleted=0;
        int albumdeleted=0;
        int failed=0;
        @Override
        protected void onPreExecute() {
            Log.i("deletem","delete async");
            //current_song=con.getsong();

            progress.setMessage("Please wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(typeBar);
            progress.setIndeterminate(false);
            progress.setProgress(songsdeleted);
            progress.show();
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(ArrayList<Long>... params) {
            Log.i("deletem","current song with id= "+String.valueOf(currentPlayingSong.getId()) );

            ArrayList<Long> selectedalbums=params[0];
            Log.i("deletem","delete async size"+String.valueOf(selectedalbums.size()));
            progress.setMax(selectedalbums.size());

            for(Long lid:selectedalbums){
                Log.i("deletem","deleting song with id= "+String.valueOf(lid) );

                if(currentPlayingSong.getAlbum_id().equals(lid)){
                    Log.i("deletem","current deleted");

                    currentdeleted=true;
                    publishProgress();
                    //stopcurrent song paying and start from beginning
                }
                /*
                int i = 0;
                try {
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    i=resolver.delete(uri, MediaStore.Audio.Media.ALBUM_ID + " = ?",new String[]{String.valueOf(lid)});

                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("uiii",e.toString());
                }
                */
                //--
                ArrayList<songs> curlist=DataFetch.getSongsOfAlbum(context,lid);
                Log.i("deletem", "current album list size=" + curlist.size());

                for(songs song:curlist) {

                    Long audioid = song.getId();
                    String sid = String.valueOf(audioid);
                    String[] whereVal = {sid};
                    String where = MediaStore.Audio.Playlists.Members._ID + " = ? ";
                    String path = song.getData();

                    int i = 0;
                    try {
                        Log.i("deletem", "path=" + path);

                        i = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                where, whereVal);
                        Log.i("uiii", String.valueOf(i));
                        if (i != 0) {
                            Log.e("deletem", "delete result="+i);

                            songsdeleted++;
                            try {
                                File file = new File(path);
                                file.delete();
                            } catch (Exception e) {
                                Log.e("deletem", "error in deleting song from sd card");
                            }
                        }else{
                            failed++;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("deletem", e.toString());
                    }

                }
                albumdeleted++;
                publishProgress();

                //--

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(currentdeleted){
                con.pause();
            }
            progress.setProgress(albumdeleted);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void songses) {
            super.onPostExecute(songses);
            Log.i("deletem","delete async total songs deleted "+String.valueOf(songsdeleted));

            if(currentdeleted){
                con.setMylist(songs_list,id,true);
                con.playsong(0);
                con.pause();
            }
            progress.dismiss();
            CoordinatorLayout m=((MainActivity)context).coordinatorlayout;
            Snackbar snack = Snackbar.make(m, "Total deleted songs :"+String.valueOf(songsdeleted)+"     Failed to delete:"+String.valueOf(failed), Snackbar.LENGTH_LONG);

            snack.show();
        }
    }
    private class deletemultipleartist extends AsyncTask<ArrayList<Long>,Void,Void>{
        public boolean currentdeleted=false;
        //songs current_song;

        ProgressDialog progress=new ProgressDialog(context);
        int typeBar=1;        // Determines type progress bar: 0 = spinner, 1 = horizontal
        int songsdeleted=0;
        int failed=0;
        @Override
        protected void onPreExecute() {
            Log.i("contxt2","delete async");
            //current_song=con.getsong();

            progress.setMessage("PLease wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(typeBar);
            progress.setIndeterminate(false);
            progress.setMax(getSelectedCount());
            progress.setProgress(songsdeleted);
            progress.show();
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(ArrayList<Long>... params) {
            Log.i("contxt2","current song with id= "+String.valueOf(currentPlayingSong.getId()) );

            ArrayList<Long> selectedsongs=params[0];
            Log.i("contxt2","delete async size"+String.valueOf(selectedsongs.size()));

            for(Long lid:selectedsongs){
                Log.i("contxt2","deleting song with id= "+String.valueOf(lid) );

                /*
                int i = 0;
                try {
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    i=resolver.delete(uri, MediaStore.Audio.Media.ARTIST_ID + " = ?",new String[]{String.valueOf(lid)});

                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("uiii",e.toString());
                }
                */
                String[] artistid =new String[]{String.valueOf(lid)};
                ArrayList<songs> curlist=DataFetch.getSongsOfArtist(context, MediaStore.Audio.Media.ARTIST_ID + " = ? ",artistid);
                for(songs song:curlist) {
                    Long audioid = song.getId();
                    int i = 0;
                    try {
                        String where = MediaStore.Audio.Playlists.Members._ID + "=?";
                        String sid = String.valueOf(audioid);
                        String[] whereVal = {sid};
                        String path = song.getData();
                        Log.i("dlt", "path=" + path);

                        i = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                where, whereVal);
                        Log.i("uiii", String.valueOf(i));
                        if (i != 0) {
                            try {
                                File file = new File(path);
                                file.delete();
                            } catch (Exception e) {
                                Log.e("dlt", "error in deleting song from sd card");

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("uiii", e.toString());
                    }
                }
                 songsdeleted++;
                publishProgress();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(currentdeleted){
                con.pause();
            }
            progress.setProgress(songsdeleted);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void songses) {
            super.onPostExecute(songses);
            Log.i("contxt2","delete async total songs deleted "+String.valueOf(songsdeleted));

            if(currentdeleted){
                con.setMylist(songs_list,id,true);
                con.playsong(0);
                con.pause();
            }
            progress.dismiss();
            CoordinatorLayout m=((MainActivity)context).coordinatorlayout;
            Snackbar snack = Snackbar.make(m, "Success :"+String.valueOf(songsdeleted)+"     Failed:"+String.valueOf(failed), Snackbar.LENGTH_LONG);

            snack.show();
        }
    }
    private class addToPaylistMultiple extends AsyncTask<ArrayList<Long>,Void,Void>{
        public boolean currentdeleted=false;
        Long playlistid=new Long(playlistIdForMultipleAdd);
        //songs current_song;

        ProgressDialog progress=new ProgressDialog(context);
        int typeBar=1;        // Determines type progress bar: 0 = spinner, 1 = horizontal
        int success=0;

        @Override
        protected void onPreExecute() {
           //current_song=con.getsong();

            progress.setMessage("Please wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(typeBar);
            progress.setIndeterminate(false);
            progress.setProgress(success);
            progress.show();
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(ArrayList<Long>... params) {
            ArrayList<Long> list=params[0];
            progress.setMax(list.size());
            for(Long l:list){
                addTracksToPlaylist(playlistid,l);
                success++;
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progress.setProgress(success);

        }
        public  String addTracksToPlaylist(final long id,Long track) {
            int count = getplaylistsize(id);
            ContentValues values ;

            values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, count + 1);
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, track);

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

        @Override
        protected void onPostExecute(Void songses) {
            super.onPostExecute(songses);
            progress.dismiss();
            try{
                ((MainActivity)context).refreshFragment(2);
            }catch (Exception e){

            }
            con.playlistfragmentchanged=true;
            }
        }
    private class fetchasyncMultipleFromFolder extends AsyncTask<Integer,Void,Void>{
        String foldername;
        int method;
        ProgressDialog progress=new ProgressDialog(context);

        // Determines type progress bar: 0 = spinner, 1 = horizontal
        int typeBar=0;

        ArrayList<songs> list;
        ArrayList<Long> idlist;
        public fetchasyncMultipleFromFolder(String foldername){
            this.foldername=foldername;
        }
        @Override
        protected void onPreExecute() {
            //current_song=con.getsong();

            progress.setMessage("Please wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(typeBar);
            progress.setIndeterminate(true);
            progress.show();
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Integer... params) {
            method=params[0];
            switch (method){
                case 0:{
                    list=fileUtil.getSongsInFoldersAsync(context,foldername);
                    break;
                }
                case 1:{
                    idlist=fileUtil.getSongIdsInFoldersAsync(context,foldername);
                    break;
                }
                case 2:{
                    progress.dismiss();
                    File file=new File(foldername);
                    fileUtil.deletefile(context,file);
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void songses) {
            super.onPostExecute(songses);
            progress.dismiss();
            switch (method){
                case 0:{
                    con.addSongToList(list);
                    break;
                }
                case 1:{
                    addMultipleTracksToPlaylist(idlist);
                    break;
                }
            }
            progress.dismiss();

        }
    }
    private class removeFromPaylistMultiple extends AsyncTask<ArrayList<Long>,Void,Void>{
        public boolean currentdeleted=false;
        Long playlistid;
        //songs current_song;

        ProgressDialog progress=new ProgressDialog(context);
        int typeBar=1;        // Determines type progress bar: 0 = spinner, 1 = horizontal
        int success=0;

        @Override
        protected void onPreExecute() {
            //current_song=con.getsong();
            playlistid=playlistIdForMultipleAdd;
            progress.setMessage("PLease wait...");
            progress.setCancelable(false);
            progress.setProgressStyle(typeBar);
            progress.setIndeterminate(false);
            progress.setMax(getSelectedCount());
            progress.setProgress(success);
            progress.show();
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(ArrayList<Long>... params) {
            ArrayList<Long> list=params[0];
            for(Long l:list){
                if(removesongfromplaylist(l)){
                    Log.i("popop","success");

                    success++;
                }else{
                    Log.i("popop","no success");
                }
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progress.setProgress(success);

        }
        public boolean removesongfromplaylist(Long song_id){
            Log.i("popop","remove song playlist id="+String.valueOf(playlistIdForMultipleAdd));

            int i=0;
            try {
                Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                        "external", playlistid);
                String where = MediaStore.Audio.Playlists.Members.AUDIO_ID  + "=?" ;

                String audioId1 = Long.toString(song_id);
                String[] whereVal = { audioId1 };
                i=resolver.delete(uri, where,whereVal);

            } catch (Exception e) {
                e.printStackTrace();
            }
            if(i>0) {
               return true;
            }else{
                return false;
            }
        }
        @Override
        protected void onPostExecute(Void songses) {
            super.onPostExecute(songses);
            progress.dismiss();
            ((open_playlist)context).refreshNoOfSongs();

        }
    }

    private class addPlaylistToQueue extends AsyncTask<Long,Void,Void>{
        ArrayList<songs> list;
        @Override
        protected Void doInBackground(Long... params) {
            list=DataFetch.getPlaylist(context,params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);
            con.addSongToList(list);
            Toast.makeText(context,"Playlist added to queue",Toast.LENGTH_SHORT).show();
        }
    }

}
