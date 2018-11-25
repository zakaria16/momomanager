package com.mazitekgh.momorecords;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class InitialActivity extends AppCompatActivity {

    private static final int SMS_PERMISION_CODE = 232;
    private ProgressBar pb;
    private TextView progressPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        pb = findViewById(R.id.progressBar);

        progressPercent = findViewById(R.id.progress_percent);

        //First checking if the app is already having the permission 
        if (isSmsPermissionGranted()) {
            //If permission is already having then showing the toast
            // Toast.makeText(this, "You already have the permission to Access Storage", Toast.LENGTH_LONG).show();
            showApp();
        } else {
            //If the app has not the permission then asking for the permission
            showDialog();
        }


    }

    private void showApp() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                DecimalFormat df = new DecimalFormat("0.00");
//                ExtractMtnMomoInfo exi = new ExtractMtnMomoInfo(InitialActivity.this);
//                String totalReceived = df.format(exi.getTotalReceived());
//                String totalSent = df.format(exi.getTotalSent());
//                String currentBalance = df.format(exi.getLatestBalance());
//                Intent intent = new Intent(InitialActivity.this, MainActivity.class);
//                intent.putExtra("totalReceived", totalReceived);
//                intent.putExtra("totalSent", totalSent);
//                intent.putExtra("currentBalance", currentBalance);
//                startActivity(intent);
//                InitialActivity.this.finish();
//            }
//        }).start();
        progressPercent.setText("20%");
        new bgrndLoad().execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == SMS_PERMISION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                // Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
                showApp();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "App cannot work without sms permission",
                        Toast.LENGTH_LONG).show();
                pb.setVisibility(View.GONE);
                progressPercent.setVisibility(View.GONE);
            }
        }
    }

    private void requestSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECEIVE_SMS)) {
            Toast.makeText(getApplicationContext(), "Sms permission Required for app to work", Toast.LENGTH_SHORT).show();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, SMS_PERMISION_CODE);
    }

    private boolean isSmsPermissionGranted() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        return (result == PackageManager.PERMISSION_GRANTED);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("The app needs your Permission")
                .setMessage(getString(R.string.permission_msg) +
                        "")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestSmsPermission();
                    }
                }).show();
    }


    private class bgrndLoad extends AsyncTask<Void, Integer, Void> {
        private Intent intent;

        @Override
        protected Void doInBackground(Void[] objects) {
            DecimalFormat df = new DecimalFormat("0.00");
            ExtractMtnMomoInfo exi = new ExtractMtnMomoInfo(InitialActivity.this);
            publishProgress(50);
            String totalReceived = df.format(exi.getTotalReceived());
            publishProgress(65);
            String totalSent = df.format(exi.getTotalSent());
            publishProgress(75);
            String currentBalance = df.format(exi.getLatestBalance());
            publishProgress(85);
            intent = new Intent(InitialActivity.this, MainActivity.class);
            intent.putExtra("totalReceived", totalReceived);
            intent.putExtra("totalSent", totalSent);
            intent.putExtra("currentBalance", currentBalance);
            publishProgress(100);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            String s = values[0] + "%";
            progressPercent.setText(s);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startActivity(intent);
            InitialActivity.this.finish();

        }
    }

}
