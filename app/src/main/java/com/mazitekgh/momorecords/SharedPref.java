package com.mazitekgh.momorecords;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.mazitekgh.momomanager.ExtractMtnMomoInfo;

import java.util.HashMap;
import java.util.List;


/**
 * MtnMomo
 * Created by Zakaria on 11-Oct-18 at 12:39 PM.
 */
public class SharedPref {

    private static final String MOMO_KEY = "momo_key";
    private static final String TOTAL_RECEIVED_KEY = "total_received_key";
    private static final String TOTAL_SENT_KEY = "total_sent_key";
    private static final String CB_AMOUNT_KEY = "cb_amount";
    private static final String CB_DATE_KEY = "cb_date_key";
    private static final String BALANCE_KEY = "balance_key";
    private static final String SHARED_KEY = "com.mazitekgh.momorecords.total_received_amount";
    private static final String TOTAL_RECEIVED_DATE_KEY = "received-date";
    private static final String TOTAL_SENT_DATE_KEY = "sent-date";
    private static final String RATING_KEY = "rating-key";
    private static final String COOKIE_KEY = "cookies-key";

    private final Context c;
    private final SharedPreferences sp;

    public SharedPref(Context context) {
        this.c = context;
        sp = c.getSharedPreferences(SHARED_KEY, Context.MODE_PRIVATE);
    }

    public void storeTotalAmount(String totalReceivedAmount, String totalSentAmount, String balance) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(TOTAL_RECEIVED_KEY, totalReceivedAmount);
        editor.putString(TOTAL_SENT_KEY, totalSentAmount);
        editor.putString(CB_AMOUNT_KEY, balance);
        editor.apply();

    }

    public HashMap<String, String> getTotalValues() {
        HashMap<String, String> hs = new HashMap<>(3);
        hs.put(TOTAL_RECEIVED_KEY, sp.getString(TOTAL_RECEIVED_KEY, "0"));
        hs.put(TOTAL_SENT_KEY, sp.getString(BALANCE_KEY, "0"));
        hs.put(CB_AMOUNT_KEY, sp.getString(BALANCE_KEY, "0"));

        return hs;
    }

    public void storeMomoMessages(List list) {
        Gson gs = new Gson();
        String gsonForm = gs.toJson(list);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(MOMO_KEY, gsonForm);
        editor.apply();
    }

    public List getStoreMomoMessages() {
        Gson gs = new Gson();
        String gsonString = sp.getString(MOMO_KEY, null);
        if (gsonString == null) {
            return null;
        }
        return gs.fromJson(gsonString, List.class);
    }

    public void StoreCurrentBalance(Double amount, long date) {

        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(CB_AMOUNT_KEY, amount.floatValue());
        editor.putLong(CB_DATE_KEY, date);
        editor.apply();
    }

    private void storeCurrentBalanceDate(long date) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(CB_DATE_KEY, date);
        editor.apply();
    }

    private void storeLastReceivedDate(long date) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(TOTAL_RECEIVED_DATE_KEY, date);
        editor.apply();
    }

    private void storeLastSentDate(long date) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(TOTAL_SENT_DATE_KEY, date);
        editor.apply();
    }

    void storeCurrentMessageDate(@ExtractMtnMomoInfo.TotalAmountType int whichType, Long lastDate) {
        switch (whichType) {
            case ExtractMtnMomoInfo.CURRENT_BALANCE: {
                storeCurrentBalanceDate(lastDate);
                break;
            }
            case ExtractMtnMomoInfo.TOTAL_RECEIVED: {
                storeLastReceivedDate(lastDate);
                break;
            }
            case ExtractMtnMomoInfo.TOTAL_SENT: {
                storeLastSentDate(lastDate);
                break;
            }
        }
    }
    public long getLastCurentBalDate() {
        return sp.getLong(CB_DATE_KEY, 0);
    }

    public long getLastTotalReceivedDate() {
        return sp.getLong(TOTAL_RECEIVED_DATE_KEY, 0);
    }

    public long getLastTotalSentDate() {
        return sp.getLong(TOTAL_SENT_DATE_KEY, 0);
    }



    public double getLastCurrentBalAmount() {
        return sp.getFloat(CB_AMOUNT_KEY, -1);
    }

    public double getTotalReceived() {
        return sp.getFloat(TOTAL_RECEIVED_KEY, -1);
    }

    public double getTotalSent() {
        return sp.getFloat(TOTAL_SENT_KEY, -1);
    }

    public void storeTotalSentAmount(Double amount) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(TOTAL_SENT_KEY, amount.floatValue());
        editor.apply();
    }

    public void storeTotalReceivedAmount(Double amount) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(TOTAL_RECEIVED_KEY, amount.floatValue());
        editor.apply();
    }

    public boolean showRatingDialog() {
        return sp.getBoolean(RATING_KEY, true);
    }

    public void disableRatingDialog() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(RATING_KEY, false);
        editor.apply();
    }


    public void saveCookie(String cookie) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(COOKIE_KEY, cookie);
        editor.apply();
    }

    public String getCookie() {
        return sp.getString(COOKIE_KEY, null);
    }


}

