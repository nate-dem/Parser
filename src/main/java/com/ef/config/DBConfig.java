package com.ef.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@Profile("default")
public class DBConfig {
	
	@Autowired
	private Environment env;	

	@Bean
	public DataSource hikariDataSource() {
	    HikariConfig config = new HikariConfig();
	    HikariDataSource ds;
    	config.setDriverClassName(env.getProperty("jdbc.driver"));
        config.setJdbcUrl( env.getProperty("jdbc.url") );
        config.setUsername( env.getProperty("jdbc.username") );
        config.setPassword( env.getProperty("jdbc.password") );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
	    
		return ds;
	}

	@Bean
	@Qualifier("customJdbcTemplate")
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(hikariDataSource());
		return jdbcTemplate;
	}

}
