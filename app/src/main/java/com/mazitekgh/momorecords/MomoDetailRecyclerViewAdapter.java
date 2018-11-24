package com.mazitekgh.momorecords;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mazitekgh.momorecords.MomoDetailFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Momo} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 *
 */
public class MomoDetailRecyclerViewAdapter extends RecyclerView.Adapter<MomoDetailRecyclerViewAdapter.ViewHolder> {

    private final List<Momo> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MomoDetailRecyclerViewAdapter(int whichMomo, List<Momo> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_momodetail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final int pos = position;
        holder.mItem = mValues.get(pos);
        holder.momo_date.setText(mValues.get(pos).getDateStr());
        // holder.momoTextView.setText(mValues.get(position).getContentStr());
        holder.sender.setText(mValues.get(pos).getSender());
        holder.amountReceived.setText("GHS " + mValues.get(pos).getAmount());
        holder.txID.setText(mValues.get(pos).getTxID());
        holder.currentBalance.setText(mValues.get(pos).getCurrentBalance());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mValues.get(pos).getContentStr());
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
                holder.transactionFeeCaption.setText(""); //todo remove when tc fees is implemented
                holder.transactionFee.setText("");
                holder.amountCaption.setText("Amount Sent");
                break;
            }
            case MomoDetailFragment.CREDIT_MOMO: {
                holder.amountCaption.setText("Credit Bought");
                holder.sender.setBackgroundColor(c.getResources().getColor(R.color.colorCredit));
                holder.transactionFeeCaption.setText(""); //todo remove when tc fees is implemented
                holder.transactionFee.setText("");
                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView momo_date;
        //public final TextView momoTextView;
        final TextView sender;
        final TextView txID;
        // public final TextView momo_date;
        final TextView currentBalance;
        final TextView amountReceived;
        final TextView amountCaption;
        final TextView transactionFee;
        final TextView transactionFeeCaption;
        //public final TextView momoTextView;

        Momo mItem;

        ViewHolder(View view) {
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
