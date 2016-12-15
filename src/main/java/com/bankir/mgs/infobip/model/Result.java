package com.bankir.mgs.infobip.model;

import java.util.Date;

public class Result {
    String bulkId;
    String messageId;
    String to;
    Date sentAt;
    Date doneAt;
    int messageCount;
    String channel;
    String mccMnc;

    Error error;
    Status status;
    Price price;

    public String getBulkId() {
        return bulkId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getTo() {
        return to;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public Date getDoneAt() {
        return doneAt;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getChannel() {
        return channel;
    }

    public Error getError() {
        return error;
    }

    public Status getStatus() {
        return status;
    }

    public Price getPrice() {
        return price;
    }

    public String getMccMnc() {
        return mccMnc;
    }
}
