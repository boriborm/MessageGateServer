package com.bankir.mgs.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name="MESSAGES")
public class Message implements Serializable {

    @Id
    @SequenceGenerator(name="SEQ_MESSAGEID", sequenceName="SEQ_MESSAGEID",allocationSize=50)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="SEQ_MESSAGEID")
    @Column(name="id")
    private Long id;

    @Column (name="typeId")
    private String typeId;

    @Column (name="phoneNumber")
    private String phoneNumber;

    @Column (name="emailAddress")
    private String emailAddress;

    @Column (name="smsText")
    private String smsText;

    @Column (name="viberText")
    private String viberText;

    @Column (name="parsecoText")
    private String parsecoText;

    @Column (name="voiceText")
    private String voiceText;

    @Column (name="emailText")
    private String emailText;

    @Column (name="scenarioId")
    private Long scenarioId;

    @Column (name="userId")
    private Long userId;

    @Column (name="sendAt")
    private Date sendAt;

    @Column (name="createDate")
    private Date createDate;

    @Column (name="emailSubject")
    private String emailSubject;

    @Column (name="externalId")
    private String externalId;

    public Message(){
        this.createDate = new Date();
    }

    public Message(String typeId, Long scenarioId, Long userId) {
        this();
        this.typeId = typeId;
        this.scenarioId = scenarioId;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }

    public String getViberText() {
        return viberText;
    }

    public void setViberText(String viberText) {
        this.viberText = viberText;
    }

    public String getParsecoText() {
        return parsecoText;
    }

    public void setParsecoText(String parsecoText) {
        this.parsecoText = parsecoText;
    }

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getSendAt() {
        return sendAt;
    }

    public void setSendAt(Date sendAt) {
        this.sendAt = sendAt;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailText() {
        return emailText;
    }

    public void setEmailText(String emailText) {
        this.emailText = emailText;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
