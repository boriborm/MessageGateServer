package com.bankir.mgs.infobip.model;


public class Message {

    To to;
    String messageId;
    Status status;
    int messageCount;

    public To getTo() {
        return to;
    }

    public String getMessageId() {
        return messageId;
    }

    public Status getStatus() {
        return status;
    }

    public int getMessageCount() {
        return messageCount;
    }
}
