package com.example.rahul.myplayer;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

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

    public boolean imagesset=false;


    public interface adaptr{
        public void setcardss(songs song);
    }
    public interface playlist_data{
        public Long getplaylist_id();
    }
    playlist_data plylst;

    public recycler_adapter(Context context,ArrayList<songs> list,String id) {
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

        if(!(id.equals("allsongs")||id.equals("now_playing"))) {
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
    public void onBindViewHolder(viewholder holder, int position) {

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
                        holder.image.setImageResource(R.drawable.mp3);}

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

    }

    @Override
    public int getItemCount() {
        return songs_list.size();
    }

    public class viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView image;
        TextView name;
        TextView artist;
        ImageView options;

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
            }

            options.setOnClickListener(this);

        }

        @Override
        public void onClick(final View v) {

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
        /////-----------
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
                con.setMylist(songs_list,"song",imagesset);
            }
        }
    }

}
