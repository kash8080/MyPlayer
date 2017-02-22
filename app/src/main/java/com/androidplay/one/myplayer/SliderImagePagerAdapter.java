package com.androidplay.one.myplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.androidplay.one.myplayer.fragments.ImageFragment;

/**
 * Created by Rahul on 17-02-2017.
 */

public class SliderImagePagerAdapter extends FragmentStatePagerAdapter {

    public int noOfSongs=1;

    @Override
    public int getItemPosition(Object object) {
        //to reload everything after calling notifydatasetchanged
        return POSITION_NONE;
    }

    public SliderImagePagerAdapter(FragmentManager fm) {
        super(fm);

    }
    public void setcount(int i){
        Log.i("newvpgr","SliderImagePagerAdapter setcount noOfSongs="+i);
        noOfSongs=i;
    }
    @Override
    public Fragment getItem(int position) {

        Log.i("newvpgr","SliderImagePagerAdapter getItem position="+position);

        ImageFragment myFragment = new ImageFragment();

        Bundle args = new Bundle();
        args.putInt("current_song_pos", position);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public int getCount() {
        //Log.i("newvpgr","SliderImagePagerAdapter getCount");
        return noOfSongs;
    }
}
