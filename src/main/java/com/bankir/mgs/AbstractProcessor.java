package com.bankir.mgs;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AbstractProcessor implements Runnable {
    private volatile boolean processorEnabled = false;
    private volatile long sleepTime = 10000;
    private volatile boolean processSignal=false;
    private Thread thread;
    private String status;

    public synchronized void startProcessor(){
        this.processorEnabled=true;
        if (this.thread==null) {
            this.thread = new Thread(this);
            this.thread.start();
            this.status = "Processor "+this.getClass().getName()+" started";
        }
    }

    public boolean isActive(){ return this.processorEnabled;}

    public synchronized void stopProcessor(){
        if (this.thread.isAlive()) processorEnabled=false;
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
                this.thread.sleep(100);
                if (((currentMilliseconds-prevMilliSeconds)>sleepTime)||(processSignal == true)) {
                    prevMilliSeconds = currentMilliseconds;
                    processSignal = false;
                    try {
                        process();
                    } catch (Exception e) {
                        processorEnabled = false;
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        setStatus(sw.toString());
                        System.out.println(getStatus());
                        this.thread = null;
                        return;
                    }
                }
                currentMilliseconds = System.currentTimeMillis();

            }
        } catch (InterruptedException e) {
        }
        this.thread = null;
        this.status = "Processor "+this.getClass().getName()+ " stopped";
    }

    protected void process()  throws Exception {}

    public String getStatus() { return this.status; }

    protected void setStatus(String status) {this.status = status;}
}
