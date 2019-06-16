package com.mazitekgh.momorecords;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mazitekgh.momorecords.fragment.MomoDetailFragment;
import com.mazitekgh.momorecords.model.Momo;
import com.mazitekgh.momorecords.model.Sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Zakaria on 01-Sep-18 at 4:03 PM.
 */
public class ExtractMtnMomoInfo {
    private static final String CASH_IN_RECEIVED_PATTERN = "\\s*[cC]ash\\s{1,3}[iI]n\\s{1,3}received\\s{0,3}for\\s{0,3}GHS\\s{0,3}([\\d]*[.]?[\\d]*)";

    /**
     * Pattern to extract current balance from momo message
     * extracted amount is at group 1
     */
    private static final String CURRENT_BALANCE_PATTERN = "(?:(?:[Nn]ew)|(?:[Cc]urrent))\\s[Bb]alance[\\s\\S]{0,3}GHS\\s{0,3}([\\d]*[.]?[\\d]*)";

    /**
     * Pattern to extract receive amount from momo message
     * extracted amount is at group 1
     */
    private static final String RECEIVED_AMOUNT_PATTERN = "[Rr]eceived\\s(?:for)?\\s*(?:GHS)?\\s*([\\d]*[.]?[\\d]*)\\s*(?:GHS)? [Ff]rom";
    private static final String REFERENCE_PATTERN = "[Rr]eference:\\s([\\w\\W]*)..(?=[T]ransaction\\s*ID)";
    private static final String PAYMENT_RECEIVED_PATTERN = "\\s*[P]ayment\\s{1,3}[rR]eceived\\s{1,3}for";
    /**
     *pattern for all received momo
     */
    private static final String ALL_RECEIVE_PAYMENT_PATTERN = "(?:([Pp]ayment)|(Cash\\s*In)|(you\\s*have))\\s*[rR]eceived";
    private Pattern currentBalancePattern, receiveAmountPattern, allPaymentReceived, referencePattern;
    public ExtractMtnMomoInfo(Context c) {
        // if(shouldLoad()) {
        //TelephonyProvider telephonyProvider = new TelephonyProvider(c);
        sharedPref = new SharedPref(c);
        //Data d = telephonyProvider.getSms(TelephonyProvider.Filter.INBOX);
        //msgList = d.getList();
        // msgList = getOnlyMomoSMS();
        msgList = new SmsContent(c).getSmsList();

        compilePattern();

    }

    public static final int CURRENT_BALANCE = 0;

    private void compilePattern() {
        receivedPattern = Pattern.compile(ReceivePattern.RECEIVED_PATTERN);
        receiveAmountPattern = Pattern.compile(RECEIVED_AMOUNT_PATTERN);
        cashInPattern = Pattern.compile(CashInPattern.CASH_IN_PATTERN);
        cashInReceivedPattern = Pattern.compile(CASH_IN_RECEIVED_PATTERN);
        paymentReceivedPattern = Pattern.compile(PAYMENT_RECEIVED_PATTERN);
        allPaymentReceived = Pattern.compile(ALL_RECEIVE_PAYMENT_PATTERN);
        paymentSentPattern = Pattern.compile(PAYMENT_SENT_PATTERN);
        cashOutPattern = Pattern.compile(CASH_OUT_PATTERN);
        currentBalancePattern = Pattern.compile(CURRENT_BALANCE_PATTERN);
        referencePattern = Pattern.compile(REFERENCE_PATTERN);
        sentMtnPattern = Pattern.compile("\\s*[yY]our\\s[pP]ayment\\s{0,3}[oO][of]");
        paymentMadeFor = Pattern.compile("\\s*[pP]ayment\\s{0,3}[mM]ade\\s{0,3}[fF]or");
        paymentSentForPattern = Pattern.compile("\\s*[pP]ayment\\s{0,3}[fF]or");
    }

    private static final String CASH_OUT_PATTERN = "\\s*[cC]ash\\s{0,3}[oO]ut\\s{1,3}made";
    public static final int TOTAL_RECEIVED = 1;
    public static final int TOTAL_SENT = 2;
    private static final String PAYMENT_SENT_PATTERN = "\\s*[pP]ayment\\s{0,3}([mM]ade)?\\s{0,3}[fF]or";
    private static final String PAYMENT_SENTFOR_PATTERN = "\\s*[pP]ayment\\s{0,3}([mM]ade)?\\s{0,3}[fF]or";
    private List msgList;
    private final SharedPref sharedPref;
    private Pattern receivedPattern, cashInPattern, cashInReceivedPattern, paymentReceivedPattern;

    public List<Sms> getOnlyMomoSMS() {
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

    private Pattern cashOutPattern, paymentSentPattern, sentMtnPattern, paymentMadeFor, paymentSentForPattern;

    public double getTotalReceived() {
        if (msgList == null || msgList.size() <= 0) {
            return 0;
        }
        double amount = 0.00;
        if (shouldLoad(TOTAL_RECEIVED)) {
            Sms sms;
            for (int i = 0; i < msgList.size(); i++) {
                sms = (Sms) msgList.get(i);
                getReference(sms);
                //  amount += getCashInReceivedAmount(sms) + getPaymentReceivedAmount(sms);
                amount += getReceivedAmount(sms);
            }
            sharedPref.storeTotalReceivedAmount(amount);
        } else
        {
            amount = sharedPref.getTotalReceived();
        }

        return amount;
    }

    public ExtractMtnMomoInfo(Context context, List msgList) {
        this.msgList = msgList;
        sharedPref = new SharedPref(context);
        compilePattern();
    }

    public List<Momo> getMessages(int whichMomo) {
        List<Momo> myResList = new ArrayList<>();
        switch (whichMomo) {
            case MomoDetailFragment.ALL_MOMO: {
                myResList = getAllMomoMessages();
                break;
            }
            case MomoDetailFragment.RECEIVED_MOMO: {
                myResList = getReceivedMomoMessages();
                break;
            }
            case MomoDetailFragment.SENT_MOMO: {
                myResList = getSentMomoMessages();
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

    /**
     * Get Received momo messages
     *
     * @return List<Momo>
     */
    public List<Momo> getReceivedMomoMessages() {
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

    public List<Momo> getSentMomoMessages() {
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

    public double getTotalSent() {
        if (msgList == null || msgList.size() <= 0) {
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

    public List<Momo> getAllMomoMessages() {
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

    public Momo getMomo(Sms sms) {
        Momo momoSent = getSentMomo(sms);
        Momo momoReceived = getReceivedMomo(sms);
        Momo momoCredit = getCreditMomo(sms);
        if (momoSent != null) {
            return momoSent;
        } else if (momoReceived != null) {
            return momoReceived;
        } else if (momoCredit != null) {
            return momoCredit;
        }
        return null;
    }

    public Momo getReceivedMomo(Sms sms) {
        Momo momo = new Momo();
        Date d = new Date();

        SimpleDateFormat sdf;
        try {
            sdf = new SimpleDateFormat("E d/M/y h:m:s a", Locale.getDefault());
        } catch (IllegalArgumentException e) {
            sdf = new SimpleDateFormat();
        }

        if (receivedMessage(sms) != null) {

            Matcher m = receivedPattern.matcher(sms.body);
            if (m.find()) {
                momo.setAmount(m.group(ReceivePattern.GROUP_AMOUNT));
                momo.setSender("FROM: " + m.group(ReceivePattern.GROUP_SENDER));
                momo.setCurrentBalance(m.group(ReceivePattern.GROUP_CURRENT_BAL));
                momo.setTxID(m.group(ReceivePattern.GROUP_TXID));
                momo.setContentStr(sms.body);
                momo.setReference(m.group(ReceivePattern.GROUP_REFERENCE));
            }
            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            momo.setType(MomoDetailFragment.RECEIVED_MOMO);
        } else if (cashInMessage(sms) != null) {

            Matcher m = cashInPattern.matcher(sms.body);
            if (m.find()) {
                momo.setAmount(m.group(CashInPattern.GROUP_CASH_IN_AMOUNT));
                momo.setSender("FROM: " + m.group(CashInPattern.GROUP_CASH_IN_SENDER));
                momo.setCurrentBalance(m.group(CashInPattern.GROUP_CASH_IN_CURRENT_BAL));
            }
            d.setTime(sms.receivedDate);
            momo.setDateStr(sdf.format(d));
            momo.setContentStr(sms.body);

            momo.setTxID(getTxID(sms)); //none
            //momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(MomoDetailFragment.RECEIVED_MOMO);

        } else {
            return null;
        }

        return momo;
    }

    /**
     * Check whether the message is momo message
     *
     * @param sms message to check
     * @return true if momo message or false if not momo message
     */
    public boolean isMobileMoneyMsg(Sms sms) {
        return (sms.address.equalsIgnoreCase("MobileMoney"));
    }

    private boolean isCashIn(Sms sms) {
//        boolean res = false;
//
//        Matcher m = cashInReceivedPattern.matcher(sms.body);
//
//        if (isMobileMoneyMsg(sms)) {
//
//            res = m.find();
//
//        }
//        return res;
        return isReceivedMomo(sms, ReceiveMOMOType.CASH_IN);
    }

    /**
     * check if the given sms is received momo message
     * and the type of momo message it is
     *
     * @param sms  the sms message to check
     * @param type int type of momo message <br/>
     *             0. check for all
     *             1.payment received<br/>
     *             2.cash in received<br/>
     *             3.you have received
     * @return boolean true if it is a momo received message
     */
    public boolean isReceivedMomo(Sms sms, ReceiveMOMOType type) {
        if (!isMobileMoneyMsg(sms)) {
            return false;
        }
        boolean res = false;
        Matcher m = allPaymentReceived.matcher(sms.body);
        if (type.ordinal() == ReceiveMOMOType.ALL.ordinal()) {
            res = m.find();
        } else {
            if (m.find()) {
                String s = m.group(type.ordinal());
                res = s != null;
            }
        }
        return res;
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
     * check if the given sms is received momo message
     * @param sms the sms message to check
     * @return boolean true if it is a momo received message
     */
    public boolean isReceivedMomo(Sms sms) {
        return isReceivedMomo(sms, ReceiveMOMOType.ALL);
    }

    private boolean isPaymentReceived(Sms sms) {
//         boolean res = false;
//        // Payment received for  GHS 200.00 from UMB Bank OVA Current Balance: GHS 1716.14 . Available Balance: GHS 1716.14. Reference: Credit MTN Customer. Transaction ID: 4772674558. TRANSACTION FEE: 0.00
//        //Pattern p = Pattern.compile(PAYMENT_RECEIVED_PATTERN);
//        Matcher m = paymentReceivedPattern.matcher(sms.body);
//        if (isMobileMoneyMsg(sms)) {
//            // res = sms.body.contains("Payment received");
//            res = m.find();
//        }

        return isReceivedMomo(sms, ReceiveMOMOType.PAYMENT_RECEIVED);
    }

    public boolean isCashOut(Sms sms) {
        //Cash Out made for GHS40.00 to IT TAKES GRACE  VENTURES, Current Balance: GHS1560.04 Financial Transaction Id: 4571767875. Cash-out fee is charged automatically from your MTN MobileMoney wallet. Please do not pay any fees to the merchant. Thank you for using MTN MobileMoney. Fee charged: GHS0.50.
        boolean res = false;
        Matcher m = cashOutPattern.matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            //  res = sms.body.contains("Cash Out made");
            res = m.find();
        }
        return res;
    }

    public boolean isPaymentSent(Sms sms) {
        boolean res = false;
        Matcher m = paymentSentPattern.matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            res= m.find();
        }
        return res;
    }

    /**
     * Get amount received from momo sms either payment received or cash in momo message
     *
     * @param sms the sms to check
     * @return double the amount receive
     */
    public double getReceivedAmount(Sms sms) {
        //if it is not cash in or payment received return 0
//        if(!(isCashIn(sms) || isPaymentReceived(sms))){
//            return  0;
//        }
        if (!isReceivedMomo(sms)) {
            return 0;
        }
        String ss = "0.0";
        Matcher m = receiveAmountPattern.matcher(sms.body);
        if (m.find()) {
            ss = m.group(1);
        }
        return Double.valueOf(ss.trim());
    }

    /**
     * get reference from payment receive momo message;
     *
     * @param sms the sms to check
     * @return the reference or null ii cant find
     */
    public String getReference(Sms sms) {
        if (!isPaymentReceived(sms)) {
            return null;
        }
//
//        String tt = "Reference:";
//        String endString = ". Transaction";
//        int st;
//        int end;
//        String ss;
//
//        st = sms.body.indexOf(tt) + tt.length() + 1;
//        //int sm = sms.body.indexOf("GHS ", st) + 4;
//        end = sms.body.indexOf(endString, st);
//        ss = mSubstring(sms.body, st, end);
//        //ss = sms.body.substring(st, end);
//
//        return (ss == null) ? "error" : ss;
        String ss = null;
        Matcher m = referencePattern.matcher(sms.body);
        if (m.find()) {
            ss = m.group(1);
        }
        return ss;
    }

    /**
     * getAmount sent from a given sms
     * @param sms sms to search for amount
     * @return the cash out/sent amount, null if cant detect amount
     */
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

        ss = mSubstring(sms.body, st, end);
        return (ss == null) ? 0 : Double.valueOf(ss);

    }

    private boolean isPaymentSentMadeFor(Sms sms) {
        boolean res = false;
        Matcher m = paymentMadeFor.matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            // res = sms.body.contains("Payment made for");
            res = m.find();
        }
        return res;
    }

    private boolean isPaymentSentFor(Sms sms) {
        boolean res = false;
        Matcher m = paymentSentForPattern.matcher(sms.body);
        if (isMobileMoneyMsg(sms)) {
            //res = sms.body.contains("Payment for");
            res = m.find();
        }
        return res;
    }

    private boolean isPaymentSentMtn(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            Matcher m = sentMtnPattern.matcher(sms.body);
            //res = sms.body.contains("Your payment of");
            res = m.find();
            if (res && sms.body.contains("failed")) {
                res = false;
            }
        }
        return res;
    }

//
//    //receive amount
//    private double getCashInReceivedAmount(Sms sms) {
//        if (!isCashIn(sms)) {
//            return 0;
//        }
//        String ss = null;
//        Pattern p = Pattern.compile(CASH_IN_RECEIVED_PATTERN);
//        Matcher m = p.matcher(sms.body);
//        if (m.find()) {
//            int count = m.groupCount();
//            ss = m.group(count);
//        }
//
//        return (ss == null || ss.isEmpty()) ? 0 : Double.valueOf(ss);
//    }
//
//    private double getPaymentReceivedAmount(Sms sms) {
//        if (!isPaymentReceived(sms)) {
//            return 0;
//        }
//
//        double db;
//        String tt = "Payment received for";
//        int st;
//        int end;
//        String ss;
//        if (sms.body.contains(tt)) {
//            st = sms.body.indexOf(tt) + tt.length() + 1;
//            int sm = sms.body.indexOf("GHS ", st) + 4;
//            end = sms.body.indexOf(" from", sm);
//            ss = mSubstring(sms.body, sm, end);
//            db = (ss == null) ? 0 : Double.valueOf(ss);
//        } else {
//            db = 0;
//        }
//
//        return db;
//    }

    /**
     * get momo sender from given sms
     * @param sms the sms to check
     * @return String of the sender
     */
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

    /**
     * get momo receiver from given sms
     * @param sms the sms to check
     * @return String of the receiver
     */
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

    private double getIndividualCB(Sms sms) {
        boolean isPay = isPaymentReceived(sms);
        boolean isCashOut = isCashOut(sms);
        // boolean isPaymentSentFor = isPaymentSentFor(sms);
        if (!(isPay || isCashIn(sms) || isCashOut || isPaymentSent(sms) || isPaymentSentMtn(sms))) {
            return -1;
        }
        String amountStr = "0.0";
//        String firstPattern = "Current Balance";
//        String endPattern; // = isPay ? ". Available Balance" : "Available Balance";
//        if (isPay) {
//            endPattern = ". Available Balance";
//        } else if (isCashOut) {
//            endPattern = "Financial";
//        } else if (isPaymentSentMtn(sms)) {
//            firstPattern = "balance:";
//            endPattern = ". Fee";
//        } else if (isPaymentSentFor) {
//            //firstPattern = "balance:";
//            endPattern = ". Transaction";
//        } else {
//            endPattern = isPaymentSentMadeFor(sms) ? ". Available Balance" : "Available Balance";
//        }
//        int st = sms.body.indexOf(firstPattern) + firstPattern.length();
//        int end = sms.body.indexOf(endPattern, st);
//        int md = isCashOut ? sms.body.indexOf("GHS", st) - 1 : sms.body.indexOf("GHS ", st); //portable
//
//        String ss = mSubstring(sms.body, md + 4, end);
//
        Matcher m = currentBalancePattern.matcher(sms.body);
        if (m.find()) {
            amountStr = m.group(1);
        }

        return Double.valueOf(amountStr.trim());
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

    /**
     * check whether there have been a new update from the last time
     * @param whichState the state to check can be one of this <br />
     *                   {@link #CURRENT_BALANCE},
     *                   {@link #TOTAL_RECEIVED},
     *                   {@link #TOTAL_SENT}
     * @return true if it should load from db else false
     */
    private boolean shouldLoad(int whichState) {

        if (msgList == null) {
            return true;
        }
        long lastDate = 0;
        switch (whichState) {
            case CURRENT_BALANCE: {
                if (sharedPref.getLastCurrentBalAmount() == -1) {
                    return true;
                } else {
                    lastDate = sharedPref.getLastCurentBalDate();
                }
                break;
            }
            case TOTAL_RECEIVED: {
                if (sharedPref.getTotalReceived() == -1) {
                    return true;
                } else {
                    lastDate = sharedPref.getLastTotalReceivedDate();
                }
                break;
            }
            case TOTAL_SENT: {
                if (sharedPref.getTotalSent() == -1) {
                    return true;
                } else {
                    lastDate = sharedPref.getLastTotalSentDate();
                }
                break;
            }
        }
        if (msgList.size() <= 0) {
            return true;
        }
        //get the latest momo message
        Sms currentMessage = (Sms) msgList.get(0);
        Long currentMsgDate = currentMessage.receivedDate;

        //new momo Msg is present reload curentBalance
        boolean isload = currentMsgDate > lastDate;
        if (isload) {
            sharedPref.storeCurrentMessageDate(whichState, currentMsgDate);
        }
        return isload;
    }

    private enum ReceiveMOMOType {
        ALL,
        PAYMENT_RECEIVED,
        CASH_IN,
        INTEREST
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

    /**
     * CASH IN PATTERN
     * group 1: amount received
     * group 2: sender
     * group 3: current balance
     */
    private static final class CashInPattern {
        static final String CASH_IN_PATTERN =
                "\\s*[cC]ash\\s{1,3}[iI]n\\s{1,3}[rR]eceived\\s{0,3}for\\s{0,3}GHS\\s{0,3}([\\d]*[.]?[\\d]*)\\s{0,3}from\\s{0,3}([\\w\\s.\\W]*)\\s{0,3}[cC]urrent\\s{0,3}[bB]alance[\\W]?\\s{0,3}GHS\\s{0,3}([\\d]*[.]?[\\d]*)\\s";
        static final int GROUP_CASH_IN_AMOUNT = 1;
        static final int GROUP_CASH_IN_SENDER = 2;
        static final int GROUP_CASH_IN_CURRENT_BAL = 3;
    }

    public double getLatestBalance() {
        double currentBal = 0;
        if (msgList == null || msgList.size() <= 0) {
            return 0;
        }
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

    /**
     * RECEIVED PATTERN
     * group 1: amount received
     * group 2: sender
     * group 3: current balance
     * group 4: reference
     * group 5: transaction id
     * group 6: transaction fee
     */
    private static final class ReceivePattern {
        static final String RECEIVED_PATTERN =
                "\\s*[pP]ayment\\s{1,3}[rR]eceived\\s{0,3}for\\s{0,3}GHS\\s{0,3}([\\d]*[.]?[\\d]*)\\s{0,3}from\\s{0,3}([\\w\\s.\\W]*)\\s{0,3}[cC]urrent\\s{0,3}[bB]alance[\\W]?\\s{0,3}GHS\\s{0,3}([\\d]*[.]?[\\d]*)\\s{0,3}[\\w\\W]*[rR]eference[\\W]?\\s{0,3}([\\w\\W]*)\\s{0,3}[tT]ransaction\\s{0,3}[Ii][Dd]\\W?\\s{0,3}([\\d]*)[\\w\\W]*\\s[tT][rR][aA][nN][sS][aA][cC][tT][iI][oO][nN] [feFE]{3}\\W?([\\d.]*)[\\w\\W]*";

        static final int GROUP_AMOUNT = 1;
        static final int GROUP_SENDER = 2;
        static final int GROUP_CURRENT_BAL = 3;
        static final int GROUP_REFERENCE = 4;
        static final int GROUP_TXID = 5;
        static final int GROUP_TX_FEE = 6;
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
