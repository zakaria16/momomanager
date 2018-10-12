package com.mazitekgh.mtnmomo;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements MomoDetailFragment.OnListFragmentInteractionListener {
    private CollapsingToolbarLayout ctl;
    private ProgressBar pb;

    private OnCommandReady mlistener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        pb = findViewById(R.id.progress);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        ctl = findViewById(R.id.ctl_layout);

        pb.setVisibility(View.VISIBLE);
        TextView totalSentView = findViewById(R.id.total_sent);
        TextView totalReceivedView = findViewById(R.id.total_received);
        TextView lastBalance = findViewById(R.id.last_balance);

        DecimalFormat df = new DecimalFormat("0.00");
        ExtractMtnMomoInfo exi = new ExtractMtnMomoInfo(this);

        String totalReceived = df.format(exi.getTotalReceived());
        String totalSent = df.format(exi.getTotalSent());
        totalReceivedView.setText(totalReceived);
        totalSentView.setText(totalSent);
        lastBalance.setText(df.format(exi.getLatestBalance()));
        frag(new MomoDetailFragment());
        pb.setVisibility(View.GONE);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void frag(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        //ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onListFragmentInteraction(Momo item) {
        pb.setVisibility(View.GONE);
    }


    interface OnCommandReady {
        void onSendComand(int whichMomo);
    }
}
