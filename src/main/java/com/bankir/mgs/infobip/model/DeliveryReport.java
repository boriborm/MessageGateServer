package com.bankir.mgs.infobip.model;

import java.util.List;

public class DeliveryReport {
    private List<InfobipObjects.Result> results;
    private InfobipObjects.RequestError requestError;
    public List<InfobipObjects.Result> getResults() {
        return results;
    }

    public InfobipObjects.RequestError getRequestError() {
        return requestError;
    }
}
