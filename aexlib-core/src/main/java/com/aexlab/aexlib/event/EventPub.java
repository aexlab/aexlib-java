package com.aexlab.aexlib.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import java.util.concurrent.Executors;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.3
 */

// @see: https://github.com/syncany/syncany/blob/b044febd5e89ae41362b802bc1746915fe77beec/syncany-daemon/src/main/java/org/syncany/operations/daemon/DaemonWebServer.java
// @see: https://github.com/google/guava/wiki/EventBusExplained

public class EventPub {
    
    //SYNC
    private static final EventBus mBus = new EventBus();
    
    public static EventBus bus(){
        return mBus;
    }
    public static EventBus register(Object handler){
        mBus.register(handler);
        return mBus;
    }
    public static void unregister(Object handler){
        mBus.unregister(handler);
    }
    public static void post(Object event){
        mBus.post(event);
    }
    
    // ASYNC
    private static final AsyncEventBus mAsyncBus = new AsyncEventBus(Executors.newCachedThreadPool());
    
    public static AsyncEventBus asyncBus(){
        return mAsyncBus;
    }
    
    public static AsyncEventBus aRegister(Object handler){
        mAsyncBus.register(handler);
        return mAsyncBus;
    }
    public static void aUnregister(Object handler){
        mAsyncBus.unregister(handler);
    }
    
    public static void aPost(Object event){
        mAsyncBus.post(event);
    }
}
