package com.bankir.mgs.infobip.model;

public class To {
    String phoneNumber;
    String emailAddress;

    public To(String phoneNumber, String emailAddress){
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public To(String phoneNumber){
        this.phoneNumber = phoneNumber;
        this.emailAddress = null;
    }
}
