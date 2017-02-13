package com.bankir.mgs.jersey.model;

public class MessageTypeObject {

    private String typeId;
    private String description;
    private boolean acceptSms;
    private boolean acceptViber;
    private boolean acceptVoice;
    private boolean acceptParseco;
    private boolean acceptFacebook;
    private Integer smsValidityPeriod;
    private Integer viberValidityPeriod;
    private Integer parsecoValidityPeriod;
    private Integer voiceValidityPeriod;
    private Integer facebookValidityPeriod;
    private boolean active;
    private boolean verifyImsi;

    public MessageTypeObject(String typeId, String description, boolean acceptSms, boolean acceptViber, boolean acceptVoice, boolean acceptParseco, boolean acceptFacebook, Integer smsValidityPeriod, Integer viberValidityPeriod, Integer parsecoValidityPeriod, Integer voiceValidityPeriod, Integer facebookValidityPeriod, boolean active, boolean verifyImsi) {
        this.typeId = typeId;
        this.description = description;
        this.acceptSms = acceptSms;
        this.acceptViber = acceptViber;
        this.acceptVoice = acceptVoice;
        this.acceptParseco = acceptParseco;
        this.acceptFacebook = acceptFacebook;
        this.smsValidityPeriod = smsValidityPeriod;
        this.viberValidityPeriod = viberValidityPeriod;
        this.parsecoValidityPeriod = parsecoValidityPeriod;
        this.voiceValidityPeriod = voiceValidityPeriod;
        this.facebookValidityPeriod = facebookValidityPeriod;
        this.active = active;
        this.verifyImsi = verifyImsi;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAcceptSms() {
        return acceptSms;
    }

    public boolean isAcceptViber() {
        return acceptViber;
    }

    public boolean isAcceptVoice() {
        return acceptVoice;
    }

    public boolean isAcceptParseco() {
        return acceptParseco;
    }

    public Integer getSmsValidityPeriod() {
        return smsValidityPeriod;
    }

    public Integer getViberValidityPeriod() {
        return viberValidityPeriod;
    }

    public Integer getParsecoValidityPeriod() {
        return parsecoValidityPeriod;
    }

    public Integer getVoiceValidityPeriod() {
        return voiceValidityPeriod;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVerifyImsi() {
        return verifyImsi;
    }

    public boolean isAcceptFacebook() {
        return acceptFacebook;
    }

    public Integer getFacebookValidityPeriod() {
        return facebookValidityPeriod;
    }
}
