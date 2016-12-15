package com.bankir.mgs.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="BULKMESSAGES")
public class BulkMessage implements Serializable {
    @Id
    @Column(name="messageId")
    private Long messageId;

    @Column(name="bulkId")
    private Long bulkId;


    public BulkMessage(){};

    public BulkMessage(Long messageId, Long bulkId) {
        this.messageId = messageId;
        this.bulkId = bulkId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getBulkId() {
        return bulkId;
    }

    public void setBulkId(Long bulkId) {
        this.bulkId = bulkId;
    }
}
