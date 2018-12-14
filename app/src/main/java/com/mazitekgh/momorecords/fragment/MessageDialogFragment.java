package com.mazitekgh.momorecords.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.mazitekgh.momorecords.R;


/**
 * Smartnet
 * Created by Zakaria on 30-Mar-18 at 8:16 PM.
 */

public class MessageDialogFragment extends DialogFragment {
    private Activity activity;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View v = activity.getLayoutInflater().inflate(R.layout.dialog_message, null);

        Button gotItButton = v.findViewById(R.id.got_it_bottom);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        gotItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDialogFragment.this.getDialog().cancel();

            }
        });
        builder.setView(v);
        // Add action buttons


        return builder.create();
    }

}

