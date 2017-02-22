package com.androidplay.one.myplayer.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidplay.one.myplayer.DataFetch;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.activities.MainActivity;
import com.androidplay.one.myplayer.fileUtil;
import com.androidplay.one.myplayer.recycler_adapter;
import com.androidplay.one.myplayer.songs;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul on 10-02-2017.
 */

public class Folders extends Fragment implements recycler_adapter.Folderfrag{

    private ArrayList<songs> list=new ArrayList<>();
    private RecyclerView rec_view;
    private RecyclerView.LayoutManager mlayoutmanager;
    private recycler_adapter adapter;
    public MainActivity mainActivity;
    private ContentResolver resolver;
    private boolean cancelled=false;
    String backfolder;
    ArrayList<String> mainroot=new ArrayList<>();


    private boolean isOnRootFolders=true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("rtgh", "createview:");

        View v=inflater.inflate(R.layout.activity_playlist,container,false);
        resolver = mainActivity.getContentResolver();
        list=new ArrayList<>();
        rec_view=(RecyclerView)v.findViewById(R.id.recview);
        mlayoutmanager=new LinearLayoutManager(getActivity());
        adapter=new recycler_adapter(getActivity(),this,list,"folders");
        rec_view.setLayoutManager(mlayoutmanager);
        rec_view.setAdapter(adapter);

        Log.i("foll","oncreate folder");
        String internal=Environment.getExternalStorageDirectory().getPath();
        Log.i("foll","internal="+internal);

        //getFolders("/");
       getrootfolders();
        return v;

    }
    @Override
    public void onAttach(Context context) {
        Log.i("foll", "onattach:");
        this.mainActivity=(MainActivity) context;
        mainActivity.setFolderfragment(this);
        super.onAttach(context);
    }
    @Override
    public void onResume() {
        Log.i("foll", "onresume:");
        super.onResume();
        // refreshview();
    }
    @Override
    public void getrootfolders(){
        isOnRootFolders=true;
        Log.i("foll","getrootfolders");
        if(fetch!=null){
            fetch.cancel(true);
            fetch.setboolean(false);
        }
        list.clear();

        String[] roots=DataFetch.getStorageDirectories(getActivity());
        for(String s:roots){
            String noOfSongs= fileUtil.getnumOfSongsForFolder(getActivity(),s);
            String par=s.substring(0,s.lastIndexOf("/"))+"/";
            String name=s.substring(s.lastIndexOf("/")+1);
            songs song=new songs();
            song.setName(name);
            song.setData(par);
            Log.i("foll","gte roots par="+par+" name="+name);

            mainroot.add(par);
            song.setIsrootfolder(true);
            //ArrayList<File> filee=new ArrayList<>();
            //listf(s,filee);
            song.setArtist(noOfSongs);
            list.add(song);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void openFolder(String parent,String filename) {
        String str=parent+filename+"/";
        Log.i("foll","");
        Log.i("foll","open folder s="+str+" par="+parent+" filename="+filename);
        backfolder=parent;
        Log.i("foll"," setting backfolder as="+parent);
        openfolder(str);

    }

    @Override
    public void openbackfolder(){
        Log.i("foll"," openbackfolder backfolder+"+backfolder);
        String prevback=backfolder;
        for(String ss:mainroot){
            if(ss.equals(backfolder)){
                getrootfolders();
                return;
            }
        }
        String s=backfolder.substring(0,backfolder.lastIndexOf("/"));
        s=backfolder.substring(0,s.lastIndexOf("/"))+"/";
        Log.i("foll"," setting backfolder as="+s);
        backfolder=s;
        openfolder(prevback);

    }
    asyncFetch fetch;
    public void openfolder(String str){
        isOnRootFolders=false;
        Log.i("foll","open folder s="+str);

        if(fetch!=null){
            fetch.cancel(true);
            fetch.setboolean(false);
        }

        list.clear();


        songs back=new songs();
        back.setName("...");
        back.setIsbackFolder(true);
        list.add(back);

        fetch=new asyncFetch();
        fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,str);

    }

    public boolean getisOnRootFolder(){
        return isOnRootFolders;
    }


    private class asyncFetch extends AsyncTask<String,Void,Void>{

        String str;
        int i=0;
        int totalfolders=0;

        private boolean canrun=true;

        public void setboolean(boolean t){
            Log.i("foll", "setboolean ="+t+"--------------------------------------");
            canrun=t;
        }
        @Override
        protected Void doInBackground(String... voids) {
            str=voids[0];
            File rootdir=new File(str);

            File[] childfiles=rootdir.listFiles();
            if(!(childfiles==null || childfiles.length<1)) {
                // add all folders
                for (File f : childfiles) {
                    if(isCancelled() || !canrun){
                        break;
                    }
                    Log.i("foll", f.getName());
                    Log.i("foll", f.getAbsolutePath());
                    //ArrayList<File> filee=new ArrayList<>();
                    if(f.isDirectory() ) {
                        //listf(f.getAbsolutePath(), filee);
                        String noOfSongs= fileUtil.getnumOfSongsForFolder(getActivity(),f.getAbsolutePath());
                        if (noOfSongs.length()>0){
                            totalfolders++;
                            songs song = new songs();
                            song.setName(f.getName());
                            song.setArtist(noOfSongs);
                            song.setData(str);
                            if(!isCancelled() && canrun){
                                list.add(totalfolders, song);
                            }else{
                                break;
                            }

                            i++;
                            //adapter.notifyItemInserted(list.size()-1);
                        }
                    }
                    if(i>2){
                        publishProgress();
                        i=0;
                    }
                }

                //add all songs
                for (File f : childfiles) {
                    if(isCancelled() || !canrun){
                        break;
                    }
                    Log.i("foll", f.getName());
                    Log.i("foll", f.getAbsolutePath());
                    if(f.isFile()){
                        if(f.getAbsolutePath().endsWith("mp3")) {
                            songs song ;
                            song=DataFetch.findSongForPath(getActivity(),f.getAbsolutePath());
                            if(song!=null) {
                                song.setIsfolder(false);

                                if(!isCancelled() && canrun){
                                    list.add(song);
                                }else{
                                    break;
                                }
                                i++;
                            }
                            //adapter.notifyItemInserted(list.size()-1);
                        }
                    }
                    if(i>20){
                        publishProgress();
                        i=0;
                    }
                }
            }else{
                Log.i("foll","no subdirectories");

            }

            Log.i("foll","no subdirectories");
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if(!isCancelled() && canrun){
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!isCancelled() && canrun){
                adapter.notifyDataSetChanged();
            }

        }
    }

}
