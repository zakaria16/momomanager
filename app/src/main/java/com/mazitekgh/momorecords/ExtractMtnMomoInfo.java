package com.mazitekgh.momorecords;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mazitekgh.momorecords.fragment.MomoDetailFragment;
import com.mazitekgh.momorecords.model.Momo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

/**
 * Created by Zakaria on 01-Sep-18 at 4:03 PM.
 */
public class ExtractMtnMomoInfo {
    private static final String CASH_IN_RECEIVED_PATTERN = "\\s*[cC]ash\\s{1,3}[iI]n\\s{1,3}received\\s{0,3}for\\s{0,3}GHS\\s{0,3}([\\d]*[.]?[\\d]*)";
    /**
     * CASH IN PATTERN
     * group 1: amount received
     * group 2: sender
     * group 3: current balance
     */
    private static final String CASH_IN_PATTERN =
            "\\s*[cC]ash\\s{1,3}[iI]n\\s{1,3}[rR]eceived\\s{0,3}for\\s{0,3}GHS\\s{0,3}([\\d]*[\\.]?[\\d]*)\\s{0,3}from\\s{0,3}([\\w\\s.\\W]*)\\s{0,3}[cC]urrent\\s{0,3}[bB]alance[\\W]?\\s{0,3}GHS\\s{0,3}([\\d]*[\\.]?[\\d]*)\\s";
    private static final int GROUP_CASH_IN_AMOUNT = 1;
    private static final int GROUP_CASH_IN_SENDER = 2;
    private static final int GROUP_CASH_IN_CURRENT_BAL = 3;
    private static final String PAYMENT_RECEIVED_PATTERN = "\\s*[P]ayment\\s{1,3}[rR]eceived\\s{1,3}for";
    /**
     * RECEIVED PATTERN
     * group 1: amount received
     * group 2: sender
     * group 3: current balance
     * group 4: reference
     * group 5: transaction id
     * group 6: transaction fee
     */
    private static final String RECEIVED_PATTERN =
            "\\s*[pP]ayment\\s{1,3}[rR]eceived\\s{0,3}for\\s{0,3}GHS\\s{0,3}([\\d]*[\\.]?[\\d]*)\\s{0,3}from\\s{0,3}([\\w\\s.\\W]*)\\s{0,3}[cC]urrent\\s{0,3}[bB]alance[\\W]?\\s{0,3}GHS\\s{0,3}([\\d]*[\\.]?[\\d]*)\\s{0,3}[\\w\\W]*[rR]eference[\\W]?\\s{0,3}([\\w\\W]*)\\s{0,3}[tT]ransaction\\s{0,3}[Ii][Dd]\\W?\\s{0,3}([\\d]*)[\\w\\W]*\\s[tT][rR][aA][nN][sS][aA][cC][tT][iI][oO][nN] [feeFEE]{3}\\W?([\\d.\\d]*)[\\w\\W]*";
    private static final int GROUP_REC_AMOUNT = 1;
    private static final int GROUP_REC_SENDER = 2;
    private static final int GROUP_REC_CURRENT_BAL = 3;
    private static final int GROUP_REC_REFERENCE = 4;
    private static final int GROUP_REC_TX_ID = 5;
    private static final int GROUP_REC_TX_FEE = 6;

    private static final String CASH_OUT_PATTERN = "\\s*[cC]ash\\s{0,3}[oO]ut\\s{1,3}made";
    private static final String PAYMENT_SENT_PATTERN = "\\s*[pP]ayment\\s{0,3}([mM]ade)?\\s{0,3}[fF]or";
    private final int CURRENT_BALANCE = 0;
    private final int TOTAL_RECEIVED = 1;
    private final int TOTAL_SENT = 2;
    private List msgList;
    private final SharedPref sharedPref;

    public ExtractMtnMomoInfo(Context c) {
        // if(shouldLoad()) {
        TelephonyProvider telephonyProvider = new TelephonyProvider(c);
        sharedPref = new SharedPref(c);
        Data d = telephonyProvider.getSms(TelephonyProvider.Filter.INBOX);
        msgList = d.getList();
        msgList = getOnlyMomoSMS();

        //sharedPref.storeMomoMessages(msgList);
//        }else {
//            msgList = sharedPref.getStoreMomoMessages();
//            //todo dont repeat ur self
//            if(msgList==null){
//                TelephonyProvider telephonyProvider = new TelephonyProvider(c);
//                sharedPref = new SharedPref(c);
//                Data d = telephonyProvider.getSms(TelephonyProvider.Filter.INBOX);
//                msgList = d.getList();
//                msgList = getOnlyMomoSMS();
//                sharedPref.storeMomoMessages(msgList);
//            }
//       }
    }

    public ExtractMtnMomoInfo(Context context, List msgList) {
        this.msgList = msgList;
        sharedPref = new SharedPref(context);
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


    public double getTotalReceived() {
        if (msgList == null) {
            return 0;
        }
        double amount = 0.00;
        if (shouldLoad(TOTAL_RECEIVED)) {
            Sms sms;
            for (int i = 0; i < msgList.size(); i++) {
                sms = (Sms) msgList.get(i);
                amount += getCashInReceivedAmount(sms) + getPaymentReceivedAmount(sms);
            }
            sharedPref.storeTotalReceivedAmount(amount);
        }
        {
            amount = sharedPref.getTotalReceived();
        }

        return amount;
    }

    public double getTotalSent() {
        if (msgList == null) {
            return 0;
        }
        double amount = 0.00;
        if (shouldLoad(TOTAL_SENT)) {
            Sms sms;
            for (int i = 0; i < msgList.size(); i++) {
                sms = (Sms) msgList.get(i);
                amount += getCashOutAmount(sms) + getPaymentSentAmount(sms);
            }
            sharedPref.storeTotalSentAmount(amount);
        } else {
            amount = sharedPref.getTotalSent();
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

    /**
     * Get Received momo messages
     *
     * @return List<Momo>
     */
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

        SimpleDateFormat sdf;
        try {
            sdf = new SimpleDateFormat("E d/M/y h:m:s a", Locale.getDefault());
        } catch (IllegalArgumentException e) {
            sdf = new SimpleDateFormat();
        }

        if (receivedMessage(sms) != null) {
            Matcher m = Pattern.compile(RECEIVED_PATTERN).matcher(sms.body);
            m.find();
            momo.setAmount(m.group(GROUP_REC_AMOUNT));
            momo.setSender("FROM: " + m.group(GROUP_REC_SENDER));
            momo.setCurrentBalance(m.group(GROUP_REC_CURRENT_BAL));
            momo.setTxID(m.group(GROUP_REC_TX_ID));
            momo.setContentStr(sms.body);
            momo.setReference(m.group(GROUP_REC_REFERENCE));

            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            // momo.setContentStr(receivedMessage(sms));
            //momo.setAmount(String.valueOf(getPaymentReceivedAmount(sms)));
            //momo.setSender("FROM: " + getSender(sms));
            //momo.setTxID(getTxID(sms));
            //momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            // momo.setReference(getReference(sms));
            momo.setType(MomoDetailFragment.RECEIVED_MOMO);
        } else if (cashInMessage(sms) != null) {
            Matcher m = Pattern.compile(CASH_IN_PATTERN).matcher(sms.body);
            m.find();
            momo.setAmount(m.group(GROUP_CASH_IN_AMOUNT));
            momo.setSender("FROM: " + m.group(GROUP_CASH_IN_SENDER));
            momo.setCurrentBalance(m.group(GROUP_CASH_IN_CURRENT_BAL));

            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            momo.setContentStr(sms.body);
            // momo.setAmount(String.valueOf(getCashInReceivedAmount(sms)));
            // momo.setSender("FROM: " + getSender(sms));
            momo.setTxID(getTxID(sms)); //none
            //momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
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

        SimpleDateFormat sdf;
        try {
            sdf = new SimpleDateFormat("E d/M/y h:m:s a", Locale.getDefault());
        } catch (IllegalArgumentException e) {
            sdf = new SimpleDateFormat();
        }

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
        //YY gives error  below 21
        SimpleDateFormat sdf;
        try {
            sdf = new SimpleDateFormat("E d/M/y h:m:s a", Locale.getDefault());
        } catch (IllegalArgumentException e) {
            sdf = new SimpleDateFormat();
        }

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
     * @param sms message to check
     * @return true if momo message or false if not momo message
     */
    private boolean isMobileMoneyMsg(Sms sms) {
        return (sms.address.equalsIgnoreCase("MobileMoney"));
    }

    private boolean isCashIn(Sms sms) {
        boolean res = false;
        Pattern p = Pattern.compile(CASH_IN_RECEIVED_PATTERN);
        Matcher m = p.matcher(sms.body);

        if (isMobileMoneyMsg(sms)) {
            //res = sms.body.contains("Cash In received");
            res = m.find();

        }
        return res;
    }

    private boolean isPaymentReceived(Sms sms) {
        boolean res = false;
        // Payment received for  GHS 200.00 from UMB Bank OVA Current Balance: GHS 1716.14 . Available Balance: GHS 1716.14. Reference: Credit MTN Customer. Transaction ID: 4772674558. TRANSACTION FEE: 0.00
        Pattern p = Pattern.compile(PAYMENT_RECEIVED_PATTERN);
        Matcher m = p.matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            // res = sms.body.contains("Payment received");
            res = m.find();
        }
        return res;
    }

    private boolean isCashOut(Sms sms) {
        //Cash Out made for GHS40.00 to IT TAKES GRACE  VENTURES, Current Balance: GHS1560.04 Financial Transaction Id: 4571767875. Cash-out fee is charged automatically from your MTN MobileMoney wallet. Please do not pay any fees to the merchant. Thank you for using MTN MobileMoney. Fee charged: GHS0.50.
        boolean res = false;
        Matcher m = Pattern.compile(CASH_OUT_PATTERN).matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            //  res = sms.body.contains("Cash Out made");
            res = m.find();
        }
        return res;
    }

    private boolean isPaymentSent(Sms sms) {
        boolean res = false;
        Matcher m = Pattern.compile(PAYMENT_SENT_PATTERN).matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            // res = isPaymentSentFor(sms) || isPaymentSentMadeFor(sms);
            res= m.find();
        }
        return res;
    }

    private boolean isPaymentSentMadeFor(Sms sms) {
        boolean res = false;
        Matcher m = Pattern.compile("\\s*[pP]ayment\\s{0,3}[mM]ade\\s{0,3}[fF]or").matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            // res = sms.body.contains("Payment made for");
            res = m.find();
        }
        return res;
    }

    private boolean isPaymentSentFor(Sms sms) {
        boolean res = false;
        Matcher m = Pattern.compile("\\s*[pP]ayment\\s{0,3}[fF]or").matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            //res = sms.body.contains("Payment for");
            res=m.find();
        }
        return res;
    }

    private boolean isPaymentSentMtn(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            Matcher m = Pattern.compile("\\s*[yY]our\\s[pP]ayment\\s{0,3}[oO][of]").matcher(sms.body);
            //res = sms.body.contains("Your payment of");
            res=m.find();
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
        String ss;
        Pattern p = Pattern.compile(CASH_IN_RECEIVED_PATTERN);
        Matcher m = p.matcher(sms.body);
        m.find();
        int count = m.groupCount();
        ss = m.group(count);
        //String tt = "Cash In received for GHS ";
        //int st = -1;
        //int end = -1;

        /*if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length();
            end = sms.body.indexOf(" from", st);
        }
        ss = mSubstring(sms.body, st, end);*/
        /*try {
            ss = sms.body.substring(st, end);
        } catch(IndexOutOfBoundsException e){
            Log.d(ExtractMtnMomoInfo.class.getSimpleName(),"caused by sms:"+ sms+"\n"+ e.getMessage());
            return 0;
        }*/
        return (ss == null || ss.isEmpty()) ? 0 : Double.valueOf(ss);
    }

    private double getPaymentReceivedAmount(Sms sms) {
        if (!isPaymentReceived(sms)) {
            return 0;
        }

        double db;
        String tt = "Payment received for";
        int st;
        int end;
        String ss;
        if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length() + 1;
            int sm = sms.body.indexOf("GHS ", st) + 4;
            end = sms.body.indexOf(" from", sm);
            ss = mSubstring(sms.body, sm, end);

           /* try {
                ss = sms.body.substring(sm, end);
            } catch(IndexOutOfBoundsException e){
                Log.d(ExtractMtnMomoInfo.class.getSimpleName(),"caused by sms:"+ sms+"\n"+ e.getMessage());
                return 0;
            }*/
            db = (ss == null) ? 0 : Double.valueOf(ss);
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
        int st;
        int end;
        String ss;

        st = sms.body.indexOf(tt) + tt.length() + 1;
        //int sm = sms.body.indexOf("GHS ", st) + 4;
        end = sms.body.indexOf(endString, st);
        ss = mSubstring(sms.body, st, end);
        //ss = sms.body.substring(st, end);

        return (ss == null) ? "error" : ss;

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
        // ss = sms.body.substring(st, end);
        ss = mSubstring(sms.body, st, end);
        return (ss == null) ? 0 : Double.valueOf(ss);

    }

    private double getPaymentSentAmount(Sms sms) {
        if (!isPaymentSent(sms)) {
            return 0;
        }

        double db;
        String tt = isPaymentSentMadeFor(sms) ? "Payment made for" : "Payment for";

        int st;
        int end;
        String ss;
        if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length() + 1;
            int sm = sms.body.indexOf("GHS", st) + 3;
            end = sms.body.indexOf(" to", sm);
            //ss = sms.body.substring(sm, end);
            ss = mSubstring(sms.body, sm, end);

            db = (ss == null) ? 0 : Double.valueOf(ss);
            // db = Double.valueOf(ss);
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
        int st;
        int end;
        String ss;
        if (sms.body.contains(tt)) {
            st = sms.body.indexOf(tt) + tt.length() + 1;
            int sm = sms.body.indexOf("GHS ", st) + 4;
            end = sms.body.indexOf(" to", sm);
            //ss = sms.body.substring(sm, end);
            ss = mSubstring(sms.body, sm, end);
            db = (ss == null) ? 0 : Double.valueOf(ss);
            //db = Double.valueOf(ss);

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
        String startPattern = "from ";
        String endPattern = "Current Balance";
        int st = sms.body.indexOf(startPattern);
        int end = sms.body.indexOf(endPattern);
        String ss = mSubstring(sms.body, st + startPattern.length(), end);
        return (ss == null) ? "error" : ss;
        // return sms.body.substring(st + 5, end);
    }

    private String getReceiver(Sms sms) {
        if (!(isPaymentSent(sms) || isPaymentSentMtn(sms) || isCashOut(sms))) {
            return null;
        }
        String ss;
        if (isPaymentSentMtn(sms)) {
            String startPattern = "to ";
            String endPattern = "has";
            int st = sms.body.indexOf(startPattern);
            int end = sms.body.indexOf(endPattern, st);
            ss = mSubstring(sms.body, st + startPattern.length(), end);
            // ss = sms.body.substring(st + 3, end);

        } else {
            String startPattern = "to ";
            String endPattern = "Current Balance";
            int st = sms.body.indexOf(startPattern);
            int end = sms.body.indexOf(endPattern);
            // ss = sms.body.substring(st + startPattern.length(), end);
            ss = mSubstring(sms.body, st + startPattern.length(), end);
        }
        return (ss == null) ? "error" : ss;

    }


    private String getTxID(Sms sms) {
        String startStr;
        if (isCashIn(sms)) {
            return "none";
        } else if (isPaymentSentMtn(sms) || isCashOut(sms)) {
            startStr = "Financial Transaction Id: ";
            int st = sms.body.indexOf(startStr);
            int end = sms.body.indexOf(".", st);
            // return sms.body.substring(st + startStr.length(), end);
            String ss = mSubstring(sms.body, st + startStr.length(), end);
            return (ss == null) ? "error" : ss;
        }
        startStr = "Transaction ID: ";
        int st = sms.body.indexOf(startStr);
        int end = sms.body.indexOf(".", st);
        //return sms.body.substring(st + startStr.length(), end);
        String ss = mSubstring(sms.body, st + startStr.length(), end);
        return (ss == null) ? "error" : ss;

    }

    private double getIndividualCB(Sms sms) {
        boolean isPay = isPaymentReceived(sms);
        boolean isCashOut = isCashOut(sms);
        boolean isPaymentSentFor = isPaymentSentFor(sms);
        if (!(isPay || isCashIn(sms) || isCashOut || isPaymentSent(sms) || isPaymentSentMtn(sms))) {
            return -1;
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
        //String ss = sms.body.substring(md + 4, end);
        String ss = mSubstring(sms.body, md + 4, end);

        //ss = ss.trim();
        // return Double.valueOf(ss`````);
        return (ss == null) ? 0 : Double.valueOf(ss.trim());
    }

    public double getLatestBalance() {
        double currentBal = 0;
        if (shouldLoad(CURRENT_BALANCE)) {
            int msgSize = msgList.size();
            Sms sms;
            for (int i = 0; i < msgSize; i++) {
                sms = (Sms) msgList.get(i);
                if (isMobileMoneyMsg(sms)) {
                    currentBal = getIndividualCB(sms);
                    if (currentBal >= 0.0) {
                        sharedPref.StoreCurrentBalance(currentBal, sms.receivedDate);
                        break;
                    }
                }
            }
        } else {
            currentBal = sharedPref.getLastCurrentBalAmount();
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

    /**
     * check whether there have been a new update from the last time
     * @param whichState the state to check
     *                   {@link #CURRENT_BALANCE}
     *                   {@link #TOTAL_RECEIVED}
     *                   {@link #TOTAL_SENT}
     * @return true if we to load from db else false
     */
    private boolean shouldLoad(int whichState) {

        if (msgList == null) {
            return true;
        }
        switch (whichState) {
            case CURRENT_BALANCE: {
                if (sharedPref.getLastCurrentBalAmount() == -1) return true;
                break;
            }
            case TOTAL_RECEIVED: {
                if (sharedPref.getTotalReceived() == -1) return true;
                break;
            }
            case TOTAL_SENT: {
                if (sharedPref.getTotalSent() == -1) return true;
                break;
            }
        }
        Sms currentMessage = (Sms) msgList.get(0);
        Long currentMsgDate = currentMessage.receivedDate;

        long lastDate = sharedPref.getLastCurentBalDate();

        //new momo Msg is present reload curentBalance
        return currentMsgDate > lastDate;

    }

    @Nullable
    private String mSubstring(String str, int start, int end) {
        String ss;
        try {
            ss = str.substring(start, end);
        } catch (IndexOutOfBoundsException e) {
            Log.d(ExtractMtnMomoInfo.class.getSimpleName(), "caused by sms:" + str + "\n" + e.getMessage());
            return null;
        }
        return ss;
    }

}
