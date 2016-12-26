package com.bankir.mgs.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="USERMESSAGETYPES")
public class UserMessageType implements Serializable {

    @Id
    @Column
    private String typeId;

    @Id
    @Column
    private Long userId;

    public UserMessageType() {}

    public UserMessageType(String typeId, Long userId) {
        this.typeId = typeId;
        this.userId = userId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
