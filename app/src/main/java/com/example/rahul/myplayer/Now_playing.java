package com.example.rahul.myplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        Log.i("lllll","--");

        now_list=new ArrayList<>();
        Log.i("lllll","--");

        con=new ApplicationController(this.getApplicationContext(),this);
        Log.i("lllll","--");

        now_list=con.getlist();
        Log.i("lllll","--");
        Log.i("lllll","now playing..list size="+String.valueOf(now_list.size()));
        toolbar=(Toolbar)findViewById(R.id.now_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Now Playing");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.i("lllll","--");
        recview=(RecyclerView)findViewById(R.id.now_recview);
        mlayoutManager=new LinearLayoutManager(this);Log.i("lllll","--");
        recview.setLayoutManager(mlayoutManager);Log.i("lllll","--");
        adapter=new recycler_adapter(this,now_list,"now_playing");Log.i("lllll","--");
        recview.setAdapter(adapter);Log.i("lllll","--");

        ItemTouchHelper.Callback _ithCallback=new ItemTouchHelper.Callback(){
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

               // Collections.swap(now_list, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                // and notify the adapter that its dataset has changed
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());


                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Log.i("wewe","onswipe: "+String.valueOf(direction));
                now_list.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                //TODO
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }
        };







         ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(recview);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        switch (id){
            case android.R.id.home:{
                finish();
                return true;
            }
            default:return super.onOptionsItemSelected(item);
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


}
