package com.bankir.mgs.jersey.model;


import java.util.List;

public class CreateMessagesResponseObject {
    private List<CreateMessageResponseObject> messages;
    private String bulkDescription;
    private Long bilkId;

    public CreateMessagesResponseObject(List<CreateMessageResponseObject> messages, Long bilkId, String bulkDescription) {
        this.messages = messages;
        this.bulkDescription = bulkDescription;
        this.bilkId = bilkId;
    }

    public CreateMessagesResponseObject(List<CreateMessageResponseObject> messages) {
        this(messages,null,null);
    }
}
