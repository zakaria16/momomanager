package com.mazitekgh.momorecords.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.mazitekgh.momorecords.R;


/**
 * Momo manager
 * Created by Zakaria on 30-Mar-18 at 8:16 PM.
 */

public class MessageDialogFragment extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View v = activity.getLayoutInflater().inflate(R.layout.dialog_message, null);

        Button gotItButton = v.findViewById(R.id.ok_button);
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

