package com.androidplay.rahul.myplayer;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Window;
import android.view.WindowManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Rahul on 14-07-2016.
 */

public class recycler_adapter extends RecyclerView.Adapter<recycler_adapter.viewholder>{
    public Context context;
    public ArrayList<songs> songs_list;
    private PopupMenu popup;
    ApplicationController con;
    String id;
    adaptr face;
    ContentResolver resolver;
    AlertDialog.Builder builder;

    //for animation set
    int startpos=0;
    boolean cananimate=true;

    //for ActionMode
    private SparseBooleanArray mSelectedItemsIds;
    private boolean mActionModeSet =false;
    songs currentPlayingSong;
    public boolean imagesset=false;
    Long playlistIdForMultipleAdd;
    DisplayMetrics displaymetrics;

    public interface adaptr{
        public void setcardss(songs song);
    }
    public interface playlist_data{
        public Long getplaylist_id();
    }


    playlist_data plylst;

    public recycler_adapter(Context context,ArrayList<songs> list,String id) {


        mSelectedItemsIds = new SparseBooleanArray();
        Log.i("llll", "construcor adapter"+id);
        this.context = context;
        this.songs_list=list;
        builder=new AlertDialog.Builder(context);
        con=new ApplicationController(context.getApplicationContext(),context);Log.i("llll", "--");

        if(id.equals("open_album_true")){
            this.id="open_album";
            con.setMylist(songs_list,"open_album",imagesset);

        }else {
            this.id = id;
        }
        Log.i("llll", "--");

        if(id.equals("allsongs_noanimation")){
            cananimate=false;
            id="allsongs";
            this.id="allsongs";
        }

        if(id.equals("allsongs") ||id.equals("playlist") ||id.equals("album") || id.equals("artist") ) {
            face = (adaptr) context;
            Log.i("llll", "--");
        }
        if(id.equals("open_playlist")  ) {
            plylst = (playlist_data) context;
            Log.i("llll", "--");
        }

        displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        Log.i("llll", "construcor adapter end"+id);
        resolver=context.getContentResolver();
    }


    @Override
    public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=null;
         if(id.equals("album")){
             Log.i("llll","album");
            view= LayoutInflater.from(context).inflate(R.layout.album_row,parent,false);

             //set height of cardview=width
             int width = displaymetrics.widthPixels;
             int n=context.getResources().getInteger(R.integer.columncount);
             width=width/n;
             ViewGroup.LayoutParams params = view.getLayoutParams();
             params.height = (int) (width * 1.3);
             view.setLayoutParams(params);
         }else if(id.equals("playlist") || id.equals("artist")){
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
        Log.i("animt","onBindView at pos"+String.valueOf(position));


        songs current=songs_list.get(position);
        String path="";
        /*if( id.equals("allsongs")){
            //all songs are stored in application context with images
             path=con.getimagepathforsong(position);
        }else {
             path = current.getImagepath();
        }
*/
        path = current.getImagepath();
        try {
            if(path!=null && path.length()>0 && !id.equals("open_album")){
                Log.i("llll", "bitmap!null");
                //dont assign images to songs in open_album
                Picasso.with(context)
                        .load(Uri.parse("file://"+path))
                        .error(R.drawable.mp3)
                        .into(holder.image);
            }else {
                Log.i("llll", "bitmap null");

                if(id.equals("playlist")){
                    Log.i("llll", "playlist");
                    //holder.image.setImageResource(R.drawable.playlist1);
                }else if(id.equals("album")){
                    holder.image.setImageResource(R.drawable.mp3full);
                }else if(id.equals("open_album")){
                    holder.image.setImageResource(R.drawable.album);
                }else if(id.equals("artist")){
                    holder.circularimage.setImageResource(R.drawable.artist);
                }else {
                    holder.image.setImageResource(R.drawable.mp3);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.name.setText(current.getName());
        holder.artist.setText(current.getArtist());

        if(!id.equals("album")) {
            holder.contextual_colour.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x55aaaaaa: Color.TRANSPARENT);
            if(mActionModeSet) {
                holder.options.setVisibility(View.INVISIBLE);
            }else{
                holder.options.setVisibility(View.VISIBLE);
            }
        }
        Log.i("animt","on bind view ... mActionmode="+String.valueOf(mActionModeSet));

        if(id.equals("allsongs") && !mActionModeSet &&cananimate) {
            Log.i("animt","animating view.....");
            boolean goesdown = (position >= startpos);
            Animationclass anim = new Animationclass();
            anim.animate(holder, goesdown);
            startpos = position;
        }
    }
    @Override
    public int getItemCount() {
        if(songs_list!=null){
            return songs_list.size();
        }else{
            songs_list=new ArrayList<>();
            return 0;
        }

    }

    public class viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView image;
        TextView name;
        TextView artist;
        ImageView options;
        LinearLayout contextual_colour;
        View item;
        CardView card;
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
            }else{

                name=(TextView)itemView.findViewById(R.id.songs_name);
                artist=(TextView)itemView.findViewById(R.id.songs_artist);
                options=(ImageView)itemView.findViewById(R.id.options);
                image=(ImageView)itemView.findViewById(R.id.songs_image);
                itemView.setOnClickListener(this);
                contextual_colour=(LinearLayout)itemView.findViewById(R.id.backgroundcolour);

                if(id.equals("artist")){
                    circularimage=(de.hdodenhof.circleimageview.CircleImageView)itemView.findViewById(R.id.cicular_drawable);
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
        public void handleClick(View v){
            if(id.equals("song")|| id.equals("allsongs")){
                if(!ApplicationController.currenntlistof.equals("song") || !con.withimages) {
                    con.setMylist(songs_list,"song",imagesset);
                }
                con.playsong(getLayoutPosition());
                if(id.equals("song") ||id.equals("playlist") ||id.equals("album") || id.equals("allsongs")) {
                    face.setcardss(con.getsong());

                }
            }else if(id.equals("playlist")){
                openplaylist();

            }else if(id.equals("now_playing")){
                //con.setsong(getLayoutPosition());
                // con.playsong();

                //---------con.setMylist(songs_list);

                con.playsong(getLayoutPosition());

            }else if(id.equals("open_album")){
                //con.setsong(getLayoutPosition());
                // con.playsong();
                con.setMylist(songs_list,"open_album",imagesset);
                con.open_playlist_id=((open_playlist)context).current_id;
                con.playsong(getLayoutPosition());

            }else if(id.equals("open_playlist")){
                //con.setsong(getLayoutPosition());
                // con.playsong();
                con.setMylist(songs_list,"open_playlist",imagesset);
                con.open_playlist_id=((open_playlist)context).current_id;
                con.playsong(getLayoutPosition());
                ((open_playlist)context).refreshfab();
            }else if(id.equals("artist")){
                open_artist(false,songs_list.get(getLayoutPosition()).getName());
            }
        }
        public void handleSongsOptions(final View v){
            Log.i("hjgh","handle songs options");
            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.songs_options,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int ids=item.getItemId();
                    switch (ids){
                        case R.id.psongs_play :{
                            if(id.equals("allsongs")){
                                con.setMylist(con.allsonglist,"allsongs",true);
                            }else {
                                con.setMylist(songs_list, id, imagesset);
                            }

                            con.playsong(getLayoutPosition());
                            if(id.equals("song")|| id.equals("allsongs")  ) {
                                face.setcardss(con.getsong());

                            }
                            return true;
                        }
                        case R.id.psongs_add_to_playlist: {
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
                        case R.id.psongs_Rename: {
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
                                            Long idd=songs_list.get(pos).getId();
                                            ContentValues values = new ContentValues();
                                            values.put(MediaStore.Audio.Media.TITLE,str);
                                            songs_list.get(pos).setName(str);
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
        public void handlePlaylistOptions(View v){
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
        public void handle_openPlaylist_options(View v){
            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.open_playlist,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==R.id.open_Remove){
                        // remove song from playlist ... plylst is the interface with activity openplaylist
                        removesongfromplaylist(songs_list.get(getLayoutPosition()).getId(),plylst.getplaylist_id());
                        ((open_playlist)context).refreshNoOfSongs();
                    } else if(item.getItemId()==R.id.open_play){
                        con.setMylist(songs_list,"open_playlist",imagesset);

                        con.playsong(getLayoutPosition());
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
        public void handle_nowPlaying_options(View v){
            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.now_playing_options,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==R.id.now_playing_play){
                        // play song
                        con.playsong(getLayoutPosition());
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
        public void handle_album_options(final View v){

            popup=new PopupMenu(context,v);
            popup.getMenuInflater().inflate(R.menu.album,popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    builder=new AlertDialog.Builder(context);
                    if(item.getItemId()==R.id.album_open){
                        open_album(false,v);
                    } else if(item.getItemId()==R.id.album_playall){
                        open_album(true,v);
                    }else if(item.getItemId()==R.id.album_morefromartist){
                        open_artist(false,songs_list.get(getLayoutPosition()).getArtist());
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
        public void handle_artist_options(final View v){

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

        public void removesongfromplaylist(Long song_id,Long playlist_id){
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
               Toast.makeText(context, "songs removed: " + songs_list.get(getLayoutPosition()).getName(), Toast.LENGTH_LONG).show();
               songs_list.remove(getLayoutPosition());
               notifyItemRemoved(getLayoutPosition());
           }
       }

        public void open_album(Boolean playall,View clickedView){
            Intent intent =new Intent(context,open_playlist.class);
            //using same activity to open playlists and albums

            // for shared transitoin
            // create the transition animation - the images in the layouts
            // of both activities are defined with android:transitionName="playlistTransition"
            try {
                ActivityOptions options = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        ((MainActivity) context).getWindow().setExitTransition(TransitionInflater.from(context).inflateTransition(R.transition.fade_edited));
                        options = ActivityOptions.makeSceneTransitionAnimation((AppCompatActivity) context,
                                Pair.create((View) image, "albumTransition"), Pair.create((View) name, "albumname_transition"),
                                Pair.create((View) artist, "albumartist_transition"),
                                Pair.create(((MainActivity) context).findViewById(android.R.id.navigationBarBackground), Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME),
                                Pair.create(((MainActivity) context).findViewById(android.R.id.statusBarBackground), Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        // for phones which do not have a navigaotion bar
                        options = ActivityOptions.makeSceneTransitionAnimation((AppCompatActivity) context,
                                Pair.create((View) image, "albumTransition"), Pair.create((View) name, "albumname_transition"),
                                Pair.create((View) artist, "albumartist_transition"),
                                Pair.create(((MainActivity) context).findViewById(android.R.id.statusBarBackground), Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)
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
        public void rename_artist(){
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
        public void delete_artist(){
            Log.i("artistedit","delete artist");

            int i=getLayoutPosition();
            //delete all songs of artist
            String[] artistid =new String[]{String.valueOf(songs_list.get(getLayoutPosition()).getId())};
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            int ii=resolver.delete(uri, MediaStore.Audio.Media.ARTIST_ID + " = ? ",artistid);
            if(ii==1){
                Toast.makeText(context," Artist deleted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context,"Oops! Something went wrong.Please try again!",Toast.LENGTH_SHORT).show();

            }
            songs_list.remove(i);
            notifyItemRemoved(i);
            Log.i("artistedit","deleted result="+String.valueOf(ii));

        }
        public void openplaylist(){
            Long id = songs_list.get(getLayoutPosition()).getId();
            Intent intent =new Intent(context,open_playlist.class);
            intent.putExtra("method","playlist");
            intent.putExtra("playlist_name",songs_list.get(getLayoutPosition()).getName());
            intent.putExtra("playlist_id",id);
                context.startActivity(intent);

        }

        public void deleteplaylist(Long playlistid){
            String playlistids = String.valueOf(playlistid);
            String where = MediaStore.Audio.Playlists._ID + "=?";
            String[] whereVal = {playlistids};
            resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
            Toast.makeText(context,songs_list.get(getLayoutPosition()).getName() + " Deleted", Toast.LENGTH_SHORT).show();
            return ;

        }

        public void deleteAlbum(Long _id) {
            String[] albumid =new String[]{String.valueOf(_id)};
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            resolver.delete(uri, MediaStore.Audio.Media.ALBUM_ID + " = ?",albumid);
            Toast.makeText(context,songs_list.get(getLayoutPosition()).getName() + " Deleted", Toast.LENGTH_SHORT).show();
            int i=getLayoutPosition();
            songs_list.remove(i);
        }
        public void deletesong(Long audioid){
            int i = 0;
            try {
                String where = MediaStore.Audio.Playlists.Members._ID + "=?";
                String sid = String.valueOf(audioid);
                String[] whereVal = {sid};

                i = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        where, whereVal);
                Log.i("uiii",String.valueOf(i));


            }catch (Exception e){
                e.printStackTrace();
                Log.i("uiii",e.toString());
            }
            if(i>0){
                Toast.makeText(context, "songs removed: " +songs_list.get(getLayoutPosition()).getName(), Toast.LENGTH_LONG).show();
                songs_list.remove(getLayoutPosition());
                notifyItemRemoved(getLayoutPosition());
            }
        }

        public void add_to_queue(songs song){
            ArrayList<songs> listt =new ArrayList<>();
            listt.add(song);
            con.addSongToList(listt);
        }

        public void shareSong(String data){
            Uri uri= Uri.parse("file:///"+data);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/mp3");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(share, "Share Sound File"));
        }
        }

    public class Animationclass{
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

    public ArrayList<songs> get_playlist(){
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

    public void addnewPlaylistwithSongs(final songs s){
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
    public Long findPlaylistIdByName(String name){

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
    public void createnewplaylist(String playlistname) {
        ContentValues mInserts = new ContentValues();
        mInserts.put(MediaStore.Audio.Playlists.NAME, playlistname);
        mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        context.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
    }
    public void addnewPlaylistwithSongsAsync(final ArrayList<Long> selectedsong_ids){
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

    //-------------------------


    public class background extends AsyncTask<ArrayList<songs>,Void,ArrayList<songs>>{

        @Override
        protected ArrayList<songs> doInBackground(ArrayList<songs>... params) {
            songs current;
            Cursor cursor;
            ArrayList<songs> sng=params[0];

            final String _id = MediaStore.Audio.Albums._ID;
            final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
            String[] projection={ _id,albumart};

            String path=null;

            if(id.equals("open_playlist")  ) {
                Log.i("pwpw", "----------open_playlist");
                Log.i("pwpw", String.valueOf(sng.size())+" songs in playlist");

            }
            for(int i=0;i<sng.size();i++){
                Log.i("pwpw",id+"---"+String.valueOf(i)+"th song image loading..");
                current=sng.get(i);
                try {
                    cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, "_ID=" + current.getAlbum_id(),
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
            return sng;
        }

        @Override
        protected void onPostExecute(ArrayList<songs> songses) {
            super.onPostExecute(songses);

            songs_list=songses;
            imagesset=true;
            if(id.equals(con.currenntlistof) && !con.withimages){
                //earlier string was song song
                con.setMylist(songs_list,id,imagesset);
            }
        }
    }

    public class addPlaylistToQueue extends AsyncTask<Long,Void,Void>{
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

    // action Mode Methods

    public void toggleSelection(int position){
        Log.i("animt","toggle selection");
        selectView(position, !mSelectedItemsIds.get(position));
    }
    public int getSelectedCount(){
    Log.i("contxt","getselectedcount"+String.valueOf(mSelectedItemsIds.size()));
        return mSelectedItemsIds.size();
    }

    //Remove selected selections
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        Log.i("animt","remove selection");

       notifyDataSetChanged();
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

    public void mActionmodeset(boolean b){
        mActionModeSet=b;
        notifyDataSetChanged();

        if(!b) {
            cananimate=false;
            delayedactionmodeset del = new delayedactionmodeset();
            del.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        Log.i("animt","actionmodeset");

    }
    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
        }

    public void addtoplaylist_contextual(){
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
        selectedsong_ids=makeArrayOfidsFromSparseArray(false);

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
        ArrayList<Long> selectedsong_ids =new ArrayList<>();
        currentPlayingSong=con.getsong();
        Log.i("contxt2","delete");
        selectedsong_ids=makeArrayOfidsFromSparseArray(true);

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

    public ArrayList<Long> makeArrayOfidsFromSparseArray(boolean removeFromRecyclerView){
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
    public ArrayList<Uri> makeArrayOfPathUriFromSparseArray(){
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

    public class deletemultiple extends AsyncTask<ArrayList<Long>,Void,Void>{
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
            CoordinatorLayout m=((MainActivity)context).coordinatorlayout;
            Snackbar snack = Snackbar.make(m, "Success :"+String.valueOf(songsdeleted)+"     Failed:"+String.valueOf(failed), Snackbar.LENGTH_LONG);

            snack.show();
        }
    }
    public class addToPaylistMultiple extends AsyncTask<ArrayList<Long>,Void,Void>{
        public boolean currentdeleted=false;
        Long playlistid=new Long(playlistIdForMultipleAdd);
        //songs current_song;

        ProgressDialog progress=new ProgressDialog(context);
        int typeBar=1;        // Determines type progress bar: 0 = spinner, 1 = horizontal
        int success=0;

        @Override
        protected void onPreExecute() {
           //current_song=con.getsong();

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
                addTracksToPlaylist(playlistid,l);
                success++;
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

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
        }
    }

    public class delayedactionmodeset extends AsyncTask<ArrayList<Void>,Void,Void>{
        @Override
        protected Void doInBackground(ArrayList<Void>... arrayLists) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            cananimate=true;
            super.onPostExecute(aVoid);
        }
    }
}
