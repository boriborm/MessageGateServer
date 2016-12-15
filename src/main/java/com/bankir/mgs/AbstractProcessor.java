package com.bankir.mgs;

public class AbstractProcessor implements Runnable {
    private volatile boolean processorEnabled = true;
    private volatile long sleepTime = 10000;
    private volatile boolean processSignal=false;
    private Thread thread;

    public synchronized void startProcessor(){
        this.processorEnabled=true;
        if (this.thread==null) {
            this.thread = new Thread(this);
            this.thread.start();
            System.out.println("Processor "+this.getClass().getName()+" started");
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
                        e.printStackTrace();
                    }
                }
                currentMilliseconds = System.currentTimeMillis();

            }
        } catch (InterruptedException e) {
        }
        this.thread = null;
        System.out.println("processor stopped");
    }

    protected void process()  throws Exception {}
}
