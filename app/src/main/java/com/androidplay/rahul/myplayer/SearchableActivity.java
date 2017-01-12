package com.androidplay.rahul.myplayer;

import android.Manifest;
import android.app.SearchManager;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Inflater;

public class SearchableActivity extends AppCompatActivity implements ApplicationController.informactivity{

    List<songs> search_result_list;
    Toolbar toolbar ;
    RecyclerView rec_view;
    RecyclerView.LayoutManager linearmanager;
    Recycleradapter adapter;
    ApplicationController con;
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

    @Override
    public void playnextsong() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void updateprofileimage() {

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

            public viewholder(View itemView) {
                super(itemView);
                title=(TextView)itemView.findViewById(R.id.songs_name);
                artist=(TextView)itemView.findViewById(R.id.songs_artist);
                imageView=(ImageView)itemView.findViewById(R.id.songs_image);
                options=(ImageView)itemView.findViewById(R.id.options);
                options.setVisibility(View.INVISIBLE);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
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
}
