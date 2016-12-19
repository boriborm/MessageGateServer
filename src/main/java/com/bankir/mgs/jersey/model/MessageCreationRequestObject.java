package com.bankir.mgs.jersey.model;


import com.bankir.mgs.Config;

import java.util.ArrayList;
import java.util.List;

public class MessageCreationRequestObject {
    private List<Message> messages;
    private String description;
    public List<Message> getMessages() {
        return messages;
    }

    public String getDescription() {
        return description;
    }

    public MessageCreationRequestObject() {
        messages = new ArrayList<>();
    }

    public static class Message {
        private String text;
        private String smsText;
        private String viberText;
        private String voiceText;
        private String parsecoText;
        private String messageType;
        private String scenarioKey;
        private Long scenarioId;
        private String phoneNumber;
        private String messageId;

        //private List<Receiver> receivers;

        private boolean toSms;
        private boolean toViber;
        private boolean toVoice;
        private boolean toParseco;

        /* Инициализация в конструкторе, т.к. GSON дефолтные значения обNULLяет */
        public Message() {
            this.messageType = Config.DEFAULT_MESSAGE_TYPE;
            this.scenarioKey = Config.getSettings().getDefaultScenarioKey();
            this.toSms = false;
            this.toViber = false;
            this.toVoice = false;
            this.toParseco = false;
        }

/*
        public Message(String text, String smsText, String viberText, String voiceText, String parsecoText, String messageType, String scenarioKey, String phone, String messageId) {
            this.text = text;
            this.smsText = smsText;
            this.viberText = viberText;
            this.voiceText = voiceText;
            this.parsecoText = parsecoText;
            this.messageType = messageType;
            this.scenarioKey = scenarioKey;
            this.toSms = (smsText!=null);
            this.toViber = (viberText!=null);
            this.toVoice = (voiceText!=null);
            this.toParseco = (parsecoText!=null);
        }
*/
        public Message(String messageId, String scenarioKey,String messageType,  String phoneNumber, String text,  boolean toSms, boolean toViber, boolean toVoice, boolean toParseco) {
            this.messageId = messageId;
            this.text = text;
            this.messageType = messageType;
            this.scenarioKey = scenarioKey;
            this.phoneNumber = phoneNumber;
            this.toSms = toSms;
            this.toViber = toViber;
            this.toVoice = toVoice;
            this.toParseco = toParseco;
        }

        public Long getScenarioId() {
            return scenarioId;
        }

        public void setScenarioId(Long scenarioId) {
            this.scenarioId = scenarioId;
        }

        public String getText() {
            return text;
        }

        public String getSmsText() {
            return (smsText==null?text:smsText);
        }

        public String getViberText() {
            return (viberText==null?text:viberText);
        }

        public String getVoiceText() {
            return (viberText==null?text:voiceText);
        }

        public String getParsecoText() {
            return (parsecoText==null?text:parsecoText);
        }

        public String getMessageType() { return messageType; }


        public String getScenarioKey() {
            return scenarioKey;
        }

        public boolean toSms() {
            return toSms;
        }

        public boolean toViber() {
            return toViber;
        }

        public boolean toVoice() {
            return toVoice;
        }

        public boolean toParseco() {
            return toParseco;
        }

        public void setScenarioKey(String scenarioKey) {
            this.scenarioKey = scenarioKey;
        }

        public void setToSms(boolean toSms) {
            this.toSms = toSms;
        }

        public void setToViber(boolean toViber) {
            this.toViber = toViber;
        }

        public void setToVoice(boolean toVoice) {
            this.toVoice = toVoice;
        }

        public void setToParseco(boolean toParseco) {
            this.toParseco = toParseco;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

    }
}