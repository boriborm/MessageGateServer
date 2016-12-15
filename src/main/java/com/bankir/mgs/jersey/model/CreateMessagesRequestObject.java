package com.bankir.mgs.jersey.model;


import java.util.List;

public class CreateMessagesRequestObject {
    private List<CreateMessageRequestObject> messages;
    private String description;
    public List<CreateMessageRequestObject> getMessages() {
        return messages;
    }

    public String getDescription() {
        return description;
    }
}
