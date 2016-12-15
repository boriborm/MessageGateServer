package com.bankir.mgs.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="QUEUEDMESSAGES")
public class QueuedMessage implements Serializable {
    @Id
    @Column(name="messageId")
    private Long messageId;

    public QueuedMessage(){};

    public QueuedMessage(Long messageId){
        this.messageId = messageId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
