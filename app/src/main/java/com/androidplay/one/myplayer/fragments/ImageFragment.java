package com.androidplay.one.myplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.songs;
import com.squareup.picasso.Picasso;

/**
 * Created by Rahul on 17-02-2017.
 */

public class ImageFragment extends Fragment {

    ApplicationController con;
    int position=-1;
    ImageView image;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i("newvpgr","on create");

        super.onCreate(savedInstanceState);
        if(getArguments()!=null) {
            Log.i("newvpgr","getArguments!=null");

            position = getArguments().getInt("current_song_pos");
        }else{
            Log.i("newvpgr","getArguments==null");

        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        con=new ApplicationController(getActivity().getApplicationContext(),getActivity());

        Log.i("newvpgr","on createview");
        View v=inflater.inflate(R.layout.fragment_image,container,false);
        image=(ImageView) v.findViewById(R.id.viewpagerimage);
        if(position!=-1) {
            Log.i("newvpgr","position="+position);

            songs song = con.getlist().get(position);
            String imagepath=song.getImagepath();
            if(imagepath!=null && imagepath.length()>0) {
                Picasso.with(getActivity())
                        .load(Uri.parse("file://" + imagepath))
                        .error(R.drawable.testalbum)
                        .into(image);
            }else{
                Picasso.with(getActivity())
                        .load(R.drawable.guitar)
                        .error(R.drawable.testalbum)
                        .into(image);
            }
        }else{
            Log.i("newvpgr","position=-1");

        }
        return v;
    }

}
