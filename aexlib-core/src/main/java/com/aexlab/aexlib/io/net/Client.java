package com.aexlab.aexlib.io.net;

import com.aexlab.aexlib.io.IOConnector;
import com.aexlab.aexlib.io.ResultListener;
import com.aexlab.aexlib.utils.Log;
import com.aexlab.aexlib.utils.Utils;
import com.aexlab.aexlib.utils.XConfig;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.10
 */

public abstract class Client implements IOConnector {
    
    // @see: http://www.html.it/articoli/i-socket-5/
    
    private boolean autoConnect = true;
    private Socket mClient = null;
    private String mClassName;
    
    protected final Object waitCondition = new Object();
    
    public Socket client() throws Exception
    {
        return mClient;
    }
    
    private boolean mKeepAlive = false;
    protected void keepAlive(boolean keep){
        mKeepAlive = keep;
    }
    
    
    private Runnable mClientListener = new Runnable() {
        @Override
        public void run() {
            
            while (autoConnect) {
                ///@todo: Log.d("%s connecting ...", mClassName);
                
                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                
                try {
                    synchronized(waitCondition){
                        mClient = createSocket();
                        onStateChanged(true);

                        bis = new BufferedInputStream( mClient.getInputStream() );
                        bos = new BufferedOutputStream( mClient.getOutputStream() );
                    }
                    
                    do {
                        onReady(bis, bos);
                    } while (mKeepAlive);
                
                } catch (ConnectException ce) {
                    
                    if(XConfig.D) Log.err(mClassName + " Client Connect exc.: " + ce.getMessage() );
                    try{ Thread.sleep(1000); } catch(Exception e){}
                    
                } catch (Exception ex) {
                    
                    if(XConfig.D) Log.err(mClassName + " exc.:\n    " + ex.getMessage(), ex);
                    try{ Thread.sleep(1000); } catch(Exception e){}
                    
                } finally {
                    
                    if(bis!=null) 
                        try{ bis.close(); } catch(Exception e){} finally { bis = null; }
                    if (bos != null)
                        try { bos.close(); } catch (Exception e) { } finally { bos = null; }
                    
                    if(mClient!=null) 
                        try { mClient.close(); } catch(Exception e){ mClient = null; }
                    
                    onStateChanged(false);
                }
                
            }
            
        }
    };
    
    @Override
    public void start() {
        
        mClassName = this.getClass().getSimpleName();
        autoConnect=true;
        onStart();
        new Thread(mClientListener).start();
    }

    @Override
    public void stop() {
        
        try {
            autoConnect=false;
            onStop();
            if(mClient!=null)
                mClient.close();
        } catch (Exception ex) {
            Log.err(mClassName + " stop() exc:\n    " + ex.getMessage(), ex);
        }
    }
    
    
    private final Object sendLock = new Object();
    @Override
    public void send(final byte[] message, final BufferedOutputStream bos) throws Exception {
        synchronized(sendLock) {
            bos.write(message);
            bos.flush();
            if(XConfig.D) 
                //Log.log( mClassName + " send: " + new String(message) );
                Log.log( String.format("%s send: %s", mClassName, Utils.bytesToHex(message)) );
        }
    }
    
    
    private ResultListener mListener = new ResultListener() {
        @Override
        public void onResult(Object result) {}
    };
    
    @Override
    public ResultListener resultListener(){
        return mListener;
    }
    
    @Override
    public IOConnector setResultListener(ResultListener listener){
        mListener = listener;
        return this;
    }
    
    protected void onStateChanged(boolean isConnected){
        try{
            String cs = (isConnected) ? "connected!": "disconnected!";
            Log.dErr(mClassName+"@"+mClient.getInetAddress().getHostAddress()+": "+cs);
        } catch(Exception e){
            Log.err(mClassName + " remote server unreachable!");
        }
    }
    
    protected abstract void onStart();
    protected abstract void onStop();
    protected abstract Socket createSocket() throws Exception;
    protected abstract void onReady(BufferedInputStream is, BufferedOutputStream os) throws Exception;
    protected abstract void onError(Exception ex);
    
}
