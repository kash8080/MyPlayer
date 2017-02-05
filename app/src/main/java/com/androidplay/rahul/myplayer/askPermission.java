package com.androidplay.rahul.myplayer;

import android.Manifest;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Rahul on 28-12-2016.
 */

public class askPermission extends Fragment {
    public final int read_external=11001;

    Button button;
    public askPermission() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_askpermission, container, false);
        button = (Button) v.findViewById(R.id.perm_button);
        //check if have permission

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ask for permission
                request_perm();
            }
        });




        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            showexplanation();

        } else {
            // No explanation needed, we can request the permission.
            request_perm();
        }


    return v;
    }

    public void request_perm(){
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},read_external
                );
        // read_external is an
        // app-defined int constant. The callback method gets the
        // result of the request.
    }
    public void showexplanation(){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setMessage("The app needs your permission to read external storage.");
        builder.setCancelable(true) ;
        builder.setPositiveButton(
                "yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //request for permission
                        request_perm();
                    }
                });

        builder.setNegativeButton(
                "no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //don't request
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

    }



}
