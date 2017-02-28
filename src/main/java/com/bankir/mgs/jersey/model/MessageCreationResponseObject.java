package com.bankir.mgs.jersey.model;


import java.util.List;

public class MessageCreationResponseObject extends JsonObject{

    private String bulkDescription;
    private Long bulkId;
    private List<SuccessMessage> successMessages;
    private List<FailedMessage> failedMessages;

    public MessageCreationResponseObject(List<SuccessMessage> successMessages, List<FailedMessage> failedMessages, Long bilkId, String bulkDescription) {
        this.successMessages = successMessages;
        this.failedMessages = failedMessages;
        this.bulkDescription = bulkDescription;
        this.bulkId = bilkId;
    }

    public List<SuccessMessage> getSuccessMessages() {
        return successMessages;
    }

    public List<FailedMessage> getFailedMessages() {
        return failedMessages;
    }

    public Long getBulkId() {
        return bulkId;
    }

    public MessageCreationResponseObject(List<SuccessMessage> successMessages, List<FailedMessage> failedMessagesMessages) {
        this(successMessages, failedMessagesMessages,null,null);
    }

    public MessageCreationResponseObject(String errorMessage){
        super(errorMessage);
    }

    public static class SuccessMessage {
        private String messageId;
        //private String message;
        private String externalId;

        public SuccessMessage(String messageId, String externalId) {
            this.messageId = messageId;
            this.externalId = externalId;
        }

        public String getMessageId() {
            return messageId;
        }
        public String getExternalId() {return externalId;}
    }


    public static class FailedMessage {
        private String messageId;
        private String error;

        public FailedMessage(String messageId, String error) {
            this.messageId = messageId;
            this.error = error;
        }
        public String getMessageId() {
            return messageId;
        }
        public String getError() {
            return error;
        }
    }
}
