package com.bankir.mgs.infobip.model;

public class RequestError {

    ServiceException serviceException;
    String error;
    String errorType = "UNKNOWN_ERROR";

    public RequestError(String error){
        this.error = error;
    }

    public RequestError(String error, String errorType) {
        this.error = error;
        this.errorType = errorType;
    }

    public ServiceException getServiceException() {
        return serviceException;
    }

    public String getError() {
        return (error!=null?error:(serviceException!=null?serviceException.getText():null));
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorType() {
        return errorType;
    }
}
