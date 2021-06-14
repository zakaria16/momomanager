package com.mazitekgh.momorecords.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mazitekgh.momomanager.MtnMomoManager;
import com.mazitekgh.momomanager.model.Momo;
import com.mazitekgh.momorecords.R;
import com.mazitekgh.momorecords.adaptor.MomoDetailRecyclerViewAdapter;
import com.mazitekgh.momorecords.databinding.FragmentMomodetailListBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MomoDetailFragment extends Fragment {
    private FragmentMomodetailListBinding binding;
    //private static final String MOMO_TYPE = "momo-type";
    // private RecyclerView recyclerView;
    // private List msgList;
    // private int isSaved = 0;
    private OnListFragmentInteractionListener mListener;
    // private TextView infoView;
    private Context context;
    MtnMomoManager mtnMomoManager;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MomoDetailFragment() {
    }


//    public static MomoDetailFragment newInstance(boolean isSavedList) {
//        MomoDetailFragment fragment = new MomoDetailFragment();
//        Bundle args = new Bundle();
//        args.putInt(MOMO_TYPE, isSavedList ? 1 : 0);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        mtnMomoManager = new MtnMomoManager(context);

        //  ProgressDialog pd = ProgressDialog.show(context, "LOADING", "Please Wait...");


        //msgList = new ExtractMtnMomoInfo(getContext()).getMomoMsgList();
        //  pd.dismiss();
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    msgList = new ExtractMtnMomoInfo(getContext()).getMomoMsgList();
//
//                    pd.dismiss();
//                }
//            }).start();


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMomodetailListBinding.inflate(inflater, container, false);
        //View view = inflater.inflate(R.layout.fragment_momodetail_list, container, false);
        View view = binding.getRoot();
        //  infoView = view.findViewById(R.id.info_textview);
        //recyclerView = view.findViewById(R.id.list);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        //select all momo at first
        bottomNavigationView.setSelectedItemId(R.id.navigation_all);


        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = getContext();
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


    private final BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            List<Momo> resList = new ArrayList<>();

            int id = item.getItemId();
            if (id == R.id.navigation_all) {
                resList = mtnMomoManager.getMomoData(MtnMomoManager.ALL_MOMO);
            } else if (id == R.id.navigation_received) {//pb.setVisibility(View.VISIBLE);
                resList = mtnMomoManager.getMomoData(MtnMomoManager.RECEIVED_MOMO);
            } else if (id == R.id.navigation_sent) {
                resList = mtnMomoManager.getMomoData(MtnMomoManager.SENT_MOMO);
            } else if (id == R.id.navigation_credit) {// ctl.setBackgroundColor(getResources().getColor(R.color.colorCredit));
                resList = mtnMomoManager.getMomoData(MtnMomoManager.CREDIT_MOMO);
            }
            if (resList != null) {
                binding.infoTextview.setVisibility(View.GONE);
            } else {
                binding.infoTextview.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
            binding.recyclerView.setAdapter(new MomoDetailRecyclerViewAdapter(resList, mListener));
            return true;
        }

    };


}
