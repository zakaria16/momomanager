package com.mazitekgh.mtnmomo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
    // TODO: Customize parameter argument names
    private static final String MOMO_TYPE = "column-count";
    private RecyclerView recyclerView;
    private List msgList;
    private int momoType = 1;
    private OnListFragmentInteractionListener mListener;
    private ProgressDialog pd;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MomoDetailFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MomoDetailFragment newInstance(int whichType) {
        MomoDetailFragment fragment = new MomoDetailFragment();
        Bundle args = new Bundle();
        args.putInt(MOMO_TYPE, whichType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            momoType = getArguments().getInt(MOMO_TYPE);
        }

        pd = ProgressDialog.show(getContext(), "LOADING", "Please Wait...");
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_momodetail_list, container, false);
        final LinearLayout allViewClick = view.findViewById(R.id.all_activities);
        LinearLayout receivedViewClick = view.findViewById(R.id.received_activities);
        LinearLayout sentViewClick = view.findViewById(R.id.sent_activities);
        LinearLayout creditViewClick = view.findViewById(R.id.credit_activities);
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

    void checkNewAddition() {
        List msgList = new ExtractMtnMomoInfo(getContext()).getMomoList();
        List oldList = new SharedPref(getContext()).getStoreMomoMessages();
        if (msgList == oldList) {
            return;
        }

    }

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
        // TODO: Update argument type and name
        void onListFragmentInteraction(Momo item);

    }

    private class mClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            List<Momo> resList = new ArrayList<>();
            switch (v.getId()) {
                case R.id.all_activities: {
                    //ctl.setBackgroundColor(getResources().getColor(R.color.colorAll));
                    //frag(MomoDetailFragment.newInstance(MomoDetailFragment.ALL_MOMO));


                    resList = new ExtractMtnMomoInfo(msgList).getMessages(MomoDetailFragment.ALL_MOMO);
                    break;
                }
                case R.id.received_activities: {
                    //pb.setVisibility(View.VISIBLE);
                    //ctl.setBackgroundColor(getResources().getColor(R.color.colorReceived));
                    // frag(MomoDetailFragment.newInstance(MomoDetailFragment.RECEIVED_MOMO));

                    resList = new ExtractMtnMomoInfo(msgList).getMessages(MomoDetailFragment.RECEIVED_MOMO);
                    break;
                }
                case R.id.sent_activities: {
                    //pb.setVisibility(View.VISIBLE);
                    //ctl.setBackgroundColor(getResources().getColor(R.color.colorSent));
                    //frag(MomoDetailFragment.newInstance(MomoDetailFragment.SENT_MOMO));

                    resList = new ExtractMtnMomoInfo(msgList).getMessages(MomoDetailFragment.SENT_MOMO);
                    break;
                }
                case R.id.credit_activities: {
                    // ctl.setBackgroundColor(getResources().getColor(R.color.colorCredit));

                    resList = new ExtractMtnMomoInfo(msgList).getMessages(MomoDetailFragment.CREDIT_MOMO);
                    break;
                }
            }

            recyclerView.setAdapter(new MomoDetailRecyclerViewAdapter(momoType,
                    resList, mListener));
        }


    }
}
