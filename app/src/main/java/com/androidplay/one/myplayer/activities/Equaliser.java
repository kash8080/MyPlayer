package com.androidplay.one.myplayer.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.helper_classes.VerticalSeekBar;

import java.util.ArrayList;

public class Equaliser extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener ,
        CompoundButton.OnCheckedChangeListener {

    Button presetbutton;
    Button reverbbutton;
    ApplicationController con;
    AlertDialog.Builder builder;
    SeekBar seek1;
    SeekBar seek2;
    SeekBar seek3;
    SeekBar seek4;
    SeekBar seek5;
    SeekBar bassboost;

    TextView seek1text;
    TextView seek2text;
    TextView seek3text;
    TextView seek4text;
    TextView seek5text;

    SwitchCompat switchCompat;
    Toolbar toolbar;

    short numberoffreqBands;
    Equalizer equalizer;
    short upperbandlevel,lowerbandlevel;
    ArrayList<Short> reverbIds=new ArrayList<>();
    //BassBoost bassBoost;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        con=new ApplicationController(this.getApplicationContext(),this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equaliser);

        equalizer=con.getEqualiser();
        initialise();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Equaliser");
        presetbutton.setOnClickListener(this);
        reverbbutton.setOnClickListener(this);

        toolbar.setBackgroundColor(con.getPrimary());

        reverbbutton.setTextColor(0xffffffff);
        presetbutton.setTextColor(0xffffffff);

        if(equalizer!=null){
            setEqualiser();
        }


        player=con.getmediaplayer();
        bassboost.setMax(1000);
        refreshSwitch();
    }

    public void refreshSwitch(){
        equalizer=con.getEqualiser();
        if(equalizer==null){
            switchCompat.setChecked(false);
        }else{
            switchCompat.setChecked(true);
        }

    }
    public void initialise(){
        presetbutton=(Button)findViewById(R.id.equaliser_preset);
        reverbbutton=(Button)findViewById(R.id.equaliser_reverb);
        seek1=(VerticalSeekBar) findViewById(R.id.seekbar1);
        seek2=(VerticalSeekBar) findViewById(R.id.seekbar2);
        seek3=(VerticalSeekBar) findViewById(R.id.seekbar3);
        seek4=(VerticalSeekBar) findViewById(R.id.seekbar4);
        seek5=(VerticalSeekBar) findViewById(R.id.seekbar5);
        bassboost=(SeekBar) findViewById(R.id.bass_boost);
        switchCompat=(SwitchCompat) findViewById(R.id.switchbutton);
        toolbar=(Toolbar) findViewById(R.id.MyToolbar);

        seek1text=(TextView)findViewById(R.id.seek1text);
        seek2text=(TextView)findViewById(R.id.seek2text);
        seek3text=(TextView)findViewById(R.id.seek3text);
        seek4text=(TextView)findViewById(R.id.seek4text);
        seek5text=(TextView)findViewById(R.id.seek5text);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case android.R.id.home:{
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setEqualiser(){
        numberoffreqBands=equalizer.getNumberOfBands();
        upperbandlevel=equalizer.getBandLevelRange()[0];
        upperbandlevel=equalizer.getBandLevelRange()[1];

        Log.i("equalis","setEqualiser");
        Log.i("equalis","setEqualiser upperbandlevel="+upperbandlevel+"lowerbandlevel="+lowerbandlevel);
        refreshseekbars();

        seek1.setOnSeekBarChangeListener(this);
        seek2.setOnSeekBarChangeListener(this);
        seek3.setOnSeekBarChangeListener(this);
        seek4.setOnSeekBarChangeListener(this);
        seek5.setOnSeekBarChangeListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(bassboost!=null) {
            bassboost.setFocusable(false);
            bassboost.setOnSeekBarChangeListener(null);
        }
        if(switchCompat!=null){
            switchCompat.setOnCheckedChangeListener(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        equalizer=con.getEqualiser();
        if(bassboost!=null) {
            bassboost.setFocusable(true);
            bassboost.setOnSeekBarChangeListener(this);
            bassboost.setMax(1000);
            bassboost.setProgress(con.getbassboost());
        }
        if(switchCompat!=null){
            switchCompat.setOnCheckedChangeListener(this);
        }
        if(equalizer!=null) {
            presetbutton.setText(equalizer.getPresetName(equalizer.getCurrentPreset()));
        }

    }

    public void refreshseekbars(){
        equalizer=con.getEqualiser();
        if(equalizer==null){
            for(short i=0;i<numberoffreqBands;i++){
                getSeekBarforpos(i).setProgress(0);
            }
            return;
        }
        String s;
        for(short i=0;i<numberoffreqBands;i++){
            Log.i("equalis","refreshseekbars band level="+equalizer.getBandLevel(i));

            s=equalizer.getCenterFreq(i)/1000+"\n Hz ";
            gettextViewforpos(i).setText(s);
            int maxx=2*(upperbandlevel-lowerbandlevel);
            getSeekBarforpos(i).setMax(maxx);
            short prog=(short)(lowerbandlevel+((upperbandlevel-lowerbandlevel))+equalizer.getBandLevel(i));
            Log.i("equalis","refreshseekbars max="+maxx+" progress="+prog);

            getSeekBarforpos(i).setProgress(prog);
        }
        Log.i("equalis","refreshseekbars getbassboost="+con.getbassboost());

        bassboost.setProgress(con.getbassboost());

        presetbutton.setText(equalizer.getPresetName(equalizer.getCurrentPreset()));
    }
    public TextView gettextViewforpos(short i){
        switch (i){
            case 0:return seek1text;
            case 1:return seek2text;
            case 2:return seek3text;
            case 3:return seek4text;
            case 4:return seek5text;
        }
        return seek1text;
    }
    public SeekBar getSeekBarforpos(short i){
        switch (i){
            case 0:return seek1;
            case 1:return seek2;
            case 2:return seek3;
            case 3:return seek4;
            case 4:return seek5;
        }
        return seek1;
    }


    @Override
    public void onClick(View view) {
        int id=view.getId();
        switch (id){
            case R.id.equaliser_preset:{
                builder=new AlertDialog.Builder(this);
                /// getplaylist to populate popupmenu
                ArrayList<String> pl=con.getPresetList();
                if(pl==null || pl.size()==0){
                    return ;
                }
                String[] presetlist=new String[pl.size()];




                for(int i=0;i<pl.size();i++){
                    presetlist[i]=pl.get(i);
                }

                //presetlist=(String[]) pl.toArray();
                builder.setTitle("Choose a Preset");
                builder.setItems(presetlist, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        con.setPresetList(i);
                        dialogInterface.dismiss();
                        refreshseekbars();
                    }
                });
                builder.create().show();
                break;
            }
            case R.id.equaliser_reverb:{
                builder=new AlertDialog.Builder(this);
                /// getplaylist to populate popupmenu
                ArrayList<String> pl=getReverbList();
                String[] presetlist=new String[pl.size()];
                for(int i=0;i<pl.size();i++){
                    presetlist[i]=pl.get(i);
                }

                //presetlist=(String[]) pl.toArray();
                builder.setTitle("Choose a Preset");
                builder.setItems(presetlist, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        short reverbid=reverbIds.get(i);
                        con.setReverb(reverbid);
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                break;
            }
        }
    }

    public ArrayList<String> getReverbList(){
        reverbIds.clear();
        ArrayList<String> list=new ArrayList<>();
        list.add("None");
        reverbIds.add(PresetReverb.PRESET_NONE);
        list.add("Small Room");
        reverbIds.add(PresetReverb.PRESET_SMALLROOM);
        list.add("Medium Room");
        reverbIds.add(PresetReverb.PRESET_MEDIUMROOM);
        list.add("Large Room");
        reverbIds.add(PresetReverb.PRESET_LARGEROOM);
        list.add("Medium Hall");
        reverbIds.add(PresetReverb.PRESET_MEDIUMHALL);
        list.add("Large Hall");
        reverbIds.add(PresetReverb.PRESET_LARGEHALL);
        list.add("Plate");
        reverbIds.add(PresetReverb.PRESET_PLATE);
        return list;
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromuser) {
        int id=seekBar.getId();
        switch (id){
            case R.id.seekbar1:{
                if(equalizer!=null){
                    short level=(short)(progress-((upperbandlevel-lowerbandlevel)));
                    if(level>upperbandlevel){
                        level=upperbandlevel;
                    }else{
                        if(level< lowerbandlevel){
                            level=lowerbandlevel;
                        }
                    }
                    Log.i("equalis","progress changed level="+level);
                    equalizer.setBandLevel((short)0,level);
                }
                break;
            }
            case R.id.seekbar2:{
                if(equalizer!=null){
                    short level=(short)(progress-((upperbandlevel-lowerbandlevel)));
                    if(level>upperbandlevel){
                        level=upperbandlevel;
                    }else{
                        if(level< lowerbandlevel){
                            level=lowerbandlevel;
                        }
                    }
                    Log.i("equalis","progress changed level="+level);
                    equalizer.setBandLevel((short)0,level);
                }
                break;
            }
            case R.id.seekbar3:{
                if(equalizer!=null){
                    short level=(short)(progress-((upperbandlevel-lowerbandlevel)));
                    if(level>upperbandlevel){
                        level=upperbandlevel;
                    }else{
                        if(level< lowerbandlevel){
                            level=lowerbandlevel;
                        }
                    }
                    Log.i("equalis","progress changed level="+level);
                    equalizer.setBandLevel((short)0,level);
                }
                break;
            }
            case R.id.seekbar4:{
                if(equalizer!=null){
                    short level=(short)(progress-((upperbandlevel-lowerbandlevel)));
                    if(level>upperbandlevel){
                        level=upperbandlevel;
                    }else{
                        if(level< lowerbandlevel){
                            level=lowerbandlevel;
                        }
                    }
                    Log.i("equalis","progress changed level="+level);
                    equalizer.setBandLevel((short)0,level);
                }
                break;
            }
            case R.id.seekbar5:{
                if(equalizer!=null){
                    short level=(short)(progress-((upperbandlevel-lowerbandlevel)));
                    if(level>upperbandlevel){
                        level=upperbandlevel;
                    }else{
                        if(level< lowerbandlevel){
                            level=lowerbandlevel;
                        }
                    }
                    Log.i("equalis","progress changed level="+level);
                    equalizer.setBandLevel((short)0,level);
                }
                break;
            }
            case R.id.bass_boost:{
                Log.i("equalis","progress changed progress="+progress);

                //bassBoost.setStrength((short)progress);
                con.setboost((short)progress);
                break;
            }

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        con.setaudioeffects(b);
        refreshseekbars();
    }
}
