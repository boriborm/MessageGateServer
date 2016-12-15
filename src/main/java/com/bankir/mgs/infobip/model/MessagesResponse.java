package com.bankir.mgs.infobip.model;

import java.util.List;

public class MessagesResponse {

    private String bulkId;
    private List<Message> messages;
    private RequestError requestError;

    public String getBulkId() {
        return bulkId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public RequestError getRequestError() {
        return requestError;
    }

    public void setRequestError(RequestError requestError) {
        this.requestError = requestError;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getError(){
        String ret = null;
        if (this.requestError!=null) ret = this.requestError.getError();
        return ret;
    }
}
