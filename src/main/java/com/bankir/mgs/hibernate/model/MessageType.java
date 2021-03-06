package com.bankir.mgs.hibernate.model;

import com.bankir.mgs.hibernate.AcceptChannels;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="MESSAGETYPES")
public class MessageType  implements Serializable, AcceptChannels {

    @Id
    @Column
    private String typeId;

    @Column
    String description;

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

    @Type(type="yes_no")
    @Column
    private boolean verifyImsi;

    @Column
    private Integer smsValidityPeriod;

    @Column
    private Integer viberValidityPeriod;

    @Column
    private Integer parsecoValidityPeriod;

    @Column
    private Integer voiceValidityPeriod;

    @Column
    private Integer facebookValidityPeriod;

    @Type(type="yes_no")
    @Column
    private boolean active;

    public MessageType(){
        this("MESSAGE");
    }

    public MessageType(String typeId) {
        this.typeId = typeId;
        this.acceptSms = true;
        this.acceptParseco = false;
        this.acceptViber = false;
        this.acceptVoice = false;
        this.acceptFacebook = false;
        this.smsValidityPeriod = null;
        this.viberValidityPeriod = 1;
        this.parsecoValidityPeriod = 1;
        this.voiceValidityPeriod = 1;
        this.facebookValidityPeriod = 1;
        this.verifyImsi = false;
    }

    public MessageType(String typeId, String description, boolean acceptSms, boolean acceptViber, boolean acceptVoice, boolean acceptParseco, boolean acceptFacebook, Integer smsValidityPeriod, Integer viberValidityPeriod, Integer parsecoValidityPeriod, Integer voiceValidityPeriod, Integer facebookValidityPeriod, boolean active, boolean verifyImsi) {
        this.typeId = typeId;
        this.description = description;
        this.acceptSms = acceptSms;
        this.acceptViber = acceptViber;
        this.acceptVoice = acceptVoice;
        this.acceptParseco = acceptParseco;
        this.acceptFacebook = acceptFacebook;
        this.smsValidityPeriod = smsValidityPeriod;
        this.viberValidityPeriod = viberValidityPeriod;
        this.parsecoValidityPeriod = parsecoValidityPeriod;
        this.voiceValidityPeriod = voiceValidityPeriod;
        this.facebookValidityPeriod = facebookValidityPeriod;
        this.active = active;
        this.verifyImsi = verifyImsi;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAcceptSms() {
        return acceptSms;
    }

    public void setAcceptSms(boolean acceptSms) {
        this.acceptSms = acceptSms;
    }

    public Integer getSmsValidityPeriod() {
        return smsValidityPeriod;
    }

    public void setSmsValidityPeriod(Integer smsValidityPeriod) {
        this.smsValidityPeriod = smsValidityPeriod;
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

    public Integer getViberValidityPeriod() {
        return viberValidityPeriod;
    }

    public void setViberValidityPeriod(Integer viberValidityPeriod) {
        this.viberValidityPeriod = viberValidityPeriod;
    }

    public Integer getParsecoValidityPeriod() {
        return parsecoValidityPeriod;
    }

    public void setParsecoValidityPeriod(Integer parsecoValidityPeriod) {
        this.parsecoValidityPeriod = parsecoValidityPeriod;
    }

    public Integer getVoiceValidityPeriod() {
        return voiceValidityPeriod;
    }

    public void setVoiceValidityPeriod(Integer voiceValidityPeriod) {
        this.voiceValidityPeriod = voiceValidityPeriod;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVerifyImsi() {
        return verifyImsi;
    }

    public void setVerifyImsi(boolean verifyImsi) {
        this.verifyImsi = verifyImsi;
    }

    public boolean isAcceptFacebook() {
        return acceptFacebook;
    }

    public void setAcceptFacebook(boolean acceptFacebook) {
        this.acceptFacebook = acceptFacebook;
    }

    public Integer getFacebookValidityPeriod() {
        return facebookValidityPeriod;
    }

    public void setFacebookValidityPeriod(Integer facebookValidityPeriod) {
        this.facebookValidityPeriod = facebookValidityPeriod;
    }
}
