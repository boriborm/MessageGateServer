package com.bankir.mgs;

import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.InfobipObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AbstractProcessor implements Runnable {
    private volatile boolean processorEnabled = false;
    private volatile long sleepTime = 10000;
    private volatile boolean processSignal=false;
    private Thread thread;
    private String status;

    public synchronized void startProcessor(String user){
        Logger logger = LoggerFactory.getLogger(getClass().getName());
        logger.info("Starting processor by user {}", user);
        this.processorEnabled=true;
        if (this.thread==null) {

            this.thread = new Thread(this);
            this.thread.start();
            this.setStatus("Processor "+this.getClass().getName()+" started");
        }
    }

    public boolean isActive(){ return this.processorEnabled;}

    public synchronized void stopProcessor(String user){
        if (this.thread.isAlive()) processorEnabled=false;
        Logger logger = LoggerFactory.getLogger(getClass().getName());
        logger.info("Stopping processor by user {}", user);
    }

    private void stopProcessorWithError(Exception e){
        processorEnabled = false;
        this.setErrorStatus("Error: "+e.getMessage(), e);
        this.thread = null;
        InfobipMessageGateway ims = null;

        List<InfobipObjects.Destination> destinations = new ArrayList<>();

        String msgId = Config.getSettings().getMessageIdPrefix()+"-ERR-"+ new Date().getTime();
        int counter=0;
        for (String phone: Config.getSettings().getStopProcessNotificationConfig().getPhones()){
            counter++;
            InfobipObjects.Destination destination = new InfobipObjects.Destination(msgId+"-"+counter, phone);
            destinations.add(destination);
        }

        String msg = "Stop processor with error "+getClass().getName()+ e.getMessage().substring(0,100);

        try {
            ims = new InfobipMessageGateway();
            ims.sendAdvancedMessage( new InfobipObjects.OmniAdvancedMessage( destinations, msg));
        } catch (InfobipMessageGateway.RequestErrorException ignored) {
        }

        if (ims!=null){
            try{ ims.stop();} catch (Exception ignored){}
        }

    }

    public synchronized void setSleepTime(long sleepTime){
        this.sleepTime = sleepTime;
    }

    public long getSleepTime(){
        return this.sleepTime;
    }

    public synchronized void signal(){
        processSignal = true;
    }

    @Override
    public void run(){
        Long currentMilliseconds = System.currentTimeMillis();
        Long prevMilliSeconds = currentMilliseconds;

        try {
            while(processorEnabled){
                Thread.sleep(100);
                if (((currentMilliseconds-prevMilliSeconds)>sleepTime)||(processSignal )) {
                    prevMilliSeconds = currentMilliseconds;
                    processSignal = false;
                    try {
                        process();
                    } catch (Exception e) {
                        stopProcessorWithError(e);
                        return;
                    }
                }
                currentMilliseconds = System.currentTimeMillis();

            }
        } catch (InterruptedException e) {
        }
        this.thread = null;
        this.setStatus("Processor "+this.getClass().getName()+ " stopped");
    }

    protected void process()  throws Exception {}

    public String getStatus() { return this.status; }

    protected void setStatus(String status) {
        this.status = status;
        Logger logger = LoggerFactory.getLogger(getClass().getName());
        logger.info(this.getStatus());
    };

    protected void setErrorStatus(String status, Exception e) {
        this.status = status;
        Logger logger = LoggerFactory.getLogger(getClass().getName());
        logger.error(this.getStatus(), e);
    }
}
