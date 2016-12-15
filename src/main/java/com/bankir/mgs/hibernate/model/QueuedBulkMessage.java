package com.bankir.mgs.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="QUEUEDBULKMESSAGES")
public class QueuedBulkMessage implements Serializable {
    @Id
    @Column(name="messageId")
    private Long messageId;

    @Id
    @Column(name="bulkId")
    private Long bulkId;


    public QueuedBulkMessage(){};

    public QueuedBulkMessage(Long messageId, Long bulkId) {
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
