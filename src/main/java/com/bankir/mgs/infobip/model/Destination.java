package com.bankir.mgs.infobip.model;

public class Destination {
    To to;
    String messageId;

    public Destination(String messageId, To to){
        this.messageId = messageId;
        this.to = to;
    }

    public Destination (String messageId, String phoneNumber){
        this.messageId = messageId;
        this.to = new To(phoneNumber);
    }

    public Destination (String messageId, String phoneNumber, String emailAddress){
        this.messageId = messageId;
        this.to = new To(phoneNumber, emailAddress);
    }
}
