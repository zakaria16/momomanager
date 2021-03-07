package com.mazitekgh.momomanager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mazitekgh.momomanager.model.Sms;

import java.util.ArrayList;
import java.util.List;


/**
 * MtnMomo
 * Created by Zakaria on 11-Dec-18 at 11:44 PM.
 */
public class SmsContent {
    private List<Sms> smsList = null;

    /**
     * Use to extract sms from the phone given the addresses of the sms to extract
     *
     * @param context   the context
     * @param addresses address to extract
     */
    public SmsContent(Context context, String[] addresses) {
        init(context, addresses);
    }

    public SmsContent(Context context, String address) {
        init(context, new String[]{address});
    }

    /**
     * extract MTN Mobile money sms
     *
     * @param context context
     *                for backward Compactibility
     */
    public SmsContent(Context context) {
        init(context, new String[]{"MobileMoney"});
    }


    public List<Sms> getSmsList() {
        return smsList;
    }

    void init(Context context, String[] addresses) {
        Uri inboxUri = Uri.parse("content://sms/inbox");
        smsList = new ArrayList<>();
        String number, body;
        long date;

        ContentResolver cr = context.getContentResolver();
        String[] projections = new String[]{"address", "body", "date"};

        Cursor cursor = cr.query(inboxUri, projections, "address=?", addresses, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Sms sms = new Sms();
                //todo assign direct
                number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                sms.setAddress(number);
                sms.setBody(body);
                sms.setReceivedDate(date);
                smsList.add(sms);
            }
            cursor.close();
        }
    }
}
