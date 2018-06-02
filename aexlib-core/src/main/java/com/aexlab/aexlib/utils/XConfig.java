package com.aexlab.aexlib.utils;

import com.google.common.io.Files;
import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.10
 * 
 */

public class XConfig {
    
    /// @todo: make dynamic for a restart()!?!
    
    protected static String CONFIG_FILE = "config.cfg";
    
    public static final String CONFIG_DATA = new String( Utils.loadFile(CONFIG_FILE) );
    private static final Properties mConfig = Utils.loadConfig( CONFIG_DATA.getBytes() );
    
    // HELPER
    protected static Integer value(String key, Integer defaultValue){
        Integer val = defaultValue;
        try{
            val = Integer.parseInt( (String) mConfig.get(key) );
        } catch(Exception e){}
        
        return val;
    }
    
    protected static String value(String key, String defaultValue){
        return (String) mConfig.getOrDefault(key, defaultValue);
    }
    
    protected static boolean value(String key, boolean defaultValue){
        return Boolean.parseBoolean(mConfig.getProperty(key, ""+defaultValue));
    }
    
    public static void update(String key, Object val){
        mConfig.put(key, val);
    }
    
    public static boolean saveConfig(){
        StringBuilder sb = new StringBuilder("# System Config\n\n");
        
        for(Map.Entry<Object, Object> entry : mConfig.entrySet() ){
            sb.append( String.format("%s = %s\n", entry.getKey(), ""+entry.getValue()) );
        }
        return saveConfig( sb.toString() );
    }
    
    public static boolean saveConfig(final String config){
        try{ 
            Files.write( config.getBytes(), new File(CONFIG_FILE) );
            /// @todo: restart the enviroment!
        } catch(Exception e) { return false; }
        
        return true;
    }
    
    public static boolean D = false;        // to enable debug
}
