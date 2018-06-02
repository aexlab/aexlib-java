package com.aexlab.aexlib.io;

import java.io.BufferedOutputStream;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.7
 */

public interface IOConnector {
    
    public void start();
    public void stop();
    
    public void onInput(Object event);
    public void send(byte[] message, BufferedOutputStream bos) throws Exception;
    
    public IOConnector setResultListener(ResultListener listener);
    public ResultListener resultListener();
}
