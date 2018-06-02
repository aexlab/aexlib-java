package com.aexlab.aexlib.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

/**
 *
 * @author Claudio Giampaolo
 * @version 0.10
 */

public class ConnectionPool {
    
    private ConnectionPool(){}
    
    public static HikariDataSource buildDataSource(String dburl, String user, String pass){
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl(dburl);
        config.setUsername(user);
        config.setPassword(pass);
        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource ds = new HikariDataSource(config);
        
        ds.setAutoCommit(false);
        ds.setMaximumPoolSize(4);   //10
        
        return ds;
    }
    
    private static DataSource mDatasource = null;
    public static DataSource mainDataSource(){
        
        return mDatasource;
    }
    
    public static DataSource setMainDataSource(DataSource ds){
        
        mDatasource = ds;
        return mDatasource;
    }
    
    
    public static void close(DataSource ds){
        if(ds!=null)
            ( (HikariDataSource)ds ).close();
    }
    
}
