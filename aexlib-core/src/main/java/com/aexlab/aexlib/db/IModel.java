
package com.aexlab.aexlib.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.persistence.Transient;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.6
 * 
 */

public abstract class IModel {
    
    protected static ArrayList<String> schema(Class modelClass){
        ArrayList<String> cols = new ArrayList<>();
        
        for(Field f : modelClass.getDeclaredFields()){
            if( f.isAnnotationPresent( (Class<? extends Annotation>) Transient.class) )
                continue;
            
            cols.add( f.getName() );
        }
        
        return cols;
    }
    
    public abstract ArrayList<String> schema();
    
    public abstract void setUuid(int uuid);
    public abstract int getUuid();
}
