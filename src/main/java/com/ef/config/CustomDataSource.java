package com.ef.config;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class CustomDataSource {
	 
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;
 
    static {
    	config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl( "jdbc:mysql://localhost:3306/parser?useSSL=false&rewriteBatchedStatements=true" );
        config.setUsername( "root" );
        config.setPassword( "root" );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
    }
 
    private CustomDataSource() {}
 
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
