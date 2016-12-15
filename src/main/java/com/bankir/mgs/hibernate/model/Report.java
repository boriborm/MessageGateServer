package com.bankir.mgs.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="REPORTS")
public class Report implements Serializable {

    @Id
    @SequenceGenerator(name="SEQ_REPORTID", sequenceName="SEQ_REPORTID",allocationSize=1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="SEQ_REPORTID")
    @Column(name="id")
    private Long id;

    @Column (name="messageId")
    private Long messageId;

    @Column (name="statusName")
    private String statusName;

    @Column (name="statusGroupName")
    private String statusGroupName;

    @Column (name="statusDescription")
    private String statusDescription;

    @Column (name="channel")
    private String channel;

    @Column (name="sentAt")
    private Date sentAt;

    @Column (name="doneAt")
    private Date doneAt;

    @Column (name="reportDate")
    private Date reportDate;

    @Column (name="priceCurrency")
    private String priceCurrency;

    @Column (name="pricePerMessage")
    private BigDecimal pricePerMessage;


    @Column (name="messageCount")
    private int messageCount;

    @Column(name="mccMnc")
    private String mccMnc;

    public Report(){}

    public Report(Long messageId, String statusName, String statusGroupName, String statusDescription) {
        this.messageId = messageId;
        this.statusName = statusName;
        this.statusGroupName = statusGroupName;
        this.statusDescription = statusDescription;
        this.reportDate = new Date();
        this.messageCount = 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusGroupName() {
        return statusGroupName;
    }

    public void setStatusGroupName(String statusGroupName) {
        this.statusGroupName = statusGroupName;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public Date getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(Date doneAt) {
        this.doneAt = doneAt;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }

    public BigDecimal getPricePerMessage() {
        return pricePerMessage;
    }

    public void setPricePerMessage(BigDecimal pricePerMessage) {
        this.pricePerMessage = pricePerMessage;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getMccMnc() {
        return mccMnc;
    }

    public void setMccMnc(String mccMnc) {
        this.mccMnc = mccMnc;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }
}
