package com.androidplay.one.myplayer.fast_rec_view;

import android.content.Context;
import android.util.AttributeSet;

import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;

/**
 * Created by Rahul on 12-02-2017.
 */

public class ColorGroupSectionTitleIndicator extends SectionTitleIndicator<char_group> {



    public ColorGroupSectionTitleIndicator(Context context) {

        super(context);

    }



    public ColorGroupSectionTitleIndicator(Context context, AttributeSet attrs) {

        super(context, attrs);

    }



    public ColorGroupSectionTitleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

    }

    @Override
    public void setSection(char_group object) {
        // Example of using a single character

        String str=String.valueOf(object.getaChar()).toUpperCase();
        setTitleText(str);



        // Example of using a longer string

        // setTitleText(colorGroup.getName());

        //setIndicatorTextColor(colorGroup.getAsColor());
    }

}