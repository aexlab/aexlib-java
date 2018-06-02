package com.aexlab.aexlib.event.message;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.9
 */


public abstract class ServiceMsg extends BaseMsg{
    
    public final String sessionID;
    
    public ServiceMsg(String message, String sID){
        super(message);
        sessionID = sID;
    }
    
    /**
     * Send over socket/channel string or byte[] message.
     * @param data - string or byte[]
     */
    public abstract void serve(Object data) throws Exception;
}
