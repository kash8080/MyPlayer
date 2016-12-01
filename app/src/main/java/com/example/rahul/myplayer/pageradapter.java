package com.example.rahul.myplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Rahul on 14-07-2016.
 */
public class pageradapter extends FragmentStatePagerAdapter {

    public pageradapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
       if(position==0) return new home();
        else if(position==1) return new Albums();
       else return new playlist();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
       switch (position){
           case 0: return "All tracks";
           case 1: return "Albums";
           case 2: return "Playlists";
           case 3: return "playlists";
           case 4: return "Playlists";
           default: return "";


       }
    }
}
