package com.bankir.mgs.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="BULKS")
public class Bulk implements Serializable {

    @Id
    @SequenceGenerator(name="SEQ_BULKID", sequenceName="SEQ_BULKID",allocationSize=1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="SEQ_BULKID")
    @Column(name="id")
    private Long id;

    @Column(name="description")
    private String description;

    public Bulk() {}

    public Bulk(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
