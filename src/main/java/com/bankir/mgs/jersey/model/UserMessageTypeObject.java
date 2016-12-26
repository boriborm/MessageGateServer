package com.bankir.mgs.jersey.model;

public class UserMessageTypeObject {

    private String typeId;
    private Long userId;

    public UserMessageTypeObject(String typeId, Long userId) {
        this.typeId = typeId;
        this.userId = userId;
    }

    public String getTypeId() {
        return typeId;
    }

    public Long getUserId() {
        return userId;
    }
}
