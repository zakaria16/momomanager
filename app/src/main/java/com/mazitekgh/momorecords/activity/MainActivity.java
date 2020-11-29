package com.mazitekgh.momorecords.activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mazitekgh.momorecords.BuildConfig;
import com.mazitekgh.momorecords.R;
import com.mazitekgh.momorecords.SharedPref;
import com.mazitekgh.momorecords.SmsReceiver;
import com.mazitekgh.momorecords.fragment.MomoDetailFragment;
import com.mazitekgh.momorecords.fragment.RateUsDialogFragment;


public class MainActivity extends AppCompatActivity implements
        MomoDetailFragment.OnListFragmentInteractionListener {
    private SmsReceiver smsReceiver;
    // private View view;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            smsReceiver = new SmsReceiver();
            registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
            // smsReceiver.setMomoReceivedListener(this);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);
        TextView totalSentView = findViewById(R.id.total_sent);
        TextView totalReceivedView = findViewById(R.id.total_received);
        TextView lastBalance = findViewById(R.id.last_balance);

        String totalReceived = getIntent().getStringExtra("totalReceived");
        String totalSent = getIntent().getStringExtra("totalSent");
        String currentBalance = getIntent().getStringExtra("currentBalance");

        totalReceivedView.setText(totalReceived);
        totalSentView.setText(totalSent);
        lastBalance.setText(currentBalance);
        frag(new MomoDetailFragment());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            final AlertDialog mzDialog = new AlertDialog.Builder(this).create();
            mzDialog.setTitle(getString(R.string.app_name));
            // TODO: 24-Nov-20 find a way to link lib version
            mzDialog.setMessage(getString(R.string.about_msg, BuildConfig.VERSION_NAME, "1.0.0"));
            mzDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Contact Mazitek GH", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String payload = "https://api.whatsapp.com/send?phone=233207062250&text=About_" + getString(R.string.app_name);

                    try {
                        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(payload)));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            mzDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mzDialog.cancel();
                }
            });
            mzDialog.show();

        } else if (id == R.id.action_mtn) {
            //toast will be here
            Toast.makeText(this, "other networks coming soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_rate_app) {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName())));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void frag(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        //ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onListFragmentInteraction(String bodyMessage) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message")
                .setMessage(bodyMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(smsReceiver);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {

        if (isFirst && new SharedPref(this).showRatingDialog()) {
            new RateUsDialogFragment().show(getSupportFragmentManager(), "rate_us");
            isFirst = false;
        } else {
            super.onBackPressed();
        }

    }
//
//    @Override
//    public void momoReceive(String body) {
//        //Toast.makeText(MainActivity.this, "It's a MOMO MESSAGE", Toast.LENGTH_SHORT).show();
//        //Toast.makeText(MainActivity.this, body, Toast.LENGTH_SHORT).show();
//        frag(new MomoDetailFragment());
//        // startActivity(new Intent(MainActivity.this,MainActivity.class));
//    }
}
