package com.bankir.mgs.jersey.model;


public class ProcessorsStatusObject extends JsonObject {
    private Status queue;
    private Status delivery;
    private Status file;

    public void setQueue(Status queue) {
        this.queue = queue;
    }

    public void setDelivery(Status delivery) {
        this.delivery = delivery;
    }

    public void setFile(Status file) {
        this.file = file;
    }

    public static class Status {

        private boolean active;
        private long interval;
        private String status;

        public Status(boolean active, long interval, String status) {
            this.active = active;
            this.interval = interval;
            this.status = status;
        }
    }

}
