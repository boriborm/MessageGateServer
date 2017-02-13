package com.bankir.mgs.infobip.model;

/**
 * Created by bankir on 24.01.17.
 */
public class ImsiResponse {

    String imsi;

    InfobipObjects.RequestError requestError;

    public InfobipObjects.RequestError getRequestError() {
        return requestError;
    }
    public void setRequestError(InfobipObjects.RequestError requestError) {
        this.requestError = requestError;
    }

    public String getError(){
        String ret = null;
        if (this.requestError!=null) ret = this.requestError.getError();
        return ret;
    }

    public String getImsi() {
        return imsi;
    }
}
