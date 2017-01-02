package com.example.rahul.myplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

            con.playsong(0);
        }else {
            this.id = id;
        }
        Log.i("llll", "--");

        //load images for every song list except allsongs
        // which is already loaded with images in application controller
        if(!(id.equals("allsongs")||id.equals("now_playing") || id.equals("playlist"))) {
            background back1 = new background();
            Log.i("llll", "--");
            back1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,songs_list);
            Log.i("llll", "--");
        }

        if(id.equals("allsongs") ||id.equals("playlist") ||id.equals("album") ) {
            face = (adaptr) context;
            Log.i("llll", "--");
        }
        if(id.equals("open_playlist")  ) {
            plylst = (playlist_data) context;
            Log.i("llll", "--");
        }


        Log.i("llll", "construcor adapter end"+id);
        resolver=context.getContentResolver();
    }


    @Override
    public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=null;
         if(id.equals("album")){
             Log.i("llll","album");
            view= LayoutInflater.from(context).inflate(R.layout.album_row,parent,false);
        }else{Log.i("llll","else");
            view= LayoutInflater.from(context).inflate(R.layout.custom_row,parent,false);}


        viewholder vh= new viewholder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final viewholder holder, final int position) {
        Log.i("animt","onBindView at pos"+String.valueOf(position));

        songs current=songs_list.get(position);
        String path;
        if( id.equals("allsongs")){
             path=con.getimagepathforsong(position);
        }else {
             path = current.getImagepath();
        }
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path);

            if(bitmap!=null){
                Log.i("llll", "bitmap!null");
                holder.image.setImageBitmap(bitmap);}
            else {
                Log.i("llll", "bitmap null");

                if(id.equals("playlist")){
                    Log.i("llll", "playlist");
                    holder.image.setImageResource(R.drawable.playlist1);
                }else{
                    if(id.equals("album")){
                        holder.image.setImageResource(R.drawable.mp3full);
                    }else{
                        if(id.equals("open_album")){
                            holder.image.setImageResource(R.drawable.album);
                        } else {

                            holder.image.setImageResource(R.drawable.mp3);
                        }
                     }
                }
            }
            if(id.equals("open_album")){
                holder.image.setImageResource(R.drawable.album);
            }
        } catch (Exception e) {

            Log.i("llll", "fffff");
        }
        holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.name.setText(current.getName());
        holder.artist.setText(current.getArtist());

        if(!id.equals("album")) {

            holder.contextual_colour.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x55aaaaaa
                    : Color.TRANSPARENT);
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
//0x9934B5E4
    @Override
    public int getItemCount() {
        return songs_list.size();
    }

    public class viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView image;
        TextView name;
        TextView artist;
        ImageView options;
        LinearLayout contextual_colour;
        View item;

        public viewholder(View itemView) {
            super(itemView);
            item=itemView;

            if(id.equals("album")){
                name=(TextView)itemView.findViewById(R.id.album_name);
                artist=(TextView)itemView.findViewById(R.id.album_artist);
                options=(ImageView)itemView.findViewById(R.id.album_options);
                image=(ImageView)itemView.findViewById(R.id.album_image);
                image.setOnClickListener(this);
            }else{

                name=(TextView)itemView.findViewById(R.id.songs_name);
                artist=(TextView)itemView.findViewById(R.id.songs_artist);
                options=(ImageView)itemView.findViewById(R.id.options);
                image=(ImageView)itemView.findViewById(R.id.songs_image);
                itemView.setOnClickListener(this);
                contextual_colour=(LinearLayout)itemView.findViewById(R.id.backgroundcolour);
            }

            options.setOnClickListener(this);

        }

        @Override
        public void onClick(final View v) {

            if(mActionModeSet){
                //this prevents song playing on double tapping in actionmode
                return;
            }
            Log.i("clickedd","onClick");
            if(v.getId()==R.id.options){
                if(id.equals("song") || id.equals("open_album")|| id.equals("allsongs")){
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
                                               if(playlist.getName().equals(item.getTitle())){
                                                   addTracksToPlaylist(playlist.getId(),songs_list.get(getLayoutPosition()));
                                                   Toast.makeText(context,"added "+songs_list.get(getLayoutPosition()).getName()+" to "+playlist.getName(),Toast.LENGTH_LONG).show();
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
                                case R.id.psongs_playnext: {
                                    con.addSongtoNextPos(songs_list.get(getLayoutPosition()));
                                    return true;
                                }
                            }
                            return true;
                        }
                    });
                    popup.show();


                }else if(id.equals("playlist")){
                    /// ppopup menu for playlists options
                    popup=new PopupMenu(context,v);
                    popup.getMenuInflater().inflate(R.menu.playlist_options,popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId()==R.id.Oplaylist_open){
                                    openplaylist();
                            } else if(item.getItemId()==R.id.Oplaylist_delete){
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
                                alert.show();
                            }
                            return true;
                        }
                    });
                    popup.show();
                }else if(id.equals("open_playlist")){
                    /// ppopup menu for playlists options
                    popup=new PopupMenu(context,v);
                    popup.getMenuInflater().inflate(R.menu.open_playlist,popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId()==R.id.open_Remove){
                                // remove song from playlist ... plylst is the interface with activity openplaylist
                                removesongfromplaylist(songs_list.get(getLayoutPosition()).getId(),plylst.getplaylist_id());

                            } else if(item.getItemId()==R.id.open_play){
                                con.setMylist(songs_list,"open_playlist",imagesset);

                                con.playsong(getLayoutPosition());
                            }
                            return true;
                        }
                    });
                    popup.show();
                }else if(id.equals("now_playing")){
                    /// ppopup menu for playlists options
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
                            }
                            return true;
                        }
                    });
                    popup.show();
                }


            }else if(v.getId()==R.id.album_options) {

                popup=new PopupMenu(context,v);
                popup.getMenuInflater().inflate(R.menu.album,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.album_open){
                           open_album("false");
                        } else if(item.getItemId()==R.id.album_playall){
                            open_album("true");

                        }
                        return true;
                    }
                });
                popup.show();


            }else if(v.getId()==R.id.album_image){
                open_album("false");
            }else
            // whole item click for song selection
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

                    con.playsong(getLayoutPosition());

                }else if(id.equals("open_playlist")){
                    //con.setsong(getLayoutPosition());
                    // con.playsong();
                        con.setMylist(songs_list,"open_playlist",imagesset);

                    con.playsong(getLayoutPosition());

                }
            }

        /////-----------


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

        public void open_album(String playall){
            Intent intent =new Intent(context,open_playlist.class);
            //using same activity to open playlists and albums
            intent.putExtra("method","album");
            intent.putExtra("album_art",songs_list.get(getLayoutPosition()).getImagepath());
            intent.putExtra("album_name",songs_list.get(getLayoutPosition()).getName());
            intent.putExtra("album_playall",playall);
            intent.putExtra("album_id",songs_list.get(getLayoutPosition()).getId());////////////////////
            context.startActivity(intent);
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

        CharSequence colors[]=new String[playlist_list.size()];
        for(int i=0;i<playlist_list.size();i++){
            String s=playlist_list.get(i).getName();
            colors[i]=(CharSequence) s;
        }
        builder = new AlertDialog.Builder(context);
        if(playlist_list.size()==0){
            builder.setTitle("No Playlists found");
        }else{
            builder.setTitle("Choose a Playlist");

        }
        final ArrayList<Long> selectedsong_ids ;
        selectedsong_ids=makeArrayOfidsFromSparseArray(false);

        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                Long l=playlist_list.get(which).getId();
                playlistIdForMultipleAdd=l;
                addToPaylistMultiple addtoPlaylist=new addToPaylistMultiple();
                addtoPlaylist.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,selectedsong_ids);

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
