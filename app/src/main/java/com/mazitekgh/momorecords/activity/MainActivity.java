package com.mazitekgh.momorecords.activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mazitekgh.momorecords.R;
import com.mazitekgh.momorecords.SharedPref;
import com.mazitekgh.momorecords.SmsReceiver;
import com.mazitekgh.momorecords.fragment.MomoDetailFragment;
import com.mazitekgh.momorecords.fragment.RateUsDialogFragment;


public class MainActivity extends AppCompatActivity implements MomoDetailFragment.OnListFragmentInteractionListener, SmsReceiver.OnMomoReceive {
    private SmsReceiver smsReceiver;
    private View view;
    private boolean isFirst = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // pb = findViewById(R.id.progress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            smsReceiver = new SmsReceiver();
            registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
            smsReceiver.setMomoReceivedListener(this);
        }

//        smsReceiver.setMomoReceivedListener(new SmsReceiver.OnMomoReceive() {
//            @Override
//            public void momoReceive(String body) {
//                //Toast.makeText(MainActivity.this, "It's a MOMO MESSAGE", Toast.LENGTH_SHORT).show();
//                //Toast.makeText(MainActivity.this, body, Toast.LENGTH_SHORT).show();
//                frag(new MomoDetailFragment());
//               // startActivity(new Intent(MainActivity.this,MainActivity.class));
//            }
//        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        //  CollapsingToolbarLayout ctl = findViewById(R.id.ctl_layout);

        // pb.setVisibility(View.VISIBLE);

        TextView totalSentView = findViewById(R.id.total_sent);
        TextView totalReceivedView = findViewById(R.id.total_received);
        TextView lastBalance = findViewById(R.id.last_balance);

        // DecimalFormat df = new DecimalFormat("0.00");
        //ExtractMtnMomoInfo exi = new ExtractMtnMomoInfo(this);

        //String totalReceived = df.format(exi.getTotalReceived());
        //String totalSent = df.format(exi.getTotalSent());
        //String currentBalance = df.format(exi.getLatestBalance());

        String totalReceived = getIntent().getStringExtra("totalReceived");
        String totalSent = getIntent().getStringExtra("totalSent");
        String currentBalance = getIntent().getStringExtra("currentBalance");

        totalReceivedView.setText(totalReceived);
        totalSentView.setText(totalSent);
        lastBalance.setText(currentBalance);
        frag(new MomoDetailFragment());
        // pb.setVisibility(View.GONE);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {

            final AlertDialog mzDialog = new AlertDialog.Builder(this).create();
            mzDialog.setTitle("Mobile Money Manager ");
            mzDialog.setMessage(getString(R.string.about_msg));
            mzDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Contact Mazitek GH", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_PHONE_NUMBER, "0541355996");
                    i.putExtra(Intent.EXTRA_CC, new String[]{"fatzak16@gmail.com"});
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@mazitekgh.com"});
                    i.putExtra(Intent.EXTRA_SUBJECT, "About MOMO Manager");
                    i.putExtra(Intent.EXTRA_TEXT, "Our website https://mazitekgh.com");
                    try {
                        startActivity(Intent.createChooser(i, "Send MaziTek GH email..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
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
                })

                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
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

    @Override
    public void momoReceive(String body) {
        //Toast.makeText(MainActivity.this, "It's a MOMO MESSAGE", Toast.LENGTH_SHORT).show();
        //Toast.makeText(MainActivity.this, body, Toast.LENGTH_SHORT).show();
        frag(new MomoDetailFragment());
        // startActivity(new Intent(MainActivity.this,MainActivity.class));
    }
}
