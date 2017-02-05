package com.androidplay.rahul.myplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;

public class settings extends AppCompatActivity {

    Toolbar toolbar;
    static Context  context;
    ApplicationController con;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        con=new ApplicationController(this.getApplicationContext(),this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String thme=sharedPref.getString("THEME_LIST","1") ;
        switch (thme){
            case "1":setTheme(R.style.AppTheme);break;
            case "2":setTheme(R.style.AppTheme_Purple);break;
            case "3":setTheme(R.style.AppTheme_Red);break;
            case "4":setTheme(R.style.AppTheme_orange);break;
            case "5":setTheme(R.style.AppTheme_indigo);break;
            case "6":setTheme(R.style.AppTheme_brown);break;
            default:setTheme(R.style.AppTheme);break;
        }

        if(savedInstanceState==null){
            super.onCreate(savedInstanceState);
        }else{
            if(con.needforpermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                super.onCreate(new Bundle());
                startActivity(new Intent(this, PermissionActivity.class));
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
            PreferenceManager.setDefaultValues(context,R.xml.settings,false);
            if(sharedPref.getBoolean("check",false)) {
                connectionPref.setSummary("click to disable Night mode");
            } else {
                connectionPref.setSummary("click to enable Night mode");
            }
            int img=sharedPref.getInt("image_chooser",0);
            Log.i("settn","current value="+img);

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i("sttng","main pref changed");
           if (key.equals("check")) {
                Preference connectionPref = findPreference("check");
                        // Set summary to be the user-description for the selected value
               if(sharedPreferences.getBoolean("check",false)) {
                   connectionPref.setSummary("click to disable Night mode");
               } else {
                   connectionPref.setSummary("click to enable Night mode");
               }
            }else if (key.equals("THEME_LIST")) {
                ((settings)context).toolbar.setBackgroundColor(((settings)context).con.getPrimary());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((settings)context).getWindow().setStatusBarColor((((settings)context).con.getPrimaryDark()));
                }

            }else if (key.equals("image_chooser")) {
               Log.i("settn","images changed");

               int no=sharedPreferences.getInt("image_chooser",0);
               if(no>=1) {
                   SharedPreferences.Editor editor = sharedPreferences.edit();
                   editor.putString("THEME_LIST", "1");
                   editor.commit();
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
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}
