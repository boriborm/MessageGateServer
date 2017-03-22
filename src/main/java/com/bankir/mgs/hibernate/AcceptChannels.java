package com.bankir.mgs.hibernate;

public interface AcceptChannels {
    boolean isAcceptSms();
    boolean isAcceptViber();
    boolean isAcceptVoice();
    boolean isAcceptParseco();
    boolean isAcceptFacebook();
    void setAcceptSms(boolean accept);
    void setAcceptViber(boolean accept);
    void setAcceptVoice(boolean accept);
    void setAcceptParseco(boolean accept);
    void setAcceptFacebook(boolean accept);
}
