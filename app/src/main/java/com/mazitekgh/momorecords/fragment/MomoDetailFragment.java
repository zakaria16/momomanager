package com.mazitekgh.momorecords.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mazitekgh.momorecords.ExtractMtnMomoInfo;
import com.mazitekgh.momorecords.R;
import com.mazitekgh.momorecords.adaptor.MomoDetailRecyclerViewAdapter;
import com.mazitekgh.momorecords.model.Momo;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MomoDetailFragment extends Fragment {

    public static final int ALL_MOMO = 1;
    public static final int RECEIVED_MOMO = 2;
    public static final int SENT_MOMO = 3;
    public static final int CREDIT_MOMO = 5;

    private static final String MOMO_TYPE = "momo-type";
    private RecyclerView recyclerView;
    private List msgList;
    private int momoType = 1;
    private OnListFragmentInteractionListener mListener;
    private ProgressDialog pd;
    private TextView infoView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MomoDetailFragment() {
    }

/*
    public static MomoDetailFragment newInstance(int whichType) {
        MomoDetailFragment fragment = new MomoDetailFragment();
        Bundle args = new Bundle();
        args.putInt(MOMO_TYPE, whichType);
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            momoType = getArguments().getInt(MOMO_TYPE);
        }

        pd = ProgressDialog.show(getContext(), "LOADING", "Please Wait...");
        //todo make it async task
        //todo if list is not empty dont load new one
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Gson gs = new Gson();
                // if(gsonString==null) {
                msgList = new ExtractMtnMomoInfo(getContext()).getMomoList();

                pd.dismiss();
//                    String st = gs.toJson(msgList);
//                    new SharedPref(getContext()).storeMomoMessages(st);
                // }else{
                //  msgList = gs.fromJson(gsonString,List.class);
                //msgList   = new SharedPref(getContext()).getStoreMomoMessages();
                // }
            }
        }).start();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_momodetail_list, container, false);
        LinearLayout allViewClick = view.findViewById(R.id.all_activities);
        LinearLayout receivedViewClick = view.findViewById(R.id.received_activities);
        LinearLayout sentViewClick = view.findViewById(R.id.sent_activities);
        LinearLayout creditViewClick = view.findViewById(R.id.credit_activities);
        infoView = view.findViewById(R.id.info_textview);
        recyclerView = view.findViewById(R.id.list);

        allViewClick.setOnClickListener(new mClickListener());
        receivedViewClick.setOnClickListener(new mClickListener());
        sentViewClick.setOnClickListener(new mClickListener());
        receivedViewClick.setOnClickListener(new mClickListener());
        creditViewClick.setOnClickListener(new mClickListener());

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*void checkNewAddition() {
        List msgList = new ExtractMtnMomoInfo(getContext()).getMomoList();
        List oldList = new SharedPref(getContext()).getStoreMomoMessages();
        if (msgList == oldList) {

        }

    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(String body);

    }

    private class mClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            List<Momo> resList = new ArrayList<>();
            switch (v.getId()) {
                case R.id.all_activities: {
                    //ctl.setBackgroundColor(getResources().getColor(R.color.colorAll));
                    //frag(MomoDetailFragment.newInstance(MomoDetailFragment.ALL_MOMO));


                    resList = new ExtractMtnMomoInfo(getContext(), msgList).getMessages(MomoDetailFragment.ALL_MOMO);
                    break;
                }
                case R.id.received_activities: {
                    //pb.setVisibility(View.VISIBLE);
                    //ctl.setBackgroundColor(getResources().getColor(R.color.colorReceived));
                    // frag(MomoDetailFragment.newInstance(MomoDetailFragment.RECEIVED_MOMO));

                    resList = new ExtractMtnMomoInfo(getContext(), msgList).getMessages(MomoDetailFragment.RECEIVED_MOMO);
                    break;
                }
                case R.id.sent_activities: {
                    //pb.setVisibility(View.VISIBLE);
                    //ctl.setBackgroundColor(getResources().getColor(R.color.colorSent));
                    //frag(MomoDetailFragment.newInstance(MomoDetailFragment.SENT_MOMO));

                    resList = new ExtractMtnMomoInfo(getContext(), msgList).getMessages(MomoDetailFragment.SENT_MOMO);
                    break;
                }
                case R.id.credit_activities: {
                    // ctl.setBackgroundColor(getResources().getColor(R.color.colorCredit));

                    resList = new ExtractMtnMomoInfo(getContext(), msgList).getMessages(MomoDetailFragment.CREDIT_MOMO);
                    break;
                }
            }
            if (resList != null) {
                infoView.setVisibility(View.GONE);
            } else {
                infoView.setVisibility(View.VISIBLE);
            }
            recyclerView.setAdapter(new MomoDetailRecyclerViewAdapter(momoType,
                    resList, mListener));
        }


    }

    /*private class bgrndLoad extends AsyncTask<Void, Integer, Void> {
        private Intent intent;

        @Override
        protected Void doInBackground(Void[] objects) {
            DecimalFormat df = new DecimalFormat("0.00");
            ExtractMtnMomoInfo exi = new ExtractMtnMomoInfo(getContext());
            msgList=exi.getMomoList();

//            publishProgress(50);
//            String totalReceived = df.format(exi.getTotalReceived());
//            publishProgress(65);
//            String totalSent = df.format(exi.getTotalSent());
//            publishProgress(75);
//            String currentBalance = df.format(exi.getLatestBalance());
//            publishProgress(85);
           // intent = new Intent(InitialActivity.this, MainActivity.class);
            //intent.putExtra("totalReceived", totalReceived);
            //intent.putExtra("totalSent", totalSent);
            //intent.putExtra("currentBalance", currentBalance);
         //   publishProgress(100);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            String s = values[0] + "%";
           // progressPercent.setText(s);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
           // startActivity(intent);
            //InitialActivity.this.finish();

        }
    }*/
}
