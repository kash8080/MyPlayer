package com.androidplay.one.myplayer.activities;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.DataFetch;
import com.androidplay.one.myplayer.RealPathUtils;
import com.androidplay.one.myplayer.helper_classes.ThemeHelper;
import com.androidplay.one.myplayer.helper_functions;
import com.androidplay.one.myplayer.songs;

import com.androidplay.one.myplayer.mp3agic_classes.ID3v1;
import com.androidplay.one.myplayer.mp3agic_classes.ID3v2;
import com.androidplay.one.myplayer.mp3agic_classes.ID3v24Tag;
import com.androidplay.one.myplayer.mp3agic_classes.InvalidDataException;
import com.androidplay.one.myplayer.mp3agic_classes.Mp3File;
import com.androidplay.one.myplayer.mp3agic_classes.NotSupportedException;
import com.androidplay.one.myplayer.mp3agic_classes.UnsupportedTagException;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.androidplay.one.myplayer.R;

public class Edit_Info_songs extends AppCompatActivity implements View.OnClickListener{

    ApplicationController con;
    ImageView main_backgroundimage;
    ImageView album_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar;
    EditText name,artist,year,album;
    FloatingActionButton fab;

    SharedPreferences sharedPref;
    String theme_no;
    Boolean dark;
    int img_no;
    ThemeHelper themeHelper;

    String s_id,sname,sartist,syear,salbum,simage,salbumid,sdata;
    TextInputLayout tname,tartist,tyear,talbum;

    Uri newImage;
    int PICK_IMAGE=111;
    int song_pos;

    Boolean isalbum=false;
    Boolean removedImage=false;
    //album vars
    String str_id,str_albumname,str_albumartist,str_year;

    final int case_previousimage=0;
    final int case_nopreviousimage=1;
    final int case_removedimage=2;
    final int case_newImage=3;
    int currentImageCase;

    private final int REQUEST_CODE_OPEN_DOCUMENT_TREE=977;
    Uri sdCardUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        con = new ApplicationController(this.getApplicationContext(), this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        theme_no=sharedPref.getString("THEME_LIST","1") ;
        dark = sharedPref.getBoolean("check", false);
        img_no = sharedPref.getInt("image_chooser", 0);

        themeHelper=new ThemeHelper(this);
        themeHelper.setthemecolours();

        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
        }else{
            if(con.needforpermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                startActivity(new Intent(this, PermissionActivity.class));
                //finish called to stop further proccess of this activity
                finish();
                return;
                //activity trying to restore previous state which is null
                // now because the system terminates the rocess while revoking perissions
            }else{
                super.onCreate(savedInstanceState);
            }
        }
        Intent intent=getIntent();
        isalbum= intent.getBooleanExtra("is_album",false);


        if(isalbum){
            setContentView(R.layout.activity_edit__info_album);
            initialise();

            str_id=intent.getStringExtra("album_id");
            str_albumname=intent.getStringExtra("album_title");
            str_albumartist=intent.getStringExtra("album_artist");
            str_year=intent.getStringExtra("album_year");
            simage=intent.getStringExtra("album_image");

            if(simage!=null && simage.length()>0){
                currentImageCase=case_previousimage;
            }else{
                currentImageCase=case_nopreviousimage;
            }

            Log.i("fgdd","this album id="+salbumid+"--");
            name.setText(str_albumname);
            artist.setText(str_albumartist);
            year.setText(str_year);

            tname.setHint("Album Name");
            tartist.setHint("Album Artist");
            tyear.setHint("Album Year");
        }else{
            setContentView(R.layout.activity_edit__info_songs);
            initialise();

            s_id=intent.getStringExtra("song_id");
            simage=intent.getStringExtra("song_image");
            sname=intent.getStringExtra("song_title");
            sartist=intent.getStringExtra("song_artist");
            salbum=intent.getStringExtra("song_albumname");
            syear=intent.getStringExtra("song_year");
            salbumid=intent.getStringExtra("album_id");
            song_pos=intent.getIntExtra("song_pos",0);
            sdata=intent.getStringExtra("song_data");

            Log.i("fgdd","this album id="+salbumid+"--");
            name.setText(sname);
            artist.setText(sartist);
            year.setText(syear);
            album.setText(salbum);

            tname.setHint("Track Title");
            tartist.setHint("Track Artist");
            tyear.setHint("Year");
            talbum.setHint("Track Album");

        }
        themeHelper.setthemeAndBackground(main_backgroundimage,null);
        if(dark || img_no>=1){
            name.setTextColor(ContextCompat.getColor(this,R.color.colorPrimaryLightText));
            artist.setTextColor(ContextCompat.getColor(this,R.color.colorPrimaryLightText));
            year.setTextColor(ContextCompat.getColor(this,R.color.colorPrimaryLightText));
            if(!isalbum) {
                album.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryLightText));
            }
        }


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");




        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width=displaymetrics.widthPixels;
        int height=displaymetrics.heightPixels;
        album_image.getHeight();
        album_image.getWidth();
        Log.i("edtt","height="+album_image.getHeight()+" width="+album_image.getWidth());
        if(simage!=null ){
            Picasso.with(this)
                    .load(Uri.parse("file://"+simage))
                    .resize(width, height)
                    .centerCrop()
                    .into(album_image,picassoalbumcallback);
        }else{
            Picasso.with(this)
                    .load(R.drawable.testalbum)
                    .resize(width, height)
                    .centerCrop()
                    .into(album_image,picassoalbumcallback);        }

        fab.setOnClickListener(this);

        album_image.setOnClickListener(this);

    }
    private Callback picassoalbumcallback=new Callback() {
        @Override
        public void onSuccess() {
            Log.i("picss","onsuccess");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && album_image.isAttachedToWindow()){
                int cx = album_image.getWidth();
                int cy = (album_image.getHeight() );
                Log.i("edtt","2 height="+album_image.getHeight()+" width="+album_image.getWidth());

                // get the initial radius for the clipping circle
                float finalradius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(album_image, 0, cy, 0,finalradius);
                anim.setDuration(400);
                // start the animation
                album_image.setVisibility(View.VISIBLE);
                anim.start();
            }else{
                album_image.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onError() {
            Log.i("picss","onerror");
            album_image.setVisibility(View.VISIBLE);

        }
    };
    public String getSDCardDirectory(){
        /*String SdcardPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        String dir = SdcardPath.substring(SdcardPath.lastIndexOf('/') + 1);
        System.out.println(dir);
        String[] trimmed = SdcardPath.split(dir);
        String sdcardPath = trimmed[0];
        System.out.println(sdcardPath);
        return sdcardPath;
        */
        /*
        Log.i("strg","getExternalStorageDirecory="+Environment.getExternalStorageDirectory());=/storage/emulated/0
        Log.i("strg","getExternalStorageDirecory.getPath()="+Environment.getExternalStorageDirectory().getPath());=/storage/emulated/0
        Log.i("strg","getExternalStorageDirecory="+Environment.getExternalStorageDirectory().getAbsolutePath());=/storage/emulated/0
        Log.i("strg","getSDCardDirectory="+getSDCardDirectory());=/storage/emulated/0/
        Log.i("strg","getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)="+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));=/storage/emulated/0/Pictures
        Log.i("strg","getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)getAbsolutePath()="+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());=/storage/emulated/0/Pictures
*/
        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/";   //  =/storage/emulated/0/
    }
    private void initialise(){
        main_backgroundimage=(ImageView)findViewById(R.id.edit_background);
        album_image=(ImageView)findViewById(R.id.edit_image);
        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
        fab=(FloatingActionButton)findViewById(R.id.fab_edit);
        toolbar=(Toolbar)findViewById(R.id.MyToolbar);
        name=(EditText)findViewById(R.id.edit_title);
        artist=(EditText)findViewById(R.id.edit_artist);
        year=(EditText)findViewById(R.id.edit_year);

        if(!isalbum) {
            album = (EditText) findViewById(R.id.edit_album);
            talbum=(TextInputLayout)findViewById(R.id.text_input_layout_album);
        }
        tname=(TextInputLayout)findViewById(R.id.text_input_layout_title);
        tartist=(TextInputLayout)findViewById(R.id.text_input_layout_artist);
        tyear=(TextInputLayout)findViewById(R.id.text_input_layout_year);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_info,menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        switch (id){
            case R.id.fab_edit:{
                if(name.getText().toString().length()==0){
                    Toast.makeText(this,"Enter a Title",Toast.LENGTH_SHORT).show();
                }else{
                    if(isalbum){
                        savecurrentAlbuminfo();
                        Toast.makeText(this,"Updated Album Info",Toast.LENGTH_SHORT).show();
                    }else {
                        savecurrentinfo();
                        Toast.makeText(this,"Updated Song Info",Toast.LENGTH_SHORT).show();
                    }
                    //finish();
                }
                break;
            }
            case R.id.edit_image:{

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        con.activityOnResume();
    }

    public void refreshsongdetailsinAllsongsllist(){
        Log.i("fgdd","refresh detais song no="+song_pos+"song id="+s_id);

        String nm=name.getText().toString().trim();
        String albm=album.getText().toString().trim();
        String yr=year.getText().toString().trim();
        String artst=artist.getText().toString().trim();

        ArrayList<songs> list=con.getAllsonglist();
        Log.i("fgdd","refresh detais listsong name="+list.get(song_pos).getName()+" listsong id="+list.get(song_pos).getId());

        if(String.valueOf(list.get(song_pos).getId()).equals(String.valueOf(s_id))){
            Log.i("fgdd","refresh detais equals");
            songs song=list.get(song_pos);
            song.setAlbumName(albm);
            song.setArtist(artst);
            song.setYear(yr);
            song.setName(nm);
        }

    }

    private void savecurrentinfo(){

        if (sdCardUri==null) {
            opendocumentfolder();
        }else{
            setid3image();
        }
        /*
       refreshsongdetailsinAllsongsllist();
        Boolean albumchanged=false;
        ContentValues values=new ContentValues();
        ContentResolver res=getContentResolver();
        Log.i("fgdd","prev album name="+salbum+"--");
        Log.i("fgdd","prev album id="+salbumid+"--");

        values.put(MediaStore.Audio.Media.TITLE,name.getText().toString().trim());
        values.put(MediaStore.Audio.Media.ALBUM,album.getText().toString().trim());
        values.put(MediaStore.Audio.Media.YEAR,year.getText().toString().trim());
        values.put(MediaStore.Audio.Media.ARTIST,artist.getText().toString().trim());

        Log.i("fgdd","id="+s_id);
        int i=res.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values,MediaStore.Audio.Media._ID+" = ? ",
                new String[]{String.valueOf(s_id)});

        //get new Album id
        String newAlbumId=DataFetch.getAlbumIdforSongId(this,s_id);

        switch (currentImageCase){
            case case_newImage:{
                // new album art has been selected
                setnewlySelectedImage(newAlbumId);
                break;
            }
            case case_previousimage:{
                // album id of song is changed .. put previous album art in this album also
                if(simage!=null || simage.length()>0){
                    putImagetosong(newAlbumId,simage);
                }
                break;
            }
            case case_nopreviousimage:{

                break;
            }
            case case_removedimage:{
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri,Long.valueOf(newAlbumId)),null,null);
                break;
            }

        }
*/
    }

    private void opendocumentfolder(){
        // call for document tree dialog
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
    }
    private void savecurrentAlbuminfo(){


        String nm=name.getText().toString().trim();
        String yr=year.getText().toString().trim();
        String artst=artist.getText().toString().trim();

        String song_id=null;
        //get a song from this album to track updated albumid
        Uri songuri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] sel=new String[]{
                MediaStore.Audio.Media._ID
        };
        String where=MediaStore.Audio.Media.ALBUM_ID+" = ? ";
        Cursor cur= getContentResolver().query(songuri,sel,where,new String[]{String.valueOf(str_id)},null);
        if(cur!=null && cur.moveToFirst()){
            //get one song from the album
            song_id=cur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID));
            cur.close();
        }

        ContentValues values=new ContentValues();
        ContentResolver res=getContentResolver();
        Log.i("fgdd","prev album name="+str_albumname+"--");
        Log.i("fgdd","prev album id="+str_id+"--");

        values.put(MediaStore.Audio.Media.ALBUM,nm);
        values.put(MediaStore.Audio.Media.YEAR,yr);
        values.put(MediaStore.Audio.Media.ARTIST,artst);

        Log.i("fgdd","previous id="+str_id);
        int i=res.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values,MediaStore.Audio.Media.ALBUM_ID+" = ? ",
                new String[]{String.valueOf(str_id)});
        Log.i("fgdd","result update i="+i);

        //get new Album id
        String newAlbumId=DataFetch.getAlbumIdforSongId(this,song_id);

        switch (currentImageCase){
            case case_newImage:{
                // new album art has been selected
                setnewlySelectedImage(newAlbumId);
                break;
            }
            case case_previousimage:{
                // album id of song is changed .. put previous album art in this album also
                if(simage!=null || simage.length()>0){
                    putImagetosong(newAlbumId,simage);
                }
                break;
            }
            case case_nopreviousimage:{

                break;
            }
            case case_removedimage:{
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri,Long.valueOf(newAlbumId)),null,null);
                break;
            }

        }

    }
    private void setnewlySelectedImage(String idss){
        Log.i("fgdd","setnewlySelectedImage");

        Log.i("fgdd","new image uri is"+newImage.getPath());
        String path = null;
        if (Build.VERSION.SDK_INT < 19) {
            path = RealPathUtils.getRealPathFromURI_API11to18(this, newImage);
        }else {
            // SDK > 19 (Android 4.4)
            path = RealPathUtils.getRealPathFromURI_API19(this, newImage);
        }
        // Get the file instance
        File file = new File(path);


        Bitmap thumbnail= DataFetch.decodeSampledBitmapFrompath(getResources(),file.getAbsolutePath(),300,300);

        String foldername=getResources().getString(R.string.thumbnailfoldername);
        String thumbnailfolder=getSDCardDirectory()+foldername;
        new File(thumbnailfolder).mkdirs();
        String filename=String.valueOf(System.currentTimeMillis());

        File thumbnailFile = new File(thumbnailfolder,filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(thumbnail!=null && !thumbnail.isRecycled()) {
            thumbnail.recycle();
        }
        String finallocation=thumbnailfolder+"/"+filename;
        putImagetosong(idss,finallocation);

    }
    private void putImagetosong(String id,String imagepath){
        Log.i("fgdd","putNewImagetosong id="+id+" imagepath="+imagepath);

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        int deleted = getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri,Long.valueOf(id)),null,null);
        Log.i("fgdd","deleted album art result="+deleted);
        ContentValues values2 = new ContentValues();
        values2.put("album_id", id);
        values2.put("_data", imagepath);
        Uri num_updates = getContentResolver().insert(sArtworkUri, values2);
        Log.i("fgdd","new album art updaed uri="+num_updates);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.edit_info_removeimage: {
                newImage=null;
                simage=null;
                removedImage=true;
                currentImageCase=case_removedimage;
                Picasso.with(this)
                        .load(R.drawable.testalbum)
                        .resize(1000, 500)
                        .centerCrop()
                        .into(album_image);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            newImage = data.getData();
            currentImageCase = case_newImage;
            String path;
            if (Build.VERSION.SDK_INT < 19) {
                path = RealPathUtils.getRealPathFromURI_API11to18(this, newImage);
            } else {
                // SDK > 19 (Android 4.4)
                path = RealPathUtils.getRealPathFromURI_API19(this, newImage);
            }

            // Get the file instance
            Picasso.with(this)
                    .load(Uri.parse("file://" + path))
                    .error(R.drawable.mp3)
                    .resize(1000, 500)
                    .centerCrop()
                    .into(album_image);

        }
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK) {
            sdCardUri=data.getData();
            Log.i("fgdd","on activity result sdcarduri="+sdCardUri.toString());

            //First we get `DocumentFile` from the `TreeUri` which in our case is `sdCardUri`.
            DocumentFile documentFile = DocumentFile.fromTreeUri(this, sdCardUri);


        }
    }

   //for mp3agic  id3 edit
    private void setid3image(){
        Log.i("fgdd","setid3image sdata ="+sdata);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("fgdd","setid3image does not have permision ");

        }else{
            Log.i("fgdd","setid3image have permision ");

        }

            File f=new File(sdata);
        boolean w=f.setWritable(true);
        Log.i("fgdd","setWritable "+w);
        boolean r=f.setReadable(true);
        Log.i("fgdd","setReadable "+r);
        try {
            Mp3File mp3file=new Mp3File(sdata);

            if(mp3file.hasId3v2Tag()){
                Log.i("fgdd"," have id3v2 tag");

            }else if(mp3file.hasId3v1Tag()){
                Log.i("fgdd"," have id3v1 tag");

            }else if(mp3file.hasCustomTag()){
                Log.i("fgdd"," have custom tag");
            }
            ID3v2 id3v2Tag;
            if(mp3file.hasId3v2Tag()){
                //have id3v2tag ,add image to it
                id3v2Tag = mp3file.getId3v2Tag();
                byte[] imagearray= new byte[0];
                try {
                    imagearray = helper_functions.readBytes(this,newImage);
                    id3v2Tag.setAlbumImage(imagearray,getContentResolver().getType(newImage));
                } catch (IOException e) {
                    Log.i("fgdd","IOException read bytes");
                    Log.i("fgdd",e.toString());
                    e.printStackTrace();
                }

                try {
                    mp3file.save(name.getText().toString().trim());
                } catch (IOException e) {
                    Log.i("fgdd","IOException save");
                    Log.i("fgdd",e.toString());

                    e.printStackTrace();
                }

                Log.i("fgdd","album art changed success");
            }else{
                //dont have id3v2
                id3v2Tag = new ID3v24Tag();

                if(mp3file.hasId3v1Tag()){
                    //copy from id3v1 to new id3v2
                    Log.i("fgdd"," dont have id3v2 but have id3v1 tag");
                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                    mp3file.setId3v2Tag(id3v2Tag);


                    id3v2Tag.setTrack(id3v1Tag.getTrack());
                    id3v2Tag.setArtist(id3v1Tag.getArtist());
                    id3v2Tag.setTitle(id3v1Tag.getTitle());
                    id3v2Tag.setAlbum(id3v1Tag.getAlbum());
                    id3v2Tag.setYear(id3v1Tag.getYear());
                    id3v2Tag.setGenre(id3v1Tag.getGenre());
                    id3v2Tag.setComment(id3v1Tag.getComment());
                    id3v2Tag.setGenreDescription(id3v1Tag.getGenreDescription());
                    //id3v2Tag.setLyrics("Some lyrics");
                    //id3v2Tag.setComposer("The Composer");
                    //id3v2Tag.setPublisher("A Publisher");
                    //id3v2Tag.setOriginalArtist("Another Artist");
                    //id3v2Tag.setAlbumArtist("An Artist");
                    //id3v2Tag.setCopyright("Copyright");
                    //id3v2Tag.setUrl("http://foobar");
                    //id3v2Tag.setEncoder("The Encoder");
                    byte[] imagearray= new byte[0];

                    try {
                        imagearray = helper_functions.readBytes(this,newImage);
                        id3v2Tag.setAlbumImage(imagearray,getContentResolver().getType(newImage));
                    } catch (IOException e) {
                        Log.i("fgdd","IOException read bytes");
                        Log.i("fgdd",e.toString());
                        e.printStackTrace();
                    }
                    try {
                        mp3file.save(name.getText().toString().trim());
                    } catch (IOException e) {
                        Log.i("fgdd","IOException save");
                        Log.i("fgdd",e.toString());

                        e.printStackTrace();
                    }

                }else{
                    Log.i("fgdd"," dont have id3v2 creating new id3v2 tag from edittext values");

                    //create new id3v2 tag and add editext values to it
                    mp3file.setId3v2Tag(id3v2Tag);
                    id3v2Tag.setTitle(name.getText().toString().trim());
                    id3v2Tag.setAlbum(album.getText().toString().trim());
                    id3v2Tag.setArtist(artist.getText().toString().trim());
                    id3v2Tag.setYear(year.getText().toString().trim());

                    byte[] imagearray= new byte[0];
                    try {
                        imagearray = helper_functions.readBytes(this,newImage);
                        id3v2Tag.setAlbumImage(imagearray,getContentResolver().getType(newImage));
                    } catch (IOException e) {
                        Log.i("fgdd","IOException read bytes");
                        Log.i("fgdd",e.toString());
                        e.printStackTrace();
                    }
                    try {
                        mp3file.save(name.getText().toString().trim());
                    } catch (IOException e) {
                        Log.i("fgdd","IOException save");
                        Log.i("fgdd",e.toString());

                        e.printStackTrace();
                    }


                }
            }
        } catch (UnsupportedTagException e) {
            Log.i("fgdd","UnsupportedTagException");
            Log.i("fgdd",e.toString());
            e.printStackTrace();
        } catch (InvalidDataException e) {
            Log.i("fgdd","InvalidDataException");
            Log.i("fgdd",e.toString());
            e.printStackTrace();
        } catch (NotSupportedException e) {
            Log.i("fgdd","NotSupportedException");
            Log.i("fgdd",e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("fgdd",e.toString());
            e.printStackTrace();
        }
    }


}
