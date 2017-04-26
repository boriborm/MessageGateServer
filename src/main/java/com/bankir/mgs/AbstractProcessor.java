package com.bankir.mgs;

import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.InfobipObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AbstractProcessor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
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
            this.setStatus(new Date(), "Processor "+this.getClass().getName()+" started");
        }
    }

    public boolean isActive(){ return this.processorEnabled;}

    public synchronized void stopProcessor(String user){
        if (this.thread.isAlive()) processorEnabled=false;
        Logger logger = LoggerFactory.getLogger(getClass().getName());
        logger.info("Stopping processor by user {}", user);
    }

    private void stopProcessorWithError(Date date, Exception e){
        processorEnabled = false;
        this.setErrorStatus(date, " Error: "+e.getMessage(), e);
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

        String msg = date + " Stop processor with error "+getClass().getName()+ e.getMessage().substring(0,100);

        try {
            ims = new InfobipMessageGateway();
            logger.debug("Try send sms about error");
            ims.sendSimpleMessage( new InfobipObjects.OmniSimpleMessage( destinations, msg));
            logger.debug("Sms about error sent");
        } catch (InfobipMessageGateway.RequestErrorException ignored) {
            logger.error("Sms about error sent with error:", ignored);
        }

        if (ims!=null){
            try{ ims.stop();} catch (Exception ignored){}
        }
    }

    synchronized void setSleepTime(long sleepTime){
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
                    }catch(PersistenceException pe){
                        //Если ошибка соединения, то ничего не делаем, просто выходим из процесса.
                        Throwable cause = getThrowableCaused(pe, SQLException.class);
                        if (cause!=null) {
                            SQLException sqlException = (SQLException) cause;
                            logger.error("PROCESSOR SQL ERROR: {}, {}", sqlException.getErrorCode(), sqlException.getMessage());
                        }
                    }catch (Exception e) {
                        logger.error("STOP PROCESSOR. PROCESSOR ERROR:", e);
                        stopProcessorWithError(new Date(), e);
                        return;
                    }
                }
                currentMilliseconds = System.currentTimeMillis();

            }
        } catch (InterruptedException e) {
            logger.error("PROCESS INTERRUPTION:", e);
            stopProcessorWithError(new Date(), e);
            return;
        }
        this.thread = null;
        this.setStatus(new Date(), "Processor "+this.getClass().getName()+ " stopped");
    }

    protected void process()  throws Exception {}

    public String getStatus() { return this.status; }

    private void setStatus(Date date, String status) {
        this.status = date+" "+status;
        logger.info(this.getStatus());
    }

    void setErrorStatus(Date date, String status, Exception e) {
        this.status = date+ " "+ status;
        logger.error(this.getStatus(), e);
    }

    private Throwable getThrowableCaused(Throwable e, Class clazz) {
        Throwable cause = null;
        Throwable result = e;

        while(null != (cause = result.getCause()) && (result != cause))  {
            result = cause;
            if (result.getClass().isAssignableFrom(clazz)){
                break;
            }
        }
        return result;
    }
}
