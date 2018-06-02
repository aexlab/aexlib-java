package com.aexlab.aexlib.io.net;

import com.aexlab.aexlib.io.IOConnector;
import com.aexlab.aexlib.io.ResultListener;
import com.aexlab.aexlib.utils.Log;
import com.aexlab.aexlib.utils.Utils;
import com.aexlab.aexlib.utils.XConfig;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.10
 */

public abstract class Server implements IOConnector {
    
    // @see: http://www.html.it/articoli/i-socket-4/
    
    private boolean run = true;
    private ServerSocket mServer;
    private String mClassName;
    
    public ServerSocket server() throws Exception
    {
        if(mServer == null)
            mServer = createSocket();
        
        return mServer;
    }
    
    private boolean mKeepAlive = false;
    protected void keepAlive(boolean keep){
        mKeepAlive = keep;
    }
    protected boolean isKeepAlive(){
        return mKeepAlive;
    }
    
    private int mSocTimeout = 10000;
    protected int sockTimeout(){
        return mSocTimeout;
    }
    protected void sockTimeout(int t){
        mSocTimeout = t;
    }
    
    private Runnable mServerListener = new Runnable() {
        @Override
        public void run() {
            
            while (run) {
                Log.log( mClassName + " listening on port: " + mServer.getLocalPort() );
                try {
                    mHandler = new SocketHandler( mServer.accept() );
                    mHandler.start();
                    
                } catch(Exception ex){
                    Log.err(mClassName + " exc.:\n    " + ex.getMessage());      //, ex
                }
            }
            
        }
    };
    
    private SocketHandler mHandler = null;
    
    class SocketHandler extends Thread
    {
        private Socket mSock;
        public SocketHandler(final Socket socket) throws Exception {
            
            mSock = socket;
            try{
                mSock.setKeepAlive(mKeepAlive);
                if(!mKeepAlive)
                    mSock.setSoTimeout(mSocTimeout);
                
            } catch(Exception e) {
                
                if(mSock!=null) 
                    try { mSock.close(); mSock = null;} catch (Exception ex) { }
                throw new Exception(e);
            } 
        }
        
        private BufferedInputStream bis = null;
        private BufferedOutputStream bos = null;
        
        public BufferedInputStream in(){
            return bis;
        }
        public BufferedOutputStream out(){
            return bos;
        }
        
        @Override
        public void run() {

            onStateChanged(true);
            try {
                bis = new BufferedInputStream( mSock.getInputStream() );
                bos = new BufferedOutputStream( mSock.getOutputStream() );
                
                do{
                    onConnect(bis, bos);
                } while (mKeepAlive);
            
            } catch (Exception ex) {
                Log.err(mClassName + ".SocketHandler exc.: " + ex.getMessage(), ex);
                
            } finally {
                if (bis != null)
                    try { bis.close(); } catch (Exception e) { e.printStackTrace(); } finally { bis = null; }
                if (bos != null)
                    try { bos.close(); } catch (Exception e) { e.printStackTrace(); } finally { bos = null; }
                
                if (mSock != null) 
                    try { mSock.close(); } catch (Exception e) { e.printStackTrace(); } finally { mSock = null; }
                    
                onStateChanged(false);
            }
        }
    }
    
    @Override
    public void start() {
        try {
            mClassName = this.getClass().getSimpleName();
            server();       // init the server
            run=true;
            onStart();
            new Thread(mServerListener).start();
        } catch (Exception ex) {
            Log.err(mClassName + " start() exc:\n    " +ex.getMessage());    //, ex
        }
    }

    @Override
    public void stop() {
        try {
            run=false;
            onStop();
            if(mServer!=null)
                mServer.close();
        } catch (Exception ex) {
            Log.err(mClassName + " stop() exc:\n    " +ex.getMessage());     //, ex
        }
    }
    
    private final Object sendLock = new Object();
    @Override
    public void send(final byte[] message, final BufferedOutputStream os) throws Exception {
        
        synchronized(sendLock) {
            BufferedOutputStream bos = os;
            if(bos == null && mKeepAlive) 
                try{ 
                    bos = mHandler.out(); 
                } catch(Exception e){
                    throw new Exception("socket closed!", e);
                }
        
            bos.write(message);
            bos.flush();
            if(XConfig.D) 
                //Log.log(mClassName + " send: " + new String(message));
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
            Log.err(mClassName+"@"+mServer.getLocalPort()+": "+cs);
        } catch(Exception e){
            Log.err(mClassName + " unreachable!");
        }
    }
    
    protected abstract ServerSocket createSocket() throws Exception;
    protected abstract void onStart();
    protected abstract void onStop();
    protected abstract void onConnect(BufferedInputStream is, BufferedOutputStream os) throws Exception;
    protected abstract void onError(Exception ex);
    
}
