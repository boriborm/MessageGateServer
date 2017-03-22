package com.bankir.mgs.jersey.model;


import com.bankir.mgs.Config;

import java.util.ArrayList;
import java.util.List;

public class MessageCreationRequestObject {
    private List<Message> messages;
    private String description;
//    private String scenarioKey;


    private String channels;
    private String messageType;
    /*
    private boolean toSms;
    private boolean toViber;
    private boolean toVoice;
    private boolean toParseco;
*/
    public List<Message> getMessages() {
        return messages;
    }

    public String getDescription() {
        return description;
    }

    public MessageCreationRequestObject() {
        this.messageType = Config.getSettings().getDefaultMessageType();
        this.channels = "S";
        this.messages = new ArrayList<>();
    }

    public MessageCreationRequestObject(String channels, String messageType) {
        this.channels = channels;
        this.messageType = messageType;
    }

    public String getChannels() {
        return channels;
    }


    public String getMessageType() { return messageType; }
/*
    public String getScenarioKey() {
        return scenarioKey;
    }
*/
    public static class Message {
        private String text;
        private String smsText;
        private String viberText;
        private String voiceText;
        private String parsecoText;
        private String facebookText;
        private String phoneNumber;
        private String messageId;
        private String channels;


        public Message(String messageId, String phoneNumber,  String text) {
            this.messageId = messageId;
            this.text = text;
            this.phoneNumber = phoneNumber;
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

        public String getFacebookText() {
            return facebookText;
        }

    public String getChannels() {
        return channels;
    }
}
}
