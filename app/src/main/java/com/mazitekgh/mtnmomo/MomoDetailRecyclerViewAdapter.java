package com.mazitekgh.mtnmomo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mazitekgh.mtnmomo.MomoDetailFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Momo} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MomoDetailRecyclerViewAdapter extends RecyclerView.Adapter<MomoDetailRecyclerViewAdapter.ViewHolder> {

    private final List<Momo> mValues;
    private final OnListFragmentInteractionListener mListener;
    private int whichMomo;

    public MomoDetailRecyclerViewAdapter(int whichMomo, List<Momo> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.whichMomo = whichMomo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_momodetail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.momo_date.setText(mValues.get(position).getDateStr());
        // holder.momoTextView.setText(mValues.get(position).getContentStr());
        holder.sender.setText(mValues.get(position).getSender());
        holder.amountReceived.setText(mValues.get(position).getAmount());
        holder.txID.setText(mValues.get(position).getTxID());
        holder.currentBalance.setText(mValues.get(position).getCurrentBalance());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        Context c = (Context) mListener;
        switch (mValues.get(position).getType()) {
            /*case MomoDetailFragment.ALL_MOMO:{
                titleView.setBackgroundColor(c.getResources().getColor(R.color.colorAll));
                break;
            }*/
            case MomoDetailFragment.RECEIVED_MOMO: {
                holder.sender.setBackgroundColor(c.getResources().getColor(R.color.colorReceived));
                holder.amountCaption.setText("Amount Received");
                holder.transactionFeeCaption.setText("Reference");
                holder.transactionFee.setText(mValues.get(position).getReference());

                break;
            }
            case MomoDetailFragment.SENT_MOMO: {
                holder.sender.setBackgroundColor(c.getResources().getColor(R.color.colorSent));
                holder.amountCaption.setText("Amount Sent");
                break;
            }
            case MomoDetailFragment.CREDIT_MOMO: {
                holder.amountCaption.setText("Credit Bought");
                holder.sender.setBackgroundColor(c.getResources().getColor(R.color.colorCredit));
                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView momo_date;
        //public final TextView momoTextView;
        public final TextView sender;
        public final TextView txID;
        // public final TextView momo_date;
        public final TextView currentBalance;
        public final TextView amountReceived;
        public final TextView amountCaption;
        public final TextView transactionFee;
        public final TextView transactionFeeCaption;
        //public final TextView momoTextView;

        public Momo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            momo_date = view.findViewById(R.id.momo_date);
            // momoTextView = (TextView) view.findViewById(R.id.momo_text);
            sender = view.findViewById(R.id.sender);
            amountReceived = view.findViewById(R.id.amount_received);
            amountCaption = view.findViewById(R.id.amount_received_text);
            txID = view.findViewById(R.id.tx_id);
            currentBalance = view.findViewById(R.id.current_balance);
            transactionFee = view.findViewById(R.id.tx_fee);
            transactionFeeCaption = view.findViewById(R.id.tx_fee_caption);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + "'";
        }
    }
}
