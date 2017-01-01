package com.example.rahul.myplayer;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class Now_playing extends AppCompatActivity implements ApplicationController.informactivity, View.OnLongClickListener,View.OnClickListener{

    private final int read_external=11001;
    private boolean contextualmode=false;

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
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Now Playing");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recview=(RecyclerView)findViewById(R.id.now_recview);
        mlayoutManager=new LinearLayoutManager(this);
        recview.setLayoutManager(mlayoutManager);
        adapter=new recycler_adapter(this,now_list,"now_playing");
        recview.setAdapter(adapter);

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
        if(contextualmode){
            getMenuInflater().inflate(R.menu.contextual_menu,menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        switch (id){
            case android.R.id.home:{
                if(contextualmode){
                    exitContexualMode();
                }else {
                    finish();
                }
                return true;
            }
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(contextualmode){
            exitContexualMode();
        }else {
            super.onBackPressed();
        }

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


    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    @Override
    public void onClick(View view) {

    }
    public boolean startContextualMode(){
        if(!contextualmode){
            contextualmode=true;
            getSupportActionBar().setTitle("");
            invalidateOptionsMenu();
        }
        return true;
    }
    public void exitContexualMode(){
        contextualmode=false;
        //getSupportActionBar().setTitle("Now Playing");
        //invalidateOptionsMenu();
    }
}
