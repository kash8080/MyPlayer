package com.androidplay.rahul.myplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Rahul on 14-07-2016.
 */
public class pageradapter extends FragmentStatePagerAdapter {

    public pageradapter(FragmentManager fm) {
        super(fm);
    }
    ArrayList<Fragment> list=new ArrayList<>();

    public void addFragment(Fragment f){
        list.add(f);
    }

    public Fragment getFragment(int position){
        return list.get(position);
    }
    @Override
    public Fragment getItem(int position) {

        return list.get(position);
       /*if(position==0) return new home();
        else if(position==1) return new Albums();
       else return new playlist();*/
    }
    public void refreshFragment(int i){
        switch (i){
            case 0:list.set(0,new home());
            case 1:list.set(1,new Albums());
            case 2:list.set(2,new playlist());
            case 3:list.set(2,new Artist());
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
       switch (position){
           case 0: return "All tracks";
           case 1: return "Albums";
           case 2: return "Playlists";
           case 3: return "Artists";
           case 4: return "Playlists";
           default: return "";


       }
    }
}
