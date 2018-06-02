package com.aexlab.aexlib.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedHashMap;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.8
 */

public class Console {
    
    private static final PipedOutputStream pos = new PipedOutputStream();
    private Console(){ }
    
    public interface Sender {
        public String peerAddress();
        public boolean handleInput(String line) throws Exception;
    }
    
    private static boolean run;
    
    
    public static void open(boolean hasPrompt) throws Exception
    {
        run = true;
        InputStream is = (hasPrompt) ? System.in : new PipedInputStream(pos);
        BufferedReader console = new BufferedReader( new InputStreamReader(is) );
        
        while(run){
            String line = console.readLine();
            //System.out.println("captured: "+line);
            if( !handleInput(line) )
                break;
        }
    }
    
    private static final LinkedHashMap<String, Sender> mConsoles = new LinkedHashMap<>();
    private static Sender mSender = null;
    
    public static final Sender register(final Sender sender) {
        
        Sender cs = mConsoles.get( sender.peerAddress() );
        if(cs==null) {
            if(mConsoles.isEmpty())
                mSender = sender;
            
            mConsoles.put(sender.peerAddress(), sender);
        }
        
        Log.d("console@%s", sender.peerAddress() );
        try{ sender.handleInput(NOTIFY_OPEN); } catch(Exception e) {}
        
        return sender;
    }
    
    public static final String NOTIFY_OPEN = "o!";
    private static final String 
            OPEN = "o ",
            LIST = "active",
            QUIT = "q!";
    
    public static String help(){
        return "\n# Console commands: "
                + "\n "+LIST+" \t\tlist of console"
                + "\n "+OPEN+" <IP address> \t open a console of telemetry server at IP"
                + "\n "+QUIT+" \tclose console for current telemetry server"
                + "\n";
    }
    
    private static synchronized boolean handleInput(final String line) throws Exception {
        
        String ln = line;
        
        if( ln.startsWith(QUIT) ){
            close();
            return false;
        }
        
        if( ln.startsWith(LIST) ){
            for(String addr : mConsoles.keySet())
                Log.log(addr);
            return true;
        }
        
        if( ln.startsWith(OPEN)){
            
            String addr = ln.substring( OPEN.length() ).trim();
            Sender cs = mConsoles.get(addr);
            
            if(cs == null){
                Log.err("Unknown peer at " + addr);
                return true;
            }
            
            mSender = cs;
            ln = NOTIFY_OPEN;
        }
        
        if(mSender != null)
            mSender.handleInput(ln);
        else
            Log.err("NULL console handler!");        // can receive input, while waiting for a sender
        
        return true;
    }
    
    public static void push(final String message) throws Exception {
        
        handleInput(message);
    }
    
    public static void close() {
        run=false;
        try{
            pos.write("\n".getBytes(), 0, 1);
        } catch(Exception e){
            //e.printStackTrace();
        }
    }
    
    public static Sender activePeer(){
        return mSender;
    }
}
