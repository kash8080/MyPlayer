package com.example.rahul.myplayer;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class settings extends AppCompatActivity  implements ApplicationController.informactivity {

    Toolbar toolbar;
    static Context  context;
    ApplicationController con;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        con=new ApplicationController(this.getApplicationContext(),this);

        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
        }else{
            if(con.needforpermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                startActivity(new Intent(this, MainActivity.class));
            }else{
                super.onCreate(savedInstanceState);
            }

        }

        setContentView(R.layout.activity_settings);
        context=this;

        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_main, new pref_fragment())
                .commit();



    }

    @Override
    public void playnextsong() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void updateprofileimage() {

    }

    public static class pref_fragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);

            // to set summery before loading as per the current value and not the default one
            Preference connectionPref = findPreference("check");
            // Set summary to be the user-description for the selected value
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

            if(sharedPref.getBoolean("check",false)) {
                connectionPref.setSummary("click to disable push notifications");
            } else {
                connectionPref.setSummary("click to enable push notifications");
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i("sttng","main pref changed");
            if (key.equals("check")) {
                Preference connectionPref = findPreference("check");
                        // Set summary to be the user-description for the selected value
                if(sharedPreferences.getBoolean(key,false)) {
                    //connectionPref.setSummary(String.valueOf(sharedPreferences.getBoolean(key, false)));
                    connectionPref.setSummary("click to disable push notifications");
                } else {
                    connectionPref.setSummary("click to enable push notifications");
                }

            }


        }

        @Override
        public void onResume() {
            super.onResume();
            PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            super.onPause();
            PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);

        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()== android.R.id.home){
            finish();
        }

        return true;
    }





}
