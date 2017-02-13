package com.bankir.mgs.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="IMSI")
public class Imsi implements Serializable {

    @Id
    @Column
    private String phoneNumber;

    @Column (name="imsi")
    private String imsi;


    public Imsi(){};

    public Imsi(String phoneNumber, String imsi) {
        this.phoneNumber = phoneNumber;
        this.imsi = imsi;
    }

    public String getImsi() {
        return imsi;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

}
