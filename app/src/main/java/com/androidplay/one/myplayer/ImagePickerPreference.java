package com.androidplay.one.myplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Rahul on 03-02-2017.
 */

public class ImagePickerPreference extends DialogPreference implements View.OnClickListener {

    int mCurrentValue = 0;
    ImageView first;
    ImageView second;
    ImageView third;
    ImageView fourth;
    ImageView fifth;
    ImageView sixth;
    ImageView check1;
    ImageView check2;
    ImageView check3;
    ImageView check4;
    ImageView check5;
    ImageView check6;
    int mNewValue;
    Context context;
    public ImagePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setDialogLayoutResource(R.layout.image_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        first=(ImageView)view.findViewById(R.id.first);
        second=(ImageView)view.findViewById(R.id.second);
        third=(ImageView)view.findViewById(R.id.third);
        fourth=(ImageView)view.findViewById(R.id.fourth);
        fifth=(ImageView)view.findViewById(R.id.fifth);
        sixth=(ImageView)view.findViewById(R.id.sixth);
        check1=(ImageView)view.findViewById(R.id.i1s);
        check2=(ImageView)view.findViewById(R.id.i2s);
        check3=(ImageView)view.findViewById(R.id.i3s);
        check4=(ImageView)view.findViewById(R.id.i4s);
        check5=(ImageView)view.findViewById(R.id.i5s);
        check6=(ImageView)view.findViewById(R.id.i6s);
        first.setOnClickListener(this);
        second.setOnClickListener(this);
        third.setOnClickListener(this);
        fourth.setOnClickListener(this);
        fifth.setOnClickListener(this);
        sixth.setOnClickListener(this);

        Picasso.with(context)
                .load(R.drawable.coffee)
                .resize(200,300)
                .centerCrop()
                .into(second);
        Picasso.with(context)
                .load(R.drawable.wood)
                .resize(200,300)
                .centerCrop()
                .into(third);
        Picasso.with(context)
                .load(R.drawable.leaves_port)
                .resize(200,300)
                .centerCrop()
                .into(fourth);
        Picasso.with(context)
                .load(R.drawable.leaves2_port)
                .resize(200,300)
                .centerCrop()
                .into(fifth);
        Picasso.with(context)
                .load(R.drawable.bloom_port)
                .resize(200,300)
                .centerCrop()
                .into(sixth);

        refreshselection();
    }

    private void refreshselection(){
        switch (mCurrentValue){
            case 0:{
                check1.setVisibility(View.VISIBLE);
                check2.setVisibility(View.INVISIBLE);
                check3.setVisibility(View.INVISIBLE);
                check4.setVisibility(View.INVISIBLE);
                check5.setVisibility(View.INVISIBLE);
                check6.setVisibility(View.INVISIBLE);
                break;
            }
            case 1:{
                check1.setVisibility(View.INVISIBLE);
                check2.setVisibility(View.VISIBLE);
                check3.setVisibility(View.INVISIBLE);
                check4.setVisibility(View.INVISIBLE);
                check5.setVisibility(View.INVISIBLE);
                check6.setVisibility(View.INVISIBLE);
                break;
            }
            case 2:{
                check1.setVisibility(View.INVISIBLE);
                check2.setVisibility(View.INVISIBLE);
                check3.setVisibility(View.VISIBLE);
                check4.setVisibility(View.INVISIBLE);
                check5.setVisibility(View.INVISIBLE);
                check6.setVisibility(View.INVISIBLE);
                break;
            }
            case 3:{
                check1.setVisibility(View.INVISIBLE);
                check2.setVisibility(View.INVISIBLE);
                check3.setVisibility(View.INVISIBLE);
                check4.setVisibility(View.VISIBLE);
                check5.setVisibility(View.INVISIBLE);
                check6.setVisibility(View.INVISIBLE);
                break;
            }
            case 4:{
                check1.setVisibility(View.INVISIBLE);
                check2.setVisibility(View.INVISIBLE);
                check3.setVisibility(View.INVISIBLE);
                check4.setVisibility(View.INVISIBLE);
                check5.setVisibility(View.VISIBLE);
                check6.setVisibility(View.INVISIBLE);
                break;
            }
            case 5:{
                check1.setVisibility(View.INVISIBLE);
                check2.setVisibility(View.INVISIBLE);
                check3.setVisibility(View.INVISIBLE);
                check4.setVisibility(View.INVISIBLE);
                check5.setVisibility(View.INVISIBLE);
                check6.setVisibility(View.VISIBLE);
                break;
            }
            default:
        }
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistInt(mNewValue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentValue = this.getPersistedInt(0);

        } else {
            // Set default state from the XML attribute
            mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
        //refreshselection();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        switch (id){
            case R.id.first:{
                Log.i("settn","first");
                mNewValue=0;
                mCurrentValue=0;
                refreshselection();
                return;
            }
            case R.id.second:{

                Log.i("settn","second");
                mNewValue=1;
                mCurrentValue=1;
                refreshselection();
                return;

            }
            case R.id.third:{
                Log.i("settn","third");
                mNewValue=2;
                mCurrentValue=2;
                refreshselection();
                return;

            }
            case R.id.fourth:{
                Log.i("settn","fourth");
                mNewValue=3;
                mCurrentValue=3;
                refreshselection();
                return;

            }
            case R.id.fifth:{
                Log.i("settn","fourth");
                mNewValue=4;
                mCurrentValue=4;
                refreshselection();
                return;

            }
            case R.id.sixth:{
                Log.i("settn","fourth");
                mNewValue=5;
                mCurrentValue=5;
                refreshselection();
                return;

            }
            default:
        }
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readInt(); // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(value); // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
    @Override
    protected Parcelable onSaveInstanceState() {
       final Parcelable superState = super.onSaveInstanceState();
         // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent,
             // use superclass state
             return superState;
             }

        // Create instance of custom BaseSavedState
         final SavedState myState = new SavedState(superState);
         // Set the state's value with the class member that holds current
         // setting value
         myState.value = mNewValue;
         return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
         // Check whether we saved the state in onSaveInstanceState
         if (state == null || !state.getClass().equals(SavedState.class)) {
             // Didn't save the state, so call superclass
             super.onRestoreInstanceState(state);
             return;
             }

         // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        // .setValue(myState.value);
    }

}