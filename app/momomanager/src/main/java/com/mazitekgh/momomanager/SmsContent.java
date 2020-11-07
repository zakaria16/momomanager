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
    private final List<Sms> smsList;
    public SmsContent(Context c) {
        Uri inboxUri = Uri.parse("content://sms/inbox");
        smsList = new ArrayList<>();
        String number, body;
        long date;

        ContentResolver cr = c.getContentResolver();
        String[] projections = new String[]{"address", "body", "date"};

        Cursor cursor = cr.query(inboxUri, projections, "address=?", new String[]{"MobileMoney"}, null);

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


    public List<Sms> getSmsList() {
        return smsList;
    }
}
