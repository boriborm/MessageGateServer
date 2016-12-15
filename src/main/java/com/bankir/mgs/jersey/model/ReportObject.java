package com.bankir.mgs.jersey.model;


import java.math.BigDecimal;
import java.util.Date;

public class ReportObject {
    private Long id;
    private Long messageId;
    private Date sentAt;
    private Date doneAt;
    private String channel;
    private int messageCount;
    private BigDecimal pricePerMessage;
    private String priceCurrency;
    private Date reportDate;

    private String statusName;
    private String statusGroup;
    private String statusDescription;

    public ReportObject(Long id, Long messageId, Date sentAt, Date doneAt, String channel, int messageCount, BigDecimal pricePerMessage, String priceCurrency, Date reportDate, String statusName, String statusGroup, String statusDescription) {
        this.id = id;
        this.messageId = messageId;
        this.sentAt = sentAt;
        this.doneAt = doneAt;
        this.channel = channel;
        this.messageCount = messageCount;
        this.pricePerMessage = pricePerMessage;
        this.priceCurrency = priceCurrency;
        this.reportDate = reportDate;
        this.statusName = statusName;
        this.statusGroup = statusGroup;
        this.statusDescription = statusDescription;
    }
}
