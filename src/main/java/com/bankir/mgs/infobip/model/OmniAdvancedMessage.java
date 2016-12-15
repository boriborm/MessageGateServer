package com.bankir.mgs.infobip.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OmniAdvancedMessage {

    private String bulkId;
    private String scenarioKey;

    private List<Destination> destinations ;

    private Sms sms;
    private Viber viber;
    private Parseco parseco;
    private Voice voice;
    private Email email;

    private Date sendAt;



    public OmniAdvancedMessage(String scenarioKey, String bulkId, Date sendAt){
        this.scenarioKey = scenarioKey;
        this.bulkId = bulkId;
        this.sendAt = sendAt;
    }

    public String getBulkId() {
        return bulkId;
    }

    public void setBulkId(String bulkId) {
        this.bulkId = bulkId;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public Viber getViber() {
        return viber;
    }

    public void setViber(Viber viber) {
        this.viber = viber;
    }

    public Parseco getParseco() {
        return parseco;
    }

    public void setParseco(Parseco parseco) {
        this.parseco = parseco;
    }

    public Voice getVoice() {
        return voice;
    }

    public void setVoice(Voice voice) {
        this.voice = voice;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Date getSendAt() {
        return sendAt;
    }

    public void setSendAt(Date sendAt) {
        this.sendAt = sendAt;
    }

    public String getScenarioKey() {
        return scenarioKey;
    }

    public void setScenarioKey(String scenarioKey) {
        this.scenarioKey = scenarioKey;
    }

    public void addSms(String text, Integer validityPeriod){
        if ((text)!=null)
            this.sms = new Sms(text, validityPeriod);
        else
            this.sms = null;
    }

    public void addViber(String text, Integer validityPeriod){
        if ((text)!=null)
            this.viber = new Viber(text, validityPeriod);
        else
            this.viber = null;
    }

    public void addParseo(String text, Integer validityPeriod){
        if ((text)!=null)
            this.parseco = new Parseco(text, validityPeriod);
        else
            this.parseco = null;
    }

    public void addVoice(String text, Integer validityPeriod){
        if ((text)!=null)
            this.voice = new Voice(text, validityPeriod);
        else
            this.voice = null;
    }

    public void addEmail(String text, String subject){
        if ((text)!=null)
            this.email = new Email(text, subject);
        else
            this.email = null;
    }

    public void addDestination (Destination destination){
        if (this.destinations==null) this.destinations = new ArrayList<>();
        this.destinations.add(destination);
    }
}
