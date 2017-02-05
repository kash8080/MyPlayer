package com.androidplay.rahul.myplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

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
        Log.i("artist","get fragment pageradapter");
        return list.get(position);
    }
    @Override
    public Fragment getItem(int position) {
        Log.i("artist","get item pageradapter");

           switch (position){
               case 0:{
                   if(list.get(0)==null){
                       list.set(0,new home());
                       return list.get(0);
                   }else{
                       return list.get(0);
                   }

               }
               case 1:{
                   if(list.get(1)==null){
                       list.set(1,new Albums());
                       return list.get(1);
                   }else{
                       return list.get(1);
                   }
               }
               case 2:{
                   if(list.get(2)==null){
                       list.set(2,new playlist());
                       return list.get(2);
                   }else{
                       return list.get(2);
                   }
               }
               case 3:{
                   if(list.get(3)==null){
                       list.set(3,new Artist());
                       return list.get(3);
                   }else{
                       return list.get(3);
                   }
               }
                default: return new home();
           }


       /*if(position==0) return new home();
        else if(position==1) return new Albums();
       else return new playlist();*/
    }
    public void refreshFragment(int i){
        switch (i){
            case 0:list.set(0,new home());
            case 1:list.set(1,new Albums());
            case 2:{
                try{
                    if((playlist)list.get(2)!=null &&((playlist)list.get(2)).context!=null) {
                        ((playlist) list.get(2)).refreshview();
                    }
                }catch (Exception e){}
            }
            case 3:list.set(3,new Artist());
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
