package com.bankir.mgs.hibernate.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="PHONEGRANTS")
public class PhoneGrant implements Serializable {

    @Id
    @Column
    private String phoneNumber;

    @Type(type="yes_no")
    @Column
    private boolean acceptSms;

    @Type(type="yes_no")
    @Column
    private boolean acceptViber;

    @Type(type="yes_no")
    @Column
    private boolean acceptVoice;

    @Type(type="yes_no")
    @Column
    private boolean acceptParseco;

    @Type(type="yes_no")
    @Column
    private boolean acceptFacebook;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public PhoneGrant(){
        this.acceptSms = true;
        this.acceptViber = false;
        this.acceptParseco = false;
        this.acceptVoice = false;
        this.acceptFacebook = false;
    };

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isAcceptSms() {
        return acceptSms;
    }

    public void setAcceptSms(boolean acceptSms) {
        this.acceptSms = acceptSms;
    }

    public boolean isAcceptViber() {
        return acceptViber;
    }

    public void setAcceptViber(boolean acceptViber) {
        this.acceptViber = acceptViber;
    }

    public boolean isAcceptVoice() {
        return acceptVoice;
    }

    public void setAcceptVoice(boolean acceptVoice) {
        this.acceptVoice = acceptVoice;
    }

    public boolean isAcceptParseco() {
        return acceptParseco;
    }

    public void setAcceptParseco(boolean acceptParseco) {
        this.acceptParseco = acceptParseco;
    }

    public boolean isAcceptFacebook() {
        return acceptFacebook;
    }

    public void setAcceptFacebook(boolean acceptFacebook) {
        this.acceptFacebook = acceptFacebook;
    }
}
