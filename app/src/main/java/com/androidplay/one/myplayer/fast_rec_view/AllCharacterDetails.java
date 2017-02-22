package com.androidplay.one.myplayer.fast_rec_view;

import java.util.HashMap;

/**
 * Created by Rahul on 12-02-2017.
 */

public class AllCharacterDetails {

    HashMap<Integer,Character> map=new HashMap<>();
    char_group[] group=new char_group[27];
    public AllCharacterDetails(){
        group[0]=new char_group();
        group[1]=new char_group();
        group[2]=new char_group();
        group[3]=new char_group();
        group[4]=new char_group();
        group[5]=new char_group();
        group[6]=new char_group();
        group[7]=new char_group();
        group[8]=new char_group();
        group[9]=new char_group();
        group[10]=new char_group();
        group[11]=new char_group();
        group[12]=new char_group();
        group[13]=new char_group();
        group[14]=new char_group();
        group[15]=new char_group();
        group[16]=new char_group();
        group[17]=new char_group();
        group[18]=new char_group();
        group[19]=new char_group();
        group[20]=new char_group();
        group[21]=new char_group();
        group[22]=new char_group();
        group[23]=new char_group();
        group[24]=new char_group();
        group[25]=new char_group();
        group[26]=new char_group();

        group[0].setaChar('*');
        group[1].setaChar('a');
        group[2].setaChar('b');
        group[3].setaChar('c');
        group[4].setaChar('d');
        group[5].setaChar('e');
        group[6].setaChar('f');
        group[7].setaChar('g');
        group[8].setaChar('h');
        group[9].setaChar('i');
        group[10].setaChar('j');
        group[11].setaChar('k');
        group[12].setaChar('l');
        group[13].setaChar('m');
        group[14].setaChar('n');
        group[15].setaChar('o');
        group[16].setaChar('p');
        group[17].setaChar('q');
        group[18].setaChar('r');
        group[19].setaChar('s');
        group[20].setaChar('t');
        group[21].setaChar('u');
        group[22].setaChar('v');
        group[23].setaChar('w');
        group[24].setaChar('x');
        group[25].setaChar('y');
        group[26].setaChar('z');

    }
    public char_group[] getchargroup(){
        return group;
    }
}
