package com.mazitekgh.momorecords;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

/**
 * MtnMomo
 * Created by Zakaria on 01-Sep-18 at 4:03 PM.
 */
class ExtractMtnMomoInfo {
    private List msgList;

    public ExtractMtnMomoInfo(Context c) {

        TelephonyProvider telephonyProvider = new TelephonyProvider(c);
        Data d = telephonyProvider.getSms(TelephonyProvider.Filter.INBOX);
        msgList = d.getList();
        msgList = getOnlyMomoSMS();

    }

    public ExtractMtnMomoInfo() {
    }

    public ExtractMtnMomoInfo(List msgList) {
        this.msgList = msgList;
    }

    private List<Sms> getOnlyMomoSMS() {
        List<Sms> resList = new ArrayList<>();
        Sms sms;
        for (int i = 0; i < msgList.size(); i++) {
            sms = (Sms) msgList.get(i);
            if (isMobileMoneyMsg(sms)) {
                resList.add(sms);
            }

        }
        return resList;
    }

    public double getRemainingBalance() {
        return 0;
    }

    public double getTotalReceived() {
        double amount = 0.00;
        Sms sms;
        for (int i = 0; i < msgList.size(); i++) {
            sms = (Sms) msgList.get(i);
            amount += getCashInReceivedAmount(sms) + getPaymentReceivedAmount(sms);
        }

        return amount;
    }

    public double getTotalSent() {
        double amount = 0.00;
        Sms sms;
        for (int i = 0; i < msgList.size(); i++) {
            sms = (Sms) msgList.get(i);
            amount += getCashOutAmount(sms) + getPaymentSentAmount(sms);
        }
        return amount;
    }

    public List<Momo> getMessages(int whichMomo) {
        List<Momo> myResList = new ArrayList<>();
        switch (whichMomo) {
            case MomoDetailFragment.ALL_MOMO: {
                myResList = getAllMessages();
                break;
            }
            case MomoDetailFragment.RECEIVED_MOMO: {
                myResList = getReceivedMessages();
                break;
            }
            case MomoDetailFragment.SENT_MOMO: {
                myResList = getSentMessages();
                break;
            }
            case MomoDetailFragment.CREDIT_MOMO: {
                myResList = getCreditMessages();
                break;
            }
        }
        return myResList;

        //mListener = onResultDone;
        //new LoadSms().execute(whichMomo);
    }

    private List<Momo> getCreditMessages() {
        List<Momo> creditMessages = new ArrayList<>();
        Sms sms;
        for (int i = 0; i < msgList.size(); i++) {
            sms = (Sms) msgList.get(i);
            Momo momo = getCreditMomo(sms);
            if (momo != null) {
                creditMessages.add(momo);
            }
        }
        return creditMessages;
    }


    private List<Momo> getReceivedMessages() {
        List<Momo> receivedMsgs = new ArrayList<>();
        Sms sms;
        for (int i = 0; i < msgList.size(); i++) {
            sms = (Sms) msgList.get(i);
            Momo momo = getReceivedMomo(sms);
            if (momo != null) {
                receivedMsgs.add(momo);
            }
        }
        return receivedMsgs;
    }


    private List<Momo> getSentMessages() {
        List<Momo> sentMessages = new ArrayList<>();
        Sms sms;
        Date d = new Date();

        for (int i = 0; i < msgList.size(); i++) {
            sms = (Sms) msgList.get(i);
            Momo momo = getSentMomo(sms);
            if (momo != null) {
                sentMessages.add(momo);
            }
        }
        return sentMessages;
    }


    private List<Momo> getAllMessages() {
        List<Momo> allMsgs = new ArrayList<>();
        Sms sms;

        for (int i = 0; i < msgList.size(); i++) {
            sms = (Sms) msgList.get(i);
            Momo momoSent = getSentMomo(sms);
            Momo momoReceived = getReceivedMomo(sms);
            Momo momoCredit = getCreditMomo(sms);
            if (momoSent != null) {
                allMsgs.add(momoSent);
            } else if (momoReceived != null) {
                allMsgs.add(momoReceived);
            } else if (momoCredit != null) {
                allMsgs.add(momoCredit);
            }
        }

        return allMsgs;
    }

    private Momo getReceivedMomo(Sms sms) {
        Momo momo = new Momo();
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("E d/M/YY h:m:s a", Locale.getDefault());
        if (receivedMessage(sms) != null) {
            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            momo.setContentStr(receivedMessage(sms));
            momo.setAmount(String.valueOf(getPaymentReceivedAmount(sms)));
            momo.setSender("FROM: " + getSender(sms));
            momo.setTxID(getTxID(sms));
            momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setReference(getReference(sms));
            momo.setType(MomoDetailFragment.RECEIVED_MOMO);
        } else if (cashInMessage(sms) != null) {
            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            momo.setContentStr(cashInMessage(sms));
            momo.setAmount(String.valueOf(getCashInReceivedAmount(sms)));
            momo.setSender("FROM: " + getSender(sms));
            momo.setTxID(getTxID(sms));
            momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(MomoDetailFragment.RECEIVED_MOMO);

        } else {
            return null;
        }

        return momo;
    }


    private Momo getSentMomo(Sms sms) {
        Momo momo = new Momo();
        Date d = new Date();
        //Date date = new Date(dateStamp);
        //TODO causing error here unknown patern character Y
        SimpleDateFormat sdf = new SimpleDateFormat("E d/M/YY h:m:s a", Locale.getDefault());
        //dateStr=sdf.format(date);
        //todo make use one var to test and extract
        if (paymentSentMessage(sms) != null) {
            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            momo.setContentStr(paymentSentMessage(sms));
            momo.setAmount(String.valueOf(getPaymentSentAmount(sms)));
            momo.setSender("To: " + getReceiver(sms));
            momo.setTxID(getTxID(sms));
            momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(MomoDetailFragment.SENT_MOMO);

        } else if (cashOutMessage(sms) != null) {
            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            momo.setContentStr(cashOutMessage(sms));
            momo.setAmount(String.valueOf(getCashOutAmount(sms)));
            momo.setSender("To: " + getReceiver(sms));
            momo.setTxID(getTxID(sms));
            momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(MomoDetailFragment.SENT_MOMO);  //todo distinguish it from above one
        } else {
            return null;
        }

        return momo;
    }

    private Momo getCreditMomo(Sms sms) {
        Momo momo = new Momo();
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("E d/M/YY h:m:s a", Locale.getDefault());
        //todo make use one var to test and extract
        if (paymentSentMessageMtn(sms) != null) {
            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            momo.setContentStr(paymentSentMessageMtn(sms));
            momo.setAmount(String.valueOf(getPaymentSentAmountMtn(sms)));
            momo.setSender("For: " + getReceiver(sms));
            momo.setTxID(getTxID(sms));
            momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(MomoDetailFragment.CREDIT_MOMO);

        } else {
            return null;
        }

        return momo;
    }

    public double getTotalCharge() {
        return 0;
    }


    /**
     * Check whether the message is mm message
     */
    private boolean isMobileMoneyMsg(Sms sms) {
        return (sms.address.equalsIgnoreCase("MobileMoney"));
    }

    private boolean isCashIn(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            res = sms.body.contains("Cash In received");
        }
        return res;
    }

    private boolean isPaymentReceived(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            res = sms.body.contains("Payment received");
        }
        return res;
    }

    private boolean isCashOut(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            res = sms.body.contains("Cash Out made");
        }
        return res;
    }

    private boolean isPaymentSent(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            res = isPaymentSentFor(sms) || isPaymentSentMadeFor(sms);
        }
        return res;
    }

    private boolean isPaymentSentMadeFor(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            res = sms.body.contains("Payment made for");
        }
        return res;
    }

    private boolean isPaymentSentFor(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            res = sms.body.contains("Payment for");
        }
        return res;
    }

    private boolean isPaymentSentMtn(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {

            res = sms.body.contains("Your payment of");
            if (res && sms.body.contains("failed")) {
                res = false;
            }
        }
        return res;
    }


    //receive amount
    private double getCashInReceivedAmount(Sms sms) {
        if (!isCashIn(sms)) {
            return 0;
        }

        String tt = "Cash In received for GHS ";
        int st = -1;
        int end = -1;
        String ss;
        if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length();
            end = sms.body.indexOf(" from", st);
        }
        ss = sms.body.substring(st, end);
        return Double.valueOf(ss);
    }

    private double getPaymentReceivedAmount(Sms sms) {
        if (!isPaymentReceived(sms)) {
            return 0;
        }

        double db;
        String tt = "Payment received for";
        int st;
        int end = -1;
        String ss;
        if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length() + 1;
            int sm = sms.body.indexOf("GHS ", st) + 4;
            end = sms.body.indexOf(" from", sm);
            ss = sms.body.substring(sm, end);
            db = Double.valueOf(ss);
        } else {
            db = 0;
        }

        return db;
    }

    private String getReference(Sms sms) {
        if (!isPaymentReceived(sms)) {
            return "none";
        }

        String tt = "Reference:";
        String endString = ". Transaction";
        int st = -1;
        int end = -1;
        String ss;

        st = sms.body.indexOf(tt) + tt.length() + 1;
        //int sm = sms.body.indexOf("GHS ", st) + 4;
        end = sms.body.indexOf(endString, st);
        ss = sms.body.substring(st, end);

        return ss;

    }


    //sent amount
    private double getCashOutAmount(Sms sms) {
        if (!isCashOut(sms)) {
            return 0;
        }

        String tt = "Cash Out made for GHS";
        int st = -1;
        int end = -1;
        String ss;
        if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length();
            end = sms.body.indexOf(" to", st);
        }
        ss = sms.body.substring(st, end);
        return Double.valueOf(ss);
    }

    private double getPaymentSentAmount(Sms sms) {
        if (!isPaymentSent(sms)) {
            return 0;
        }

        double db;
        String tt = isPaymentSentMadeFor(sms) ? "Payment made for" : "Payment for";

        int st = -1;
        int end = -1;
        String ss;
        if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length() + 1;
            int sm = sms.body.indexOf("GHS", st) + 3;
            end = sms.body.indexOf(" to", sm);
            ss = sms.body.substring(sm, end);
            db = Double.valueOf(ss);
        } else {
            db = getPaymentSentAmountMtn(sms);
        }

        return db;
    }

    private double getPaymentSentAmountMtn(Sms sms) {
        if (!isPaymentSentMtn(sms)) {
            return 0;
        }

        double db = 0.0;
        String tt = "Your payment of";
        int st = -1;
        int end = -1;
        String ss;
        if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length() + 1;
            int sm = sms.body.indexOf("GHS ", st) + 4;
            end = sms.body.indexOf(" to", sm);
            ss = sms.body.substring(sm, end);
            db = Double.valueOf(ss);

        }
        return db;
    }

    //receive messages
    private String receivedMessage(Sms sms) {
        return isPaymentReceived(sms) ? sms.body : null;
    }

    private String cashInMessage(Sms sms) {
        return isCashIn(sms) ? sms.body : null;
    }


    //sent messages
    private String cashOutMessage(Sms sms) {
        return isCashOut(sms) ? sms.body : null;
    }

    private String paymentSentMessage(Sms sms) {
        return isPaymentSent(sms) ? sms.body : null;
    }

    private String paymentSentMessageMtn(Sms sms) {
        return isPaymentSentMtn(sms) ? sms.body : null;
    }


    //sender_receiver
    private String getSender(Sms sms) {
        if (!(isPaymentReceived(sms)) && !(isCashIn(sms))) {
            return null;
        }

        int st = sms.body.indexOf("from ");
        int end = sms.body.indexOf("Current Balance");
        return sms.body.substring(st + 5, end);
    }

    private String getReceiver(Sms sms) {
        if (!(isPaymentSent(sms) || isPaymentSentMtn(sms) || isCashOut(sms))) {
            return null;
        }
        String ss;
        if (isPaymentSentMtn(sms)) {
            int st = sms.body.indexOf("to ");
            int end = sms.body.indexOf("has", st);

            ss = sms.body.substring(st + 3, end);
        } else {
            int st = sms.body.indexOf("to ");
            int end = sms.body.indexOf("Current Balance");
            ss = sms.body.substring(st + 3, end);
        }
        return ss;

    }


    private String getTxID(Sms sms) {
        String startStr;
        if (isCashIn(sms)) {
            return "none";
        } else if (isPaymentSentMtn(sms) || isCashOut(sms)) {
            startStr = "Financial Transaction Id: ";
            int st = sms.body.indexOf(startStr);
            int end = sms.body.indexOf(".", st);
            String ss = sms.body.substring(st + startStr.length(), end);
            return ss;
        }
        startStr = "Transaction ID: ";
        int st = sms.body.indexOf(startStr);
        int end = sms.body.indexOf(".", st);
        return sms.body.substring(st + startStr.length(), end);


    }

    private double getIndividualCB(Sms sms) {
        boolean isPay = isPaymentReceived(sms);
        boolean isCashOut = isCashOut(sms);
        boolean isPaymentSentFor = isPaymentSentFor(sms);
        if (!isPay && !isCashIn(sms) && !isCashOut && !isPaymentSent(sms) && !isPaymentSentMtn(sms)) {
            return 0;
        }
        String firstPattern = "Current Balance";
        String endPattern; // = isPay ? ". Available Balance" : "Available Balance";
        if (isPay) {
            endPattern = ". Available Balance";
        } else if (isCashOut) {
            endPattern = "Financial";
        } else if (isPaymentSentMtn(sms)) {
            firstPattern = "balance:";
            endPattern = ". Fee";
        } else if (isPaymentSentFor) {
            //firstPattern = "balance:";
            endPattern = ". Transaction";
        } else {
            endPattern = isPaymentSentMadeFor(sms) ? ". Available Balance" : "Available Balance";
        }
        int st = sms.body.indexOf(firstPattern) + firstPattern.length();
        int end = sms.body.indexOf(endPattern, st);
        int md = isCashOut ? sms.body.indexOf("GHS", st) - 1 : sms.body.indexOf("GHS ", st); //portable
        String ss = sms.body.substring(md + 4, end);
        ss = ss.trim();
        return Double.valueOf(ss);
    }

    public double getLatestBalance() {
        int lastIndex = msgList.size();
        double currentBal = 0;

        Sms sms;
        for (int i = 0; i < lastIndex; i++) {
            sms = (Sms) msgList.get(i);
            if (isMobileMoneyMsg(sms)) {
                currentBal = getIndividualCB(sms);
                break;
            }
        }

        return currentBal;
    }

    public List getMomoList() {
        return msgList;
    }


    /*  void ffgggggg(){
          String patern="Cash * In * received * for * GHS [0-9]*.[0-9]*";

          String smms="Cash In received for GHS 60.50 from F.O.DIVINE VENTURES ISAAC SARPONG. Current Balance GHS 60.57  Available Balance GHS 60.57. Cash in (Deposit) is a free transaction on MTN Mobile Money. Please do not pay any fees for it. Thank you for using MTN MobileMoney. Fee charged: GHS 0.\n";



      }*/


    public interface OnResultDone {
        void resultDone(List<Momo> resList);
    }
}
