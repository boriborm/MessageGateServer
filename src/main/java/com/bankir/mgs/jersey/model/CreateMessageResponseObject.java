package com.bankir.mgs.jersey.model;


public class CreateMessageResponseObject {
    private String messageId;
    private boolean success;
    private String message;
    private String externalId;

    public CreateMessageResponseObject(String messageId, boolean success, String message, String externalId) {
        this.messageId = messageId;
        this.success = success;
        this.message = message;
        this.externalId = externalId;
    }

    public String getMessageId() {
        return messageId;
    }
    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    public String getExternalId() {
        return externalId;
    }
}
