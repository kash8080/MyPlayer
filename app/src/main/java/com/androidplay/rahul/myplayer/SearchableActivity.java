package com.androidplay.rahul.myplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Inflater;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class SearchableActivity extends AppCompatActivity {

    List<songs> search_result_list;
    Toolbar toolbar ;
    RecyclerView rec_view;
    RecyclerView.LayoutManager linearmanager;
    Recycleradapter adapter;
    ApplicationController con;
    PopupMenu popup;
    AlertDialog.Builder builder;

    VerticalRecyclerViewFastScroller fastScroller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        con=new ApplicationController(this.getApplicationContext(),this);

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
                super.onCreate(savedInstanceState);
            }
        }

        setContentView(R.layout.activity_now_playing);
        handleIntent(getIntent());

        toolbar=(Toolbar)findViewById(R.id.now_toolbar);
        toolbar.setBackgroundColor(con.getPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(String.valueOf(search_result_list.size())+" songs found");
        refreshview();
    }

    public void refreshview(){
        rec_view=(RecyclerView)findViewById(R.id.now_recview);
        linearmanager=new LinearLayoutManager(this);
        adapter=new Recycleradapter();

        rec_view.setLayoutManager(linearmanager);
        rec_view.setAdapter(adapter);
        fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller3);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(rec_view);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        rec_view.addOnScrollListener(fastScroller.getOnScrollListener());
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
                android.provider.MediaStore.Audio.Media.ARTIST};
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


            //add songs to list
            do {
                Long albumid=musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);

                search_result_list.add(new songs(thisId, thisTitle, thisArtist,"",albumid));

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

        }

        @Override
        public int getItemCount() {
            return search_result_list.size();
        }

        public class viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{

            ImageView imageView;
            TextView title;
            TextView artist;
            ImageView options;
            ContentResolver resolver=SearchableActivity.this.getContentResolver();
            public viewholder(View itemView) {
                super(itemView);
                title=(TextView)itemView.findViewById(R.id.songs_name);
                artist=(TextView)itemView.findViewById(R.id.songs_artist);
                imageView=(ImageView)itemView.findViewById(R.id.songs_image);
                options=(ImageView)itemView.findViewById(R.id.options);
                options.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case (R.id.options): {
                        handleOptions(view);
                        break;
                    }
                    default:{
                        Long id=search_result_list.get(getLayoutPosition()).getId();
                        songs song=ApplicationController.getSongById(id);
                        if(song.getId().equals(0L)){
                            Toast.makeText(SearchableActivity.this,"Error!",Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            ArrayList<songs> list = new ArrayList<>();
                            list.add(song);
                            ApplicationController.setMylist(list, "", false);
                            ApplicationController.playsong(0);
                            startActivity(new Intent(SearchableActivity.this, playerr.class));
                        }
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
                                con.playsong(getLayoutPosition());
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
                            case R.id.psongs_playnext: {
                                con.addSongtoNextPos(search_result_list.get(getLayoutPosition()));
                                return true;
                            }
                            case R.id.psongs_addtoqueue: {
                                add_to_queue(search_result_list.get(getLayoutPosition()));
                                return true;
                            }
                        }
                        return true;
                    }
                });
                popup.show();


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

                    i = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            where, whereVal);
                    Log.i("uiii",String.valueOf(i));


                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("uiii",e.toString());
                }
                if(i>0){
                    Toast.makeText(SearchableActivity.this, "songs removed: " +search_result_list.get(getLayoutPosition()).getName(), Toast.LENGTH_LONG).show();
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

        }
    }
}
