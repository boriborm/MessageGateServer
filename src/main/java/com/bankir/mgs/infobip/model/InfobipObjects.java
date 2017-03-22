package com.bankir.mgs.infobip.model;

import com.bankir.mgs.hibernate.model.MessageType;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class InfobipObjects {

    public static class RequestError {

        ServiceException serviceException;
        String error;
        String errorType = "UNKNOWN_ERROR";

        public RequestError(String error){
            this.error = error;
        }

        public RequestError(String error, String errorType) {
            this.error = error;
            this.errorType = errorType;
        }

        public ServiceException getServiceException() {
            return serviceException;
        }

        public String getError() {
            return (error!=null?error:(serviceException!=null?serviceException.getText():null));
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getErrorType() {
            return errorType;
        }
    }

    public static class Flow {
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

    public static class Scenario {
        private String key;
        private String name;
        private List<InfobipObjects.Flow> flow;
        private InfobipObjects.RequestError requestError;

        @SerializedName("default")
        private Boolean defaultScenario = false;

        public Scenario (String key, String name, boolean defaultScenario){
            this.key = key;
            this.name = name;
            this.defaultScenario = defaultScenario;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public InfobipObjects.RequestError getRequestError() {
            return requestError;
        }

        public String getServiceErrorMessage(){
            String ret = null;
            if (requestError!=null&&requestError.getServiceException()!=null){
                ret = requestError.getServiceException().getText();
            }
            return ret;
        }

        public List<InfobipObjects.Flow> getFlow() {
            return flow;
        }

        public Boolean isDefaultScenario() {
            return defaultScenario;
        }

        public void setFlow(List<InfobipObjects.Flow> flow) {
            this.flow = flow;
        }
    }

    public static class Parseco {
        String text;
        Integer validityPeriod;

        public Parseco(String text, Integer validityPeriod){
            this.text = text;
            this.validityPeriod = validityPeriod;
        }
    }

    public static class Sms {
        String text;
        Integer validityPeriod;

        public Sms(String text, Integer validityPeriod){
            this.text = text;
            //this.validityPeriod = validityPeriod;
        }
    }

    public static class Viber {
        String text;
        Integer validityPeriod;
        String imageURL;
        String buttonText;
        String buttonURL;
        boolean isPromotional;

        public Viber(String text, Integer validityPeriod){
            this.text = text;
            this.validityPeriod = validityPeriod;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        public void setButtonText(String buttonText) {
            this.buttonText = buttonText;
        }

        public void setButtonURL(String buttonURL) {
            this.buttonURL = buttonURL;
        }

        public void setPromotional(boolean promotional) {
            isPromotional = promotional;
        }
    }

    public static class Voice {
        String text;
        Integer validityPeriod;

        public Voice(String text, Integer validityPeriod){
            this.text = text;
            this.validityPeriod = validityPeriod;
        }
    }

    public static class Facebook {
        String text;
        Integer validityPeriod;

        public Facebook(String text, Integer validityPeriod){
            this.text = text;
            //this.validityPeriod = validityPeriod;
        }
    }

    public static class To {
        String phoneNumber;
        String emailAddress;

        public To(String phoneNumber){
            this.phoneNumber = phoneNumber;
            this.emailAddress = null;
        }
    }

    public static class Destination {
        InfobipObjects.To to;
        String messageId;

        public Destination(String messageId, InfobipObjects.To to){
            this.messageId = messageId;
            this.to = to;
        }

        public Destination (String messageId, String phoneNumber){
            this.messageId = messageId;
            this.to = new InfobipObjects.To(phoneNumber);
        }

    }

    public static class OmniAdvancedMessage {

        private String bulkId;
        private String scenarioKey;

        private List<InfobipObjects.Destination> destinations ;

        private InfobipObjects.Sms sms;
        private InfobipObjects.Viber viber;
        private InfobipObjects.Parseco parseco;
        private InfobipObjects.Voice voice;
        private InfobipObjects.Facebook facebook;
        private Date sendAt;

        public OmniAdvancedMessage(String scenarioKey, String bulkId, com.bankir.mgs.hibernate.model.Message msg, MessageType msgType, List<InfobipObjects.Destination> destinations){
            this.scenarioKey = scenarioKey;
            this.bulkId = bulkId;
            this.sendAt = msg.getSendAt();
            this.destinations = destinations;

            this.sms = (msg.getSmsText()!=null?new InfobipObjects.Sms(msg.getSmsText(), msgType.getSmsValidityPeriod()):null);
            this.viber = (msg.getViberText()!=null?new InfobipObjects.Viber(msg.getViberText(), msgType.getViberValidityPeriod()):null);
            this.parseco = (msg.getViberText()!=null?new InfobipObjects.Parseco(msg.getParsecoText(), msgType.getParsecoValidityPeriod()):null);
            this.voice = (msg.getViberText()!=null?new InfobipObjects.Voice(msg.getVoiceText(), msgType.getVoiceValidityPeriod()):null);
            this.facebook = (msg.getViberText()!=null?new InfobipObjects.Facebook(msg.getFacebookText(), msgType.getFacebookValidityPeriod()):null);

        }

    }

    public class Price {
        BigDecimal pricePerMessage;
        String currency;

        public BigDecimal getPricePerMessage() {
            return pricePerMessage;
        }

        public String getCurrency() {
            return currency;
        }
    }

    public class Status {

        private int id;
        private int groupId;
        private String name;
        private String description;
        private String groupName;

        public int getId() {
            return id;
        }

        public int getGroupId() {
            return groupId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getGroupName() {
            return groupName;
        }
    }

    public class Result {
        String bulkId;
        String messageId;
        String to;
        Date sentAt;
        Date doneAt;
        int messageCount;
        String channel;
        String mccMnc;

        InfobipObjects.Error error;
        InfobipObjects.Status status;
        InfobipObjects.Price price;

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

        public InfobipObjects.Error getError() {
            return error;
        }

        public InfobipObjects.Status getStatus() {
            return status;
        }

        public InfobipObjects.Price getPrice() {
            return price;
        }

        public String getMccMnc() {
            return mccMnc;
        }
    }

    public class Error {
        int groupId;
        String groupName;
        int id;
        String name;
        String description;
        boolean permanent;
    }
}
