package com.bankir.mgs.infobip.model;

public class Voice {
    String text;
    Integer validityPeriod;

    public Voice(String text, Integer validityPeriod){
        this.text = text;
        this.validityPeriod = validityPeriod;
    }
}
