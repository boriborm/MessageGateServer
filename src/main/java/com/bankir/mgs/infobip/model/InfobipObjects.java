package com.bankir.mgs.infobip.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InfobipObjects {

    public class RequestError {

        ServiceException serviceException;
        String error;
        String errorType = "ERROR";

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

    public static class Flow {
        String from;
        String channel;

        public Flow(String from, String channel) {
            this.from = from;
            this.channel = channel;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

    }

    public static class Scenario {
        private String key;
        private String name;
        private List<InfobipObjects.Flow> flow;
        private InfobipObjects.RequestError requestError;

        @SerializedName("default")
        private Boolean defaultScenario = false;

        public Scenario (String key, String name, boolean defaultScenario){
            this.key = key;
            this.name = name;
            this.defaultScenario = defaultScenario;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public InfobipObjects.RequestError getRequestError() {
            return requestError;
        }

        public String getServiceErrorMessage(){
            String ret = null;
            if (requestError!=null&&requestError.getServiceException()!=null){
                ret = requestError.getServiceException().getText();
            }
            return ret;
        }

        public List<InfobipObjects.Flow> getFlow() {
            return flow;
        }

        public Boolean isDefaultScenario() {
            return defaultScenario;
        }

        public void setFlow(List<InfobipObjects.Flow> flow) {
            this.flow = flow;
        }
    }
}
