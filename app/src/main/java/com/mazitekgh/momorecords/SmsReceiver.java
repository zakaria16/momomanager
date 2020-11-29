package com.mazitekgh.momorecords;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.mazitekgh.momomanager.ExtractMtnMomoInfo;
import com.mazitekgh.momomanager.model.Momo;
import com.mazitekgh.momomanager.model.Sms;

public class SmsReceiver extends BroadcastReceiver {
    //private OnMomoReceive mListener;
    private static final String TAG = "SmsReceiver";
    @Override
    public void onReceive(final Context context, Intent intent) {
        // mListener = (OnMomoReceive) context;

        // an Intent broadcast.
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs;


            if (bundle != null) {
                //---retrieve the SMS message received---
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus == null) {
                        return;
                    }
                    msgs = new SmsMessage[pdus.length];
                    StringBuilder msgBody= new StringBuilder();
                    long date=0;
                    String sender="";
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        sender = msgs[i].getOriginatingAddress();
                        msgBody.append(msgs[i].getMessageBody());
                        date = msgs[i].getTimestampMillis();
                    }
                    Sms sms = new Sms(sender, msgBody.toString(), date);
                    processSMS(context,sms);
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

    private void processSMS(Context context,Sms sms){
        Log.d(TAG, "processSMS: "+sms);
        ExtractMtnMomoInfo momoExi = new ExtractMtnMomoInfo(context);
        //check if it is mobile money msg
        if (momoExi.isMobileMoneyMsg(sms)) {
            Momo momo = momoExi.getMomo(sms);
//            if (momoExi.isReceivedMomo(sms)) {
//                // new Server(context, null).sendData(momo);
//            }


            //Pass the message text to interface
            // mListener.momoReceive(msgBody);

            Toast.makeText(context, "It's a mobile Money Message\n" +
                    "amount: " + momo.getAmount() + "\n" +
                    "sender: " + momo.getSender() + "\n" +
                    "reference: " + momo.getReference() + "\n", Toast.LENGTH_LONG).show();
            //  new MomoDB(context).saveNewsItem(momo);

        } else {
            Toast.makeText(context, "Not Momo Message", Toast.LENGTH_LONG).show();
        }
        //mListener.momoReceive(msgBody);
    }

//    public void setMomoReceivedListener(OnMomoReceive listener) {
//        this.mListener = listener;
//    }

//    public interface OnMomoReceive {
//        void momoReceive(String body);
//    }


}