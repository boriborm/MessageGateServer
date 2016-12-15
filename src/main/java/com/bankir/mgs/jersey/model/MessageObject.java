package com.bankir.mgs.jersey.model;


import java.util.Date;

public class MessageObject {
    private Long id;
    private String phoneNumber;
    private Date createDate;
    private String externalId;
    private Scenario scenario;
    private User user;
    private MessageType messageType;
    private String smsText;
    private String viberText;
    private String voiceText;
    private String parsecoText;

    public static class User{
        Long id;
        String userName;

        public User(Long id, String userName) {
            this.id = id;
            this.userName = userName;
        }
    }

    public static class Scenario{
        Long id;
        String scenarioName;
        String scenarioKey;

        public Scenario(Long id, String scenarioName, String scenarioKey) {
            this.id = id;
            this.scenarioName = scenarioName;
            this.scenarioKey = scenarioKey;
        }
    }

    public static class MessageType{
        String typeId;
        String description;
        boolean acceptSms;
        boolean acceptViber;
        boolean acceptVoice;
        boolean acceptParseco;

        public MessageType(String typeId, String description, boolean acceptSms, boolean acceptViber, boolean acceptVoice, boolean acceptParseco) {
            this.typeId = typeId;
            this.description = description;
            this.acceptSms = acceptSms;
            this.acceptViber = acceptViber;
            this.acceptVoice = acceptVoice;
            this.acceptParseco = acceptParseco;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }

    public String getViberText() {
        return viberText;
    }

    public void setViberText(String viberText) {
        this.viberText = viberText;
    }

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public String getParsecoText() {
        return parsecoText;
    }

    public void setParsecoText(String parsecoText) {
        this.parsecoText = parsecoText;
    }
}
