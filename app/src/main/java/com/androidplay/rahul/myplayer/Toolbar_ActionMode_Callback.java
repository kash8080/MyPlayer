package com.androidplay.rahul.myplayer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
* Created by Rahul on 31-12-2016.
*/

public class Toolbar_ActionMode_Callback implements ActionMode.Callback {

private Context context;
private recycler_adapter recyclerView_adapter;
private ArrayList<songs> songs_list;
    MainActivity main;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public Toolbar_ActionMode_Callback(Context context, recycler_adapter recyclerView_adapter, ArrayList<songs> message_models) {
    this.context = context;
    this.recyclerView_adapter = recyclerView_adapter;
    this.songs_list = message_models;
    main=(MainActivity)context;
}

@Override
public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    String currentDateandTime = sdf.format(new Date());
    Log.i("colortiming","oncreate "+currentDateandTime);
    mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);//Inflate the menu over action mode
     currentDateandTime = sdf.format(new Date());
    Log.i("colortiming","oncreate end"+currentDateandTime);

    return true;
}

@Override
public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return true;
}

@Override
public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    Fragment f=main.getFragment(0);
    ((home)f).canremoveSelection=false;
    switch (item.getItemId()) {
       case R.id.contextual_addtoqueue:
           ((home)f).addtoqueue_contextual();
           mode.finish();//Finish action mode
           break;
       case R.id.contextual_delete:
           ((home)f).delete_contextual();
           mode.finish();//Finish action mode
            break;
        case R.id.contextual_addtoplaylist:
            ((home)f).addtoplaylist_contextual();
            mode.finish();//Finish action mode
            break;
        case R.id.contextual_share:
            ((home)f).share_contextual();
            mode.finish();//Finish action mode
            break;
    }
    return false;
}

@Override
public void onDestroyActionMode(ActionMode mode) {
    String currentDateandTime = sdf.format(new Date());
    Log.i("colortiming","ondestroy actionmode "+currentDateandTime);
    //When action mode destroyed remove selected selections and set action mode to null
    //First check current fragment action mode
    //recyclerView_adapter.removeSelection();// remove selection
    Fragment recyclerFragment = main.getFragment(0);//Get recycler fragment
    if (recyclerFragment != null) {
        //((home) recyclerFragment).removeSelection();
        ((home) recyclerFragment).setNullToActionMode();//Set action mode null
    }
    currentDateandTime = sdf.format(new Date());
    Log.i("colortiming","ondestroy actionmode end"+currentDateandTime);
    }


}