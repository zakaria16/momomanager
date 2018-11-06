package com.mazitekgh.momorecords;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    OnMomoReceive mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        mListener = (OnMomoReceive) context;
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String sender;
            if (bundle != null) {
                //---retrieve the SMS message received---
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        sender = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        if (sender.equals("MobileMoney")) {


                            //Pass the message text to interface
                            mListener.momoReceive(msgBody);

                        }
                    }
                } catch (Exception e) {
                    //      Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }

    void setMomoReceivedListener(OnMomoReceive listener) {
        this.mListener = listener;
    }

    interface OnMomoReceive {
        void momoReceive(String body);
    }


}
