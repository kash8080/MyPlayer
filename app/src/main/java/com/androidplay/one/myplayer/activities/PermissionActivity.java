package com.androidplay.one.myplayer.activities;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.Manifest;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.fragments.askPermission;
import com.androidplay.one.myplayer.fragments.launcher;

public class PermissionActivity extends AppCompatActivity {
    public final int read_external = 11001;
    Button button;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment launch;
    ApplicationController con;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("llllp","permission on create");


        super.onCreate(savedInstanceState);
        con=new ApplicationController(this.getApplicationContext(),this);

         launch = new launcher();
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(android.R.id.content, launch, "HELLO");
        fragmentTransaction.commit();

        if (ContextCompat.checkSelfPermission(PermissionActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){


             ///replace fragment
                Fragment perm = new askPermission();
                fragmentTransaction = fragmentManager.beginTransaction();
                //// android.R.content used to place fragment directly to parent container
                fragmentTransaction.replace(android.R.id.content, perm, "HELLO");
                fragmentTransaction.commit();
        }else{

                Thread t1 =new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startact();
                            }
                        });
                    }
                });

           t1.start();
        }
    }

    @Override
    public void onRequestPermissionsResult ( int requestCode,
    String permissions[],int[] grantResults){
        switch (requestCode) {
            case read_external: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        ) {

                    if(con.musicSrv==null){
                        con.setservice(this);
                    }

                    // permission was granted,
                    ApplicationController.readlistfrommemory();
                    ApplicationController.restoresongvalue();
                    startact();
                } else {
                    // permission denied,
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void startact() {
        Intent intent = new Intent(this, MainActivity.class);
        ActivityOptions options;
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        con.activityOnResume();
    }
}
