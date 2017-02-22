package com.androidplay.one.myplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.androidplay.one.myplayer.fragments.Albums;
import com.androidplay.one.myplayer.fragments.Artist;
import com.androidplay.one.myplayer.fragments.Folders;
import com.androidplay.one.myplayer.fragments.home;
import com.androidplay.one.myplayer.fragments.playlist;

import java.util.ArrayList;

/**
 * Created by Rahul on 14-07-2016.
 */
public class pageradapter extends FragmentStatePagerAdapter {

    public pageradapter(FragmentManager fm) {
        super(fm);

    }

    /*public void addFragment(Fragment f){
        list.add(f);
    }
*/

    @Override
    public Fragment getItem(int position) {
        Log.i("artist","get item pageradapter");

           switch (position){
               case 0:{
                   return new home();
               }
               case 1:{
                   return new Albums();
               }
               case 2:{
                   return new playlist();
               }
               case 3:{
                   return new Folders();
               }
               case 4:{
                  return new Artist();
               }
               default: return new home();
           }


       /*if(position==0) return new home();
        else if(position==1) return new Albums();
       else return new playlist();*/
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
       switch (position){
           case 0: return "Tracks";
           case 1: return "Albums";
           case 2: return "Playlists";
           case 3: return "Folders";
           case 4: return "Artists";
           default: return "";

       }
    }
}
