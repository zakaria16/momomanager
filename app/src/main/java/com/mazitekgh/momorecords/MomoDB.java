package com.mazitekgh.momorecords;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mazitekgh.momomanager.model.Momo;

import java.util.ArrayList;
import java.util.List;

/**
 * MomoDB.java
 * Created by Zakaria on 18-jun-19 at 11:27 PM.
 */

public class MomoDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "momo_db";
    private static final int VERSION = 2;
    private static final String TABLE_NAME = "to_server";
    private static final String COL_ID = "id";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_TXID = "txid";
    private static final String COL_CURRENT_BALANCE = "currentBalance";
    private static final String COL_MOMO_TYPE = "type";
    private static final String COL_CONTENT = "content";
    private static final String COL_DATE = "date";
    private static final String COL_REFERENCE = "reference";
    private static final String COL_SENDER = "sender";

    private static final String COL_SEND_DATE = "serverDate";
    private static final String COL_server_Sucess = "serverSuccess";


    public MomoDB(@Nullable Context context) {
        super(context, DB_NAME, null, VERSION);

    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        //todo check sql statement
        createDB(db);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        createDB(db);
    }

    private void createDB(SQLiteDatabase db) {
        //@todo change amount to double
        db.execSQL("CREATE TABLE " + TABLE_NAME + "( "
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_SENDER + " TEXT,"
                + COL_AMOUNT + " TEXT,"
                + COL_REFERENCE + " TEXT,"
                + COL_TXID + " TEXT,"
                + COL_CURRENT_BALANCE + " TEXT,"
                + COL_MOMO_TYPE + "   BOOLEAN,"
                + COL_CONTENT + "     TEXT,"
                + COL_DATE + "       TEXT,"
                + COL_SEND_DATE + "TEXT,"
                + COL_server_Sucess + "BOOLEAN"
                + ")"
        );
    }

    public boolean saveNewsItem(@NonNull Momo momoToSave) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SENDER, momoToSave.getSender());
        cv.put(COL_AMOUNT, momoToSave.getAmount());
        cv.put(COL_REFERENCE, momoToSave.getReference());
        cv.put(COL_TXID, momoToSave.getTxID());
        cv.put(COL_CURRENT_BALANCE, Double.valueOf(momoToSave.getCurrentBalance()));
        cv.put(COL_MOMO_TYPE, momoToSave.getType());
        cv.put(COL_CONTENT, momoToSave.getContentStr());
        cv.put(COL_DATE, momoToSave.getDateStr());

        long res = this.getWritableDatabase().insert(TABLE_NAME, null, cv);
        return res != -1;
    }

    @NonNull
    public List<Momo> LoadSavedNews() {
        List<Momo> momoList = new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            Momo momo = new Momo();
            momo.setAmount(Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_AMOUNT)).trim()));
            momo.setCurrentBalance(cursor.getString(cursor.getColumnIndexOrThrow(COL_CURRENT_BALANCE)));
            momo.setTxID(cursor.getString(cursor.getColumnIndexOrThrow(COL_TXID)));
            momo.setDateStr(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
            momo.setSender(cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER)));
            momo.setReference(cursor.getString(cursor.getColumnIndexOrThrow(COL_REFERENCE)));
            momo.setType(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MOMO_TYPE)));
            momo.setContentStr((cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT))));
            momoList.add(momo);
        }
        cursor.close();
        return momoList;
    }

    public int updateServerInfo(int ID, boolean status, String sendDate) {
        ContentValues cv = new ContentValues();
        cv.put(COL_server_Sucess, status);
        cv.put(COL_SEND_DATE, sendDate);
        //todo remove redundant number
        int number = this.getWritableDatabase().update(TABLE_NAME, cv, "" + COL_ID + "=" + ID, null);
        return number;
    }

    public void removeNews(String link) {
        //    this.getWritableDatabase().execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COL_TXID + "=?", new String[]{link});
    }
}
