package com.bankir.mgs.infobip.model;


public class Message {

    InfobipObjects.To to;
    String messageId;
    InfobipObjects.Status status;
    int messageCount;

    public String getMessageId() {
        return messageId;
    }

    public InfobipObjects.Status getStatus() {
        return status;
    }

}
