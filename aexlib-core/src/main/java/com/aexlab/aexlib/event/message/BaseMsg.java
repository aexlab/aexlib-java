package com.aexlab.aexlib.event.message;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.4
 */

abstract class BaseMsg {
    
    private final String m;
    
    public BaseMsg(String message){
        m = message;
    }
    
    public String data(){
        return m;
    }
    
}
