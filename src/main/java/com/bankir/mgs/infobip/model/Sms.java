package com.bankir.mgs.infobip.model;

public class Sms {
    String text;
    Integer validityPeriod;

    public Sms(String text, Integer validityPeriod){
        this.text = text;
        //this.validityPeriod = validityPeriod;
    }
}
