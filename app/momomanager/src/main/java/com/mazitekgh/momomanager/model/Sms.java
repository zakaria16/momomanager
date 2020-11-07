package com.mazitekgh.momomanager.model;

/**
 * MtnMomo
 * Created by Zakaria on 12-Dec-18 at 12:57 AM.
 */
public class Sms {
    //todo access to private
    //did this to for compatibility with old everythingme sms lib
    private String address;
    private String body;
    private Long receivedDate;

    public Sms() {

    }

    public Sms(String address, String body, Long receivedDate) {
        this.address = address;
        this.body = body;
        this.receivedDate = receivedDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Long receivedDate) {
        this.receivedDate = receivedDate;
    }

}
