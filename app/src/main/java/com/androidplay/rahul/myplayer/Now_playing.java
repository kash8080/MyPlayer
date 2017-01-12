package com.androidplay.rahul.myplayer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class Now_playing extends AppCompatActivity implements ApplicationController.informactivity{


    Toolbar toolbar;
    RecyclerView recview;
    RecyclerView.LayoutManager mlayoutManager;
    recycler_adapter adapter;
    ArrayList<songs> now_list;
    ApplicationController con;
    ItemTouchHelper ith;
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
            if(con.needforpermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
                startActivity(new Intent(this, MainActivity.class));
                //finish called to stop further proccess of this activity
            }else{
                super.onCreate(savedInstanceState);
            }

        }
        setContentView(R.layout.activity_now_playing);

        now_list=new ArrayList<>();

        if(con.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

        now_list=con.getlist();

        toolbar=(Toolbar)findViewById(R.id.now_toolbar);
        toolbar.setBackgroundColor(con.getPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Now Playing");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recview=(RecyclerView)findViewById(R.id.now_recview);
        mlayoutManager=new LinearLayoutManager(this);
        recview.setLayoutManager(mlayoutManager);
        adapter=new recycler_adapter(this,now_list,"now_playing");
        recview.setAdapter(adapter);
        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller)findViewById(R.id.fast_scroller3);
        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(recview);
        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recview.addOnScrollListener(fastScroller.getOnScrollListener());

        ItemTouchHelper.SimpleCallback _ithCallback=new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,ItemTouchHelper.LEFT ){
            //and in your imlpementaion of
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                int from=viewHolder.getAdapterPosition();
                int to=target.getAdapterPosition();
                int current=con.getCurrentPosition();

                con.notifydatachange(0,from,to);
                if(from<current && to>=current){
                    // current move up
                    current--;
                    con.setCurrent_pos(current);
                }
                else if(from>current && to<=current) {
                    //  current  move down
                    current++;
                    con.setCurrent_pos(current);

                }else if(from==current){
                    //current = to
                    current=to;
                    con.setCurrent_pos(current);

                }
            adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());


                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int i=viewHolder.getAdapterPosition();
               if(direction==4) {
                   adapter.notifyItemRemoved(i);
                   con.remove_song(i);
               }
            }
        };




         ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(recview);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.now_playing_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        switch (id){
            case android.R.id.home:{
                    finish();
                    break;
                }
            case R.id.nowplayingmenu_removeall:{
                AlertDialog.Builder builder =new AlertDialog.Builder(this);

                builder.setMessage("Are you sure you want to clear the queue?");
                builder.setPositiveButton(
                        "yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                adapter.songs_list=new ArrayList<>();
                                con.setMylist(new ArrayList<songs>(),"queue cleared",false);
                                adapter.notifyDataSetChanged();
                            }
                        });

                builder.setNegativeButton(
                        "no",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog=builder.create();
                dialog.show();

            }
            default:return super.onOptionsItemSelected(item);
        }
        return true;
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




}
