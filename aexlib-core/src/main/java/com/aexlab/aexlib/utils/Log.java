package com.aexlab.aexlib.utils;

import com.aexlab.aexlib.event.EventPub;
import com.aexlab.aexlib.event.message.ErrorMsg;
import com.aexlab.aexlib.event.message.LogMsg;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * @author Claudio Giampaolo
 * @version 0.10
 */

public class Log {
    
    static { 
        EventPub.aRegister(new Log()); 
    }
    private Log(){}
    
    public static final void d(String mask, Object... vars){
        if(XConfig.D) 
            System.out.println( String.format(mask, vars) );
    }
    
    public static final void dErr(String mask, Object... vars){
        if(XConfig.D) 
            System.err.println( String.format(mask, vars) );
    }
    
    public static final void log(String msg){
        EventPub.aPost(new LogMsg(msg));
    }
    
    public static final void err(String msg){
        EventPub.aPost( new ErrorMsg(msg, null) );
    }
    
    public static final void err(String msg, Exception e){
        EventPub.aPost( new ErrorMsg(msg, e) );
    }
    
    @Subscribe
    @AllowConcurrentEvents
    public final void doLog(LogMsg msg){
        System.out.println(msg.data());
    }
    
    @Subscribe
    @AllowConcurrentEvents
    public final void errLog(ErrorMsg emsg){
        System.err.println(emsg.data());
        Exception e = emsg.exception();
        if(e!=null && XConfig.D)
            e.printStackTrace();
    }
    
    
}
