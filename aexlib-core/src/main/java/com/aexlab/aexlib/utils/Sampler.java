package com.aexlab.aexlib.utils;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.10
 */

public abstract class Sampler  extends Thread {
    
    private long dT;
    private boolean toRun;
    
    public Sampler(){
        dT = 1000;
        toRun = false;
    }
    
    public synchronized void open(){
        toRun = true;
        start();
    }
    
    public synchronized void close(){
        toRun = false;
        lock.notify();
        //try{ join(1); }catch(Exception e){}
    }
    
    public synchronized void period(long ms, boolean restart){
        dT = ms;
        if(restart) lock.notify();
    }
    
    private final Object lock = new Object();
    
    @Override
    public void run() {
        while(toRun)
            try {
                if(XConfig.D) Log.log( getClass().getSimpleName() + ".run(): sampling" );
                
                synchronized (this) {
                    if (!sample())
                        break;
                }
                
                synchronized (lock) {
                    lock.wait(dT);
                }
                
            } catch (Exception ex) {
                Log.dErr("Sampler exc.:\n    %s", ex.getMessage() );
            }
    }
    
    protected abstract boolean sample();
    
}
