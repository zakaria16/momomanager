package com.mazitekgh.momomanager.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.mazitekgh.momomanager.ExtractMtnMomoInfo;

/**
 * MtnMomo
 * Created by Zakaria on 03-Sep-18 at 7:02 PM.
 */
public class Momo {

//    public static final Creator<Momo> CREATOR = new Creator<Momo>() {
//        @Override
//        public Momo createFromParcel(Parcel in) {
//            return new Momo(in);
//        }
//
//        @Override
//        public Momo[] newArray(int size) {
//            return new Momo[size];
//        }
//    };


    private String date;
    private String content;
    private String sender;
    private String txID;
    private String currentBalance;
    private double amount;
    private int type;
    private String reference;
    //private boolean serverStatus;
    //private String serverSentDate;

    public Momo() {
    }


    public Momo(int type, String date, String content, String senderReceiver, String txID, String currentBalance, double amount, String reference) {
        this.type = type;
        this.date = date;
        this.content = content;
        this.sender = senderReceiver;
        this.txID = txID;
        this.currentBalance = currentBalance;
        this.amount = amount;
        this.reference = reference;

    }

//    private Momo(Parcel in) {
//        dateStr = in.readString();
//        contentStr = in.readString();
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }

//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(dateStr);
//        dest.writeString(contentStr);
//    }


    public int getType() {
        return type;
    }

    public void setType(@ExtractMtnMomoInfo.MomoType int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTxID() {
        return txID;
    }

    public void setTxID(String txID) {
        this.txID = txID;
    }

    public String getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(String currentBalance) {
        this.currentBalance = currentBalance;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }


//    public boolean isServerStatus() {
//        return serverStatus;
//    }

//    public void setServerStatus(boolean serverStatus) {
//        this.serverStatus = serverStatus;
//    }

//    public String getServerSentDate() {
//        return serverSentDate;
//    }
//
//    public void setServerSentDate(String serverSentDate) {
//        this.serverSentDate = serverSentDate;
//    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Momo fromJson(String jsonString) {

        return new Gson().fromJson(jsonString, Momo.class);
    }

    @NonNull
    @Override
    public String toString() {
        //in case it is null
        return "" + toJson();
    }
}