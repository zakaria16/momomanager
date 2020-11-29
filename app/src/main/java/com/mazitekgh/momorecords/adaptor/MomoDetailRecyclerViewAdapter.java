package com.mazitekgh.momorecords.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mazitekgh.momomanager.ExtractMtnMomoInfo;
import com.mazitekgh.momomanager.model.Momo;
import com.mazitekgh.momorecords.R;
import com.mazitekgh.momorecords.fragment.MomoDetailFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Momo} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 *
 */
public class MomoDetailRecyclerViewAdapter extends RecyclerView.Adapter<MomoDetailRecyclerViewAdapter.ViewHolder> {

    private final List<Momo> mValues;
    private final OnListFragmentInteractionListener mListener;
   // private boolean isServerMomo = false;
    Context context;

    public MomoDetailRecyclerViewAdapter(List<Momo> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;

        context = (Context) mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_momodetail,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final int pos = position;
        holder.mItem = mValues.get(pos);
        holder.momo_date.setText(mValues.get(pos).getDate());
        // holder.momoTextView.setText(mValues.get(position).getContentStr());
        holder.amountReceived.setText(context.getString(R.string.amount,mValues.get(pos).getAmount()));
        holder.txID.setText(mValues.get(pos).getTxID());
        holder.currentBalance.setText(mValues.get(pos).getCurrentBalance());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  new Server(context,null).sendData(mValues.get(pos));
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mValues.get(pos).getContent());
                }
            }
        });


        switch (mValues.get(position).getType()) {
            /*case MomoDetailFragment.ALL_MOMO:{
                titleView.setBackgroundColor(context.getResources().getColor(R.color.colorAll));
                break;
            }*/
            case ExtractMtnMomoInfo.RECEIVED_MOMO: {
                holder.sender.setText(context.getString(R.string.sender, "FROM", mValues.get(pos).getSender()));
                holder.sender.setBackgroundColor(context.getResources().getColor(R.color.colorReceived));
                holder.amountCaption.setText(context.getString(R.string.amount_received));
                holder.transactionFeeCaption.setText(context.getString(R.string.reference));
                holder.transactionFee.setText(mValues.get(position).getReference());

                break;
            }
            case ExtractMtnMomoInfo.SENT_MOMO: {
                holder.sender.setText(context.getString(R.string.sender, "TO", mValues.get(pos).getSender()));
                holder.sender.setBackgroundColor(context.getResources().getColor(R.color.colorSent));
                holder.transactionFeeCaption.setText(""); //todo remove when "tc" fees is implemented
                holder.transactionFee.setText("");
                holder.amountCaption.setText(context.getString(R.string.amount_sent));
                break;
            }
            case ExtractMtnMomoInfo.CREDIT_MOMO: {
                holder.sender.setText(context.getString(R.string.sender, "FOR", mValues.get(pos).getSender()));
                holder.amountCaption.setText(context.getString(R.string.credit_bought));
                holder.sender.setBackgroundColor(context.getResources().getColor(R.color.colorCredit));
                holder.transactionFeeCaption.setText(""); //todo remove when "tc" fees is implemented
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
        TextView serverSentTime;
        ToggleButton isSent2ServerView;
        Button retryButton;
        //public final TextView momoTextView;

        Momo mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
//            if (isServerMomo) {
//                serverSentTime = view.findViewById(R.id.time_to_server);
//                isSent2ServerView = view.findViewById(R.id.sent_to_server);
//                retryButton = view.findViewById(R.id.retry_button);
//            }
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
