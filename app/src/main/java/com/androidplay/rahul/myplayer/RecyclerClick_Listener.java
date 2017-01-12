package com.androidplay.rahul.myplayer;

import android.view.View;

/**
 * Created by Rahul on 31-12-2016.
 */


public interface RecyclerClick_Listener {
    void onClick(View view, int position);
    void onLongClick(View view, int position);
}