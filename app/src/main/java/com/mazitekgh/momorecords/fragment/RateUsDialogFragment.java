package com.mazitekgh.momorecords.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.mazitekgh.momorecords.R;
import com.mazitekgh.momorecords.SharedPref;


/**
 * momo manager
 * Created by Zakaria on 30-Mar-18 at 8:16 PM.
 */

public class RateUsDialogFragment extends DialogFragment {
    private Activity activity;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();

        assert activity != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View v = activity.getLayoutInflater().inflate(R.layout.dialog_rate_us, null);

        Button okButton = v.findViewById(R.id.ok_button);
        Button laterButton = v.findViewById(R.id.later_button);
        Button noButton = v.findViewById(R.id.no_button);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + activity.getPackageName())));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName())));
                }
                new SharedPref(activity.getApplicationContext()).disableRatingDialog();
                Toast.makeText(activity, "Press Back key again to Exit", Toast.LENGTH_SHORT).show();
            }
        });
        laterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RateUsDialogFragment.this.getDialog().cancel();
                Toast.makeText(activity, "Press Back key again to Exit", Toast.LENGTH_SHORT).show();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SharedPref(activity.getApplicationContext()).disableRatingDialog();
                RateUsDialogFragment.this.getDialog().cancel();
                Toast.makeText(activity, "Press Back key again to Exit", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(v);
        // Add action buttons


        return builder.create();
    }

}

