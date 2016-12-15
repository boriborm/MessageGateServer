package com.bankir.mgs.infobip.model;

import java.math.BigDecimal;

public class Price {
    BigDecimal pricePerMessage;
    String currency;

    public BigDecimal getPricePerMessage() {
        return pricePerMessage;
    }

    public String getCurrency() {
        return currency;
    }
}
