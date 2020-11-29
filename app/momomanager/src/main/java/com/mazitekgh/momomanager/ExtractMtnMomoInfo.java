package com.mazitekgh.momomanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.mazitekgh.momomanager.model.Momo;
import com.mazitekgh.momomanager.model.Sms;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CURRENT_BALANCE, TOTAL_RECEIVED, TOTAL_SENT})
    public @interface TotalAmountType {
    }

    public static final int CURRENT_BALANCE = 0;
    public static final int TOTAL_RECEIVED = 1;
    public static final int TOTAL_SENT = 2;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ALL_MOMO, RECEIVED_MOMO, SENT_MOMO, CREDIT_MOMO})
    public @interface MomoType {
    }
    public static final int ALL_MOMO = 0;
    public static final int RECEIVED_MOMO = 1;
    public static final int SENT_MOMO = 2;
    public static final int CREDIT_MOMO = 3;


    private Pattern cashOutPattern, paymentSentPattern, sentMtnPattern, paymentMadeFor, paymentSentForPattern;
    private Pattern currentBalancePattern, receiveAmountPattern, allPaymentReceived, referencePattern;
    private Pattern receivedPattern, cashInPattern, cashInReceivedPattern, paymentReceivedPattern;

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
     * pattern for all received momo
     */
    private static final String ALL_RECEIVE_PAYMENT_PATTERN = "(?:([Pp]ayment)|(Cash\\s*In)|(you\\s*have))\\s*[rR]eceived";

    private static final String CASH_OUT_PATTERN = "\\s*[cC]ash\\s{0,3}[oO]ut\\s{1,3}made";

    private static final String PAYMENT_SENT_PATTERN = "\\s*[pP]ayment\\s{0,3}([mM]ade)?\\s{0,3}[fF]or";
    //private static final String PAYMENT_SENTFOR_PATTERN = "\\s*[pP]ayment\\s{0,3}([mM]ade)?\\s{0,3}[fF]or";
    private final List<Sms> smsList;


    public ExtractMtnMomoInfo(Context c) {
        smsList = new SmsContent(c, "MobileMoney").getSmsList();
        compilePattern();
    }

//    public ExtractMtnMomoInfo(Context context, List msgList) {
//        this.msgList = msgList;
//        // sharedPref = new SharedPref(context);
//        compilePattern();
//    }


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


    //private final SharedPref sharedPref;


//    public List<Sms> getOnlyMomoSMS() {
//        List<Sms> resList = new ArrayList<>();
//        Sms sms;
//        for (int i = 0; i < msgList.size(); i++) {
//            sms = (Sms) msgList.get(i);
//            if (isMobileMoneyMsg(sms)) {
//                resList.add(sms);
//            }
//
//        }
//        return resList;
//    }


    /**
     * Get Total amount received so far
     * sums all the received momo amount
     *
     * @return double the total amount
     */
    public double getTotalReceived() {
        //TODO: 25-Nov-20 remove duplicate sms that is sms with the same txID
        if (smsList == null || smsList.size() <= 0) {
            return 0;
        }
        double amount = 0.00;
        for (Sms sms : smsList) {
            amount += getReceivedAmount(sms);
        }
        return amount;
    }


    /**
     * Get All momo messages
     *
     * @param whichMomo type of momo Message to retrieve
     * @return List<Momo> list of momo messages
     */
    public List<Momo> getMessages(@MomoType int whichMomo) {
        List<Momo> momoList;
        switch (whichMomo) {
            case ALL_MOMO: {
                momoList = getAllMomoMessages();
                break;
            }
            case RECEIVED_MOMO: {
                momoList = getReceivedMomoMessages();
                break;
            }
            case SENT_MOMO: {
                momoList = getSentMomoMessages();
                break;
            }
            case CREDIT_MOMO: {
                momoList = getCreditMessages();
                break;
            }
            default: {
                momoList = null;
            }
        }
        return momoList;
    }

    /**
     * Get Received momo messages
     *
     * @return List<Momo>
     */
    public List<Momo> getReceivedMomoMessages() {
        List<Momo> receivedMomoList = new ArrayList<>();
        for (Sms sms : smsList) {
            Momo momo = getReceivedMomo(sms);
            if (momo != null) {
                receivedMomoList.add(momo);
            }
        }
        return receivedMomoList;
    }

    public List<Momo> getSentMomoMessages() {
        List<Momo> sentMessages = new ArrayList<>();
        for (Sms sms : smsList) {

            Momo momo = getSentMomo(sms);
            if (momo != null) {
                sentMessages.add(momo);
            }
        }
        return sentMessages;
    }

    public double getTotalSent() {
        if (smsList == null || smsList.size() <= 0) {
            return 0;
        }
        double amount = 0.00;

        Sms sms;
        for (int i = 0; i < smsList.size(); i++) {
            sms = smsList.get(i);
            amount += getCashOutAmount(sms) + getPaymentSentAmount(sms);
        }

        // TODO: 06-Nov-20 set error

        return amount;
    }

    public List<Momo> getAllMomoMessages() {
        List<Momo> allMsgs = new ArrayList<>();

        for (Sms sms : smsList) {

            Momo momo = getMomo(sms);
            if (momo == null) continue;
            allMsgs.add(momo);
        }

        return allMsgs;
    }

    private List<Momo> getCreditMessages() {
        List<Momo> creditMessages = new ArrayList<>();
        Sms sms;
        for (int i = 0; i < smsList.size(); i++) {
            sms = smsList.get(i);
            Momo momo = getCreditMomo(sms);
            if (momo != null) {
                creditMessages.add(momo);
            }
        }
        return creditMessages;
    }

    //todo tweak to select the type of momo to get
    public Momo getMomo(Sms sms) {
        Momo momoSent = getSentMomo(sms);
        Momo momoReceived = getReceivedMomo(sms);
        Momo momoCredit = getCreditMomo(sms);
        if (momoSent != null) {
            return momoSent;
        } else if (momoReceived != null) {
            return momoReceived;
        } else return momoCredit;
    }

    /**
     * Parse sms and check if it is a received momo and return it else return null
     *
     * @param sms the sms to parse
     * @return receive Momo or null if is not receive momo
     */
    public Momo getReceivedMomo(Sms sms) {
        Momo momo = new Momo();
        Date d = new Date();

        SimpleDateFormat sdf;
        try {
            sdf = new SimpleDateFormat("E d/M/y h:m:s a", Locale.getDefault());
        } catch (IllegalArgumentException e) {
            //let's try again with old constructor
            sdf = new SimpleDateFormat();
        }
        // TODO: 25-Nov-20 is this check really important
        if (receivedMessage(sms) != null) {

            Matcher m = receivedPattern.matcher(sms.getBody());
            if (m.find()) {
                String amountStr = m.group(ReceivePattern.GROUP_AMOUNT);
                if (amountStr != null) {
                    // TODO: 25-Nov-20 catch null pointer of parseDouble
                    momo.setAmount(Double.parseDouble(amountStr.trim()));
                }
                momo.setSender(m.group(ReceivePattern.GROUP_SENDER));
                momo.setCurrentBalance(m.group(ReceivePattern.GROUP_CURRENT_BAL));
                momo.setTxID(m.group(ReceivePattern.GROUP_TXID));
                momo.setContent(sms.getBody());
                momo.setReference(m.group(ReceivePattern.GROUP_REFERENCE));
            }
            d.setTime(sms.getReceivedDate());
            momo.setDate(sdf.format(d));
            momo.setType(RECEIVED_MOMO);
        } else if (cashInMessage(sms) != null) {

            Matcher m = cashInPattern.matcher(sms.getBody());
            if (m.find()) {
                String amountStr = m.group(CashInPattern.GROUP_CASH_IN_AMOUNT);
                if (amountStr != null) {
                    // TODO: 25-Nov-20 catch null pointer of parseDouble
                    momo.setAmount(Double.parseDouble(amountStr.trim()));
                }
                momo.setSender("FROM: " + m.group(CashInPattern.GROUP_CASH_IN_SENDER));
                momo.setCurrentBalance(m.group(CashInPattern.GROUP_CASH_IN_CURRENT_BAL));
            }
            d.setTime(sms.getReceivedDate());
            momo.setDate(sdf.format(d));
            momo.setContent(sms.getBody());

            momo.setTxID(getTxID(sms)); //none
            //momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(RECEIVED_MOMO);

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
        return (sms.getAddress().equalsIgnoreCase("MobileMoney"));
    }

    private boolean isCashIn(Sms sms) {
        return isReceivedMomo(sms, RECEIVED_MOMO);
    }

    /**
     * check if the given sms is received momo message
     * and the type of momo message it is
     *
     * @param sms  the sms message to check
     * @param type type of momo message one of MomoType <br/>
     *             <ol>
     *             <li>check for all</li>
     *             <li>payment received</li>
     *             <li>cash in received</li>
     *             <li>you have received</li>
     *             </ol>
     * @return boolean true if it is a momo received message
     */
    public boolean isReceivedMomo(Sms sms, @MomoType int type) {
        if (!isMobileMoneyMsg(sms)) {
            return false;
        }
        boolean res = false;
        Matcher m = allPaymentReceived.matcher(sms.getBody());
        if (type == ALL_MOMO) {
            res = m.find();
        } else {
            if (m.find()) {
                String s = m.group(type);
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
            d.setTime(sms.getReceivedDate());
            momo.setDate(sdf.format(d));
            momo.setContent(paymentSentMessage(sms));
            momo.setAmount(getPaymentSentAmount(sms));
            momo.setSender(getReceiver(sms));
            momo.setTxID(getTxID(sms));
            momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(SENT_MOMO);

        } else if (cashOutMessage(sms) != null) {
            d.setTime(sms.getReceivedDate());
            momo.setDate(sdf.format(d));
            momo.setContent(cashOutMessage(sms));
            momo.setAmount(getCashOutAmount(sms));
            momo.setSender(getReceiver(sms));
            momo.setTxID(getTxID(sms));
            momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(SENT_MOMO);  //todo distinguish it from above one
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
            d.setTime(sms.getReceivedDate());
            momo.setDate(sdf.format(d));
            momo.setContent(paymentSentMessageMtn(sms));
            momo.setAmount(getPaymentSentAmountMtn(sms));
            momo.setSender(getReceiver(sms));
            momo.setTxID(getTxID(sms));
            momo.setCurrentBalance(String.valueOf(getIndividualCB(sms)));
            momo.setType(CREDIT_MOMO);

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
        return isReceivedMomo(sms, ALL_MOMO);
    }

    private boolean isPaymentReceived(Sms sms) {
//         boolean res = false;
//        // Payment received for  GHS 200.00 from UMB Bank OVA Current Balance: GHS 1716.14 . Available Balance: GHS 1716.14. Reference: Credit MTN Customer. Transaction ID: 4772674558. TRANSACTION FEE: 0.00
//        //Pattern p = Pattern.compile(PAYMENT_RECEIVED_PATTERN);
//        Matcher m = paymentReceivedPattern.matcher(sms.getBody());
//        if (isMobileMoneyMsg(sms)) {
//            // res = sms.getBody().contains("Payment received");
//            res = m.find();
//        }

        return isReceivedMomo(sms, RECEIVED_MOMO);
    }

    public boolean isCashOut(Sms sms) {
        //Cash Out made for GHS40.00 to IT TAKES GRACE  VENTURES, Current Balance: GHS1560.04 Financial Transaction Id: 4571767875. Cash-out fee is charged automatically from your MTN MobileMoney wallet. Please do not pay any fees to the merchant. Thank you for using MTN MobileMoney. Fee charged: GHS0.50.
        boolean res = false;
        Matcher m = cashOutPattern.matcher(sms.getBody());
        if (isMobileMoneyMsg(sms)) {
            //  res = sms.getBody().contains("Cash Out made");
            res = m.find();
        }
        return res;
    }

    public boolean isPaymentSent(Sms sms) {
        boolean res = false;
        Matcher m = paymentSentPattern.matcher(sms.getBody());
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
        Matcher m = receiveAmountPattern.matcher(sms.getBody());
        if (m.find()) {
            ss = m.group(1);
        }
        return Double.parseDouble(ss.trim());
    }

    /**
     * get reference from payment receive momo message;
     *
     * @param sms the sms to check
     * @return the reference or null ii cant find
     * @TODO: 24-Nov-20 it includes the dot at the end of reference remove it
     */
    public String getReference(Sms sms) {
        if (!isPaymentReceived(sms)) {
            return null;
        }
        String ss = null;
        Matcher m = referencePattern.matcher(sms.getBody());
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
        if (sms.getBody().contains(tt)) {
            st = sms.getBody().indexOf(tt) + tt.length();
            end = sms.getBody().indexOf(" to", st);
        }

        ss = mSubString(sms.getBody(), st, end);
        return (ss == null) ? 0 : Double.parseDouble(ss);

    }

    private boolean isPaymentSentMadeFor(Sms sms) {
        boolean res = false;
        Matcher m = paymentMadeFor.matcher(sms.getBody());
        if (isMobileMoneyMsg(sms)) {
            // res = sms.getBody().contains("Payment made for");
            res = m.find();
        }
        return res;
    }

    private boolean isPaymentSentFor(Sms sms) {
        boolean res = false;
        Matcher m = paymentSentForPattern.matcher(sms.getBody());
        if (isMobileMoneyMsg(sms)) {
            //res = sms.getBody().contains("Payment for");
            res = m.find();
        }
        return res;
    }

    private boolean isPaymentSentMtn(Sms sms) {
        boolean res = false;
        if (isMobileMoneyMsg(sms)) {
            Matcher m = sentMtnPattern.matcher(sms.getBody());
            //res = sms.getBody().contains("Your payment of");
            res = m.find();
            if (res && sms.getBody().contains("failed")) {
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
//        Matcher m = p.matcher(sms.getBody());
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
//        if (sms.getBody().contains(tt)) {
//            st = sms.getBody().indexOf(tt) + tt.length() + 1;
//            int sm = sms.getBody().indexOf("GHS ", st) + 4;
//            end = sms.getBody().indexOf(" from", sm);
//            ss = mSubstring(sms.getBody(), sm, end);
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
     * @return the sender
     */
    private String getSender(Sms sms) {
        if (!(isPaymentReceived(sms)) && !(isCashIn(sms))) {
            return null;
        }
        String startPattern = "from ";
        String endPattern = "Current Balance";
        int st = sms.getBody().indexOf(startPattern);
        int end = sms.getBody().indexOf(endPattern);
        String ss = mSubString(sms.getBody(), st + startPattern.length(), end);
        return (ss == null) ? "error" : ss;
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
            int st = sms.getBody().indexOf(startPattern);
            int end = sms.getBody().indexOf(endPattern, st);
            ss = mSubString(sms.getBody(), st + startPattern.length(), end);
            // ss = sms.getBody().substring(st + 3, end);

        } else {
            String startPattern = "to ";
            String endPattern = "Current Balance";
            int st = sms.getBody().indexOf(startPattern);
            int end = sms.getBody().indexOf(endPattern);
            // ss = sms.getBody().substring(st + startPattern.length(), end);
            ss = mSubString(sms.getBody(), st + startPattern.length(), end);
        }
        return (ss == null) ? "error" : ss;

    }

    private double getIndividualCB(Sms sms) {
        boolean isPay = isPaymentReceived(sms);
        boolean isCashOut = isCashOut(sms);
        // boolean isPaymentSentFor = isPaymentSentFor(sms);
        if (!(isPay || isCashIn(sms) || isCashOut || isPaymentSent(sms) || isPaymentSentMtn(sms))) {
            // TODO: 25-Nov-20 why are using -1
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
//        int st = sms.getBody().indexOf(firstPattern) + firstPattern.length();
//        int end = sms.getBody().indexOf(endPattern, st);
//        int md = isCashOut ? sms.getBody().indexOf("GHS", st) - 1 : sms.getBody().indexOf("GHS ", st); //portable
//
//        String ss = mSubstring(sms.getBody(), md + 4, end);
//
        Matcher m = currentBalancePattern.matcher(sms.getBody());
        if (m.find()) {
            amountStr = m.group(1);
        }
        // FIXME: 25-Nov-20 check if amountStr is null
        return Double.parseDouble(amountStr.trim());
    }

    private double getPaymentSentAmount(Sms sms) {
        if (!isPaymentSent(sms)) {
            return 0;
        }

        double sentAmount;
        String tt = isPaymentSentMadeFor(sms) ? "Payment made for" : "Payment for";

        int st;
        int end;
        String ss;
        if (sms.getBody().contains(tt)) {
            st = sms.getBody().indexOf(tt) + tt.length() + 1;
            int sm = sms.getBody().indexOf("GHS", st) + 3;
            end = sms.getBody().indexOf(" to", sm);
            //ss = sms.getBody().substring(sm, end);
            ss = mSubString(sms.getBody(), sm, end);

            sentAmount = (ss == null) ? 0 : Double.parseDouble(ss);
            // db = Double.valueOf(ss);
        } else {
            sentAmount = getPaymentSentAmountMtn(sms);
        }

        return sentAmount;
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
        if (sms.getBody().contains(tt)) {
            st = sms.getBody().indexOf(tt) + tt.length() + 1;
            int sm = sms.getBody().indexOf("GHS ", st) + 4;
            end = sms.getBody().indexOf(" to", sm);
            //ss = sms.getBody().substring(sm, end);
            ss = mSubString(sms.getBody(), sm, end);
            db = (ss == null) ? 0 : Double.parseDouble(ss);
            //db = Double.valueOf(ss);

        }
        return db;
    }


    //receive messages
    private String receivedMessage(Sms sms) {
        return isPaymentReceived(sms) ? sms.getBody() : null;
    }

    private String cashInMessage(Sms sms) {
        return isCashIn(sms) ? sms.getBody() : null;
    }


    //sent messages
    private String cashOutMessage(Sms sms) {
        return isCashOut(sms) ? sms.getBody() : null;
    }

    private String paymentSentMessage(Sms sms) {
        return isPaymentSent(sms) ? sms.getBody() : null;
    }

    private String paymentSentMessageMtn(Sms sms) {
        return isPaymentSentMtn(sms) ? sms.getBody() : null;
    }


//    /**
//     * check whether there have been a new update from the last time
//     * @param whichState the state to check can be one of this <br />
//     *                   {@link #CURRENT_BALANCE},
//     *                   {@link #TOTAL_RECEIVED},
//     *                   {@link #TOTAL_SENT}
//     * @return true if it should load from db else false
//     */
//    private boolean shouldLoad(int whichState) {
//
//        if (msgList == null) {
//            return true;
//        }
//        long lastDate = 0;
//        switch (whichState) {
//            case CURRENT_BALANCE: {
//                if (sharedPref.getLastCurrentBalAmount() == -1) {
//                    return true;
//                } else {
//                    lastDate = sharedPref.getLastCurentBalDate();
//                }
//                break;
//            }
//            case TOTAL_RECEIVED: {
//                if (sharedPref.getTotalReceived() == -1) {
//                    return true;
//                } else {
//                    lastDate = sharedPref.getLastTotalgetReceivedDate()();
//                }
//                break;
//            }
//            case TOTAL_SENT: {
//                if (sharedPref.getTotalSent() == -1) {
//                    return true;
//                } else {
//                    lastDate = sharedPref.getLastTotalSentDate();
//                }
//                break;
//            }
//        }
//        if (msgList.size() <= 0) {
//            return true;
//        }
//        //get the latest momo message
//        Sms currentMessage = (Sms) msgList.get(0);
//        Long currentMsgDate = currentMessage.getReceivedDate();
//
//        //new momo Msg is present reload curentBalance
//        boolean isload = currentMsgDate > lastDate;
//        if (isload) {
//            sharedPref.storeCurrentMessageDate(whichState, currentMsgDate);
//        }
//        return isload;
//    }

//    @Retention(RetentionPolicy.SOURCE)
//    @IntDef({ALL,PAYMENT_RECEIVED,CASH_IN})
//    @interface ReceiveMOMOType{}
//     private static final int ALL=1,
//        PAYMENT_RECEIVED=2,
//        CASH_IN=3,
//        INTEREST=4 ;
//

    /**
     * get the transaction id from the momo sms
     *
     * @param sms {@link Sms} the sms you want to get txid from
     * @return {@link String} if contains txid or {@code null} if none found
     */
    private String getTxID(Sms sms) {
        String startStr;
        if (isCashIn(sms)) {
            return null;
        } else if (isPaymentSentMtn(sms) || isCashOut(sms)) {
            startStr = "Financial Transaction Id: ";
            int st = sms.getBody().indexOf(startStr);
            int end = sms.getBody().indexOf(".", st);
            // return sms.getBody().substring(st + startStr.length(), end);
            return mSubString(sms.getBody(), st + startStr.length(), end);
        }
        startStr = "Transaction ID: ";
        int st = sms.getBody().indexOf(startStr);
        int end = sms.getBody().indexOf(".", st);
        //return sms.getBody().substring(st + startStr.length(), end);
        return mSubString(sms.getBody(), st + startStr.length(), end);

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
        if (smsList == null || smsList.size() <= 0) {
            return 0;
        }

        int msgSize = smsList.size();
        Sms sms;
            for (int i = 0; i < msgSize; i++) {
                sms = (Sms) smsList.get(i);
                if (isMobileMoneyMsg(sms)) {
                    currentBal = getIndividualCB(sms);
                    if (currentBal >= 0.0) {
                        // sharedPref.StoreCurrentBalance(currentBal, sms.getReceivedDate());
                        break;
                    }
                }
            }

        return currentBal;
    }

    public List<Sms> getMomoMsgList() {
        return smsList;
    }

    /** RECEIVED PATTERN
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
    private String mSubString(String str, int start, int end) {
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
