package com.bankir.mgs.infobip.model;

public class Viber {
    String text;
    Integer validityPeriod;
    String imageURL;
    String buttonText;
    String buttonURL;
    boolean isPromotional;

    public Viber(String text, Integer validityPeriod){
        this.text = text;
        this.validityPeriod = validityPeriod;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public void setButtonURL(String buttonURL) {
        this.buttonURL = buttonURL;
    }

    public void setPromotional(boolean promotional) {
        isPromotional = promotional;
    }
}
