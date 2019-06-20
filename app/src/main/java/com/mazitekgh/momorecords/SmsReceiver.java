package com.mazitekgh.momorecords;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.mazitekgh.momorecords.model.Momo;
import com.mazitekgh.momorecords.model.Sms;

public class SmsReceiver extends BroadcastReceiver {
    private OnMomoReceive mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        // mListener = (OnMomoReceive) context;

        // an Intent broadcast.
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs;
            String sender;
            ExtractMtnMomoInfo momoExi = new ExtractMtnMomoInfo(context);
            if (bundle != null) {
                //---retrieve the SMS message received---
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus == null) {
                        return;
                    }
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        sender = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        Long date = msgs[i].getTimestampMillis();
                        Sms sms = new Sms(sender, msgBody, date);

                        if (momoExi.isMobileMoneyMsg(sms)) {
                            Momo momo = new Momo();
                            //if (momoExi.isReceivedMomo(sms)) {
                            momo = momoExi.getMomo(sms);
                            //}
                            //Pass the message text to interface
                            // mListener.momoReceive(msgBody);

                            Toast.makeText(context, "It's a mobile Message\n" +
                                    "amount: " + momo.getAmount() + "\n" +
                                    "sender: " + momo.getSender() + "\n" +
                                    "reference: " + momo.getReference() + "\n", Toast.LENGTH_SHORT).show();
                            new MomoDB(context).saveNewsItem(momo);
                            if (mListener != null) {
                                mListener.momoReceive(msgBody);
                            }

                        } else {
                            Toast.makeText(context, "Not Momo Message", Toast.LENGTH_SHORT).show();
                        }
                        //mListener.momoReceive(msgBody);
                    }
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

    public void setMomoReceivedListener(OnMomoReceive listener) {
        this.mListener = listener;
    }

    public interface OnMomoReceive {
        void momoReceive(String body);
    }


}