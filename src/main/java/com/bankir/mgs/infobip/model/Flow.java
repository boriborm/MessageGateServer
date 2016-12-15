package com.bankir.mgs.infobip.model;

public class Flow {
    String from;
    String channel;

    public Flow(String from, String channel) {
        this.from = from;
        this.channel = channel;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

}
