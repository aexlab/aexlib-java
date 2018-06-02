package com.aexlab.aexlib.event.message;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.4
 */

public class ErrorMsg  extends BaseMsg {
    private final Exception e;
    
    public ErrorMsg(String message, Exception exc){
        super(message);
        e = exc;
    }
    
    public Exception exception(){
        return e;
    }
}
