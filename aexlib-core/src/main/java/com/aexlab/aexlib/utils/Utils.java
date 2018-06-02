package com.aexlab.aexlib.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.imageio.stream.FileImageInputStream;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.10
 */

public class Utils {
    
    // FILE
    public static byte[] read(InputStream is, final int len) throws Exception {
        int b;
        if(len<0){
            byte[] 
                    data = new byte[0],
                    buff = new byte[4096];
            while((b = is.read(buff, 0, buff.length)) > -1){
                byte[] tb = new byte[data.length + b];
                System.arraycopy(data, 0, tb, 0, data.length);
                System.arraycopy(buff, 0, tb, data.length, b);
                data = tb;
                //System.out.println("Utils.read(-1): " + b);
            }
            //System.out.println(bytesToHex(data));
            return data;
            
        }
        
        byte[] data = new byte[len];
        int rb = 0;
        while((b = is.read(data, rb, data.length - rb)) > -1)
            rb += b;

        return data;
    }
    
    public static byte[] readline(InputStream is) throws Exception {
        
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        while(true){
            int c = is.read();
            if(c==0x0D)
                c = is.read();
            if(c==0x0A || c==-1)
                break;
            bao.write(c);
        }

        return bao.toByteArray();
    }
    
    public static ArrayList<String> shell(String cmd) throws Exception {
        
        ArrayList<String> lines = new ArrayList<>();
        
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader pbr = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while (p.isAlive()) {
            String l = pbr.readLine();
            if(l!=null)
                lines.add(l);
        }
        
        return lines;
    }
    
    public static byte[] loadFile(String filename) {
        byte[] data = new byte[8192];
        
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        FileImageInputStream fis = null;
        try{
            fis = new FileImageInputStream( new File(filename) );
            
            int b=0;
            while( (b=fis.read(data))>0 )
                bao.write(data, 0, b);
        } catch(Exception ex) {
            //ex.printStackTrace();
            Log.dErr("loadFile exc.:\n    %s", ex.getMessage());
        } finally {
            if(fis!=null) try{ fis.close(); } catch(Exception e){}
        }
        
        data = bao.toByteArray();
        return data;
    }
    
    public static List<String> loadFileArray(final String filename){
        ArrayList<String> file = new ArrayList<>();
        FileImageInputStream fis = null;
        
        try{
            fis = new FileImageInputStream( new File(filename) );
            while(true){
                String line = fis.readLine();
                if(line == null)
                    break;
                file.add(line);
            }
        } catch(Exception ex) {
            //ex.printStackTrace();
            Log.dErr("loadFileArray %s:\n    %s", filename, ex.getMessage());
        } finally {
            if(fis!=null) try{ fis.close(); } catch(Exception e){}
        }
        
        return file;
    }
    
    
    public static Properties loadConfig(final byte[] data){
        
        return loadConfig( new ByteArrayInputStream(data) );
    }
    
    public static Properties loadConfig(final String filename){
        try{
            return loadConfig(new FileInputStream(filename));
        } catch(Exception e) {
            return new Properties();
        }
    }
    
    private static Properties loadConfig(final InputStream in){
        
        Properties p = new Properties();
        try{
            p.load(in);
            in.close();
        } catch(Exception e){
            
        }
        
        return p;
    }
    
    public static LinkedHashMap<String, Object> mapConfig(final String filename, final String delim){
        List<String> cSet = loadFileArray(filename);
        //System.out.println("file lines: " + cSet);
        
        return mapConfig(cSet, delim);
    }
    
    public static LinkedHashMap<String, Object> mapConfig(final List<String> lines, final String delim){
        
        return mapper(
                lines, delim, 
                //(Object key, Object value) -> ((String)value).isEmpty() || ((String)key).startsWith("#") 
                new TokenFilter() {
                    @Override
                    public boolean filter(Object key, Object value) {
                        return ((String)value).isEmpty() || ((String)key).startsWith("#");
                    }
                }
        );
        
    }
    
    public interface TokenFilter{
        public boolean filter(Object key, Object value);
    }
    public static LinkedHashMap<String, Object> mapper(final List<String> lines, final String delim, final TokenFilter filter){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        
        for(String l : lines){
            StringTokenizer st = new StringTokenizer(l, delim);
            String k = "", v = "";
            try{ k = st.nextToken().trim(); } catch(Exception e){}
            try{ v = st.nextToken().trim(); } catch(Exception e){}
            
            boolean condition = (filter==null) ? false : filter.filter(k, v);
            if( k.isEmpty() || condition )
                continue;
            map.put(k,v);
        }
        //System.out.println("mapper: " + mConfig);
        
        return map;
    }
    
    public static String[] tokenizer(final String raw, final String delim){
        
        StringTokenizer st = new StringTokenizer(raw, delim);
        String[] tokens = new String[st.countTokens()];
        
        for(int i=0; st.hasMoreTokens(); i++){
            tokens[i] = st.nextToken();
        }
        
        return tokens;
    }
    
    
    // HEX
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    //hexStringToByteArray
    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    public static long hexToDec(String s) {
        long n = 0;
        ByteBuffer bb = ByteBuffer.wrap( hexToBytes(s) );
        switch(s.length())
        {
            case 2: n = bb.get(); break;
            case 4: n = bb.getShort(); break;
            case 8: n = bb.getInt(); break;
            default: n = bb.getLong(); break;
        }
        return n;
    }
    
    public static float hexToFloat(String s) {
        return ByteBuffer.wrap( hexToBytes(s)).getFloat();
    }
    public static double hexToDouble(String s) {
        return ByteBuffer.wrap( hexToBytes(s)).getDouble();
    }
    
    public static void printBuffer(String prefix, byte[] buffer, int max, boolean showString){
        
        int bytes = max;
        if(buffer.length < max)
            bytes = buffer.length;
        
        byte[] tmpB = new byte[bytes];
        ByteBuffer bb = ByteBuffer.wrap(buffer).get(tmpB);
        Log.log(prefix);
        Log.log("hex: \n" + bytesToHex(tmpB));
        if(showString)
            Log.log("data: \n" + new String(tmpB));
    }
    
    public static void printBench(String prefix, long time, long start, boolean nano) {
        double dT = time - start;
        if(nano)
            dT = dT/1000000;
        
        Log.log( (prefix + " Bench: "+ dT + " ms").trim() );
    }
    
    public static void stringStat(final String s){
        byte[] sb = s.getBytes();
        System.out.println(
                String.format("String stat:\n\t%s (%d bytes) len: %d chars\n\t%s", 
                        s, sb.length, s.length(), bytesToHex(sb))
        );
    }
    
    //INPUT - PROMPT
    public static String input(){
        return input(null, System.in, false);
    }
    
    public static String input(String question, InputStream is, boolean isPassword)
    {
        if(question!=null && !question.isEmpty())
            System.out.print(question);
        
        String in = "";
        BufferedReader console = new BufferedReader( new InputStreamReader(is) );
        try {
            if(isPassword){
                java.io.Console csl = System.console();
                in = (csl==null) ? console.readLine() : new String( csl.readPassword() );
            } else
                in = console.readLine();
            
        } catch (Exception ex) {
            in = ex.getMessage();
        }
        
        return in;
    }
    
}
