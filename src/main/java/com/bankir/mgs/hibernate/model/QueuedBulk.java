package com.bankir.mgs.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="QUEUEDBULKS")
public class QueuedBulk implements Serializable {

    @Id
    @Column(name="id")
    private Long id;

    public QueuedBulk() {}

    public QueuedBulk(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
