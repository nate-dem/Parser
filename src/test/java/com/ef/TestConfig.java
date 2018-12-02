package com.ef;


import javax.sql.DataSource;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.ef.observer.ConsoleLogger;
import com.ef.observer.Observer;
import com.ef.repository.ParserRepo;
import com.ef.repository.ParserRepoImpl;
import com.ef.util.CommandLineArgsParser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan(basePackages = { "com.ef" })
public class TestConfig {

	@Bean
	public ParserRepo parserRepo() {
		ParserRepo parserRepo = new ParserRepoImpl();
		Observer consoleLogger = new ConsoleLogger();
		parserRepo.addObserver(consoleLogger);
		return parserRepo;
	}

	@Bean
	public Options cliOptions() {
		Options options = new Options();

		options.addOption("s", "startDate", true, "Start date");
		options.addOption("d", "duration", true, "Duration");
		options.addOption("t", "threshold", true, "Threshold");
		options.addOption("a", "accesslog", true, "Access log file");

		return options;
	}

	@Bean
	public CommandLineArgsParser getCommandLineArgsParser() {
		Options options = cliOptions();
		CommandLineParser parser = new DefaultParser();
		return new CommandLineArgsParser(options, parser);
	}

	/*
	@Bean
	public DataSource hikariDataSource() {
	    HikariConfig config = new HikariConfig();
	    HikariDataSource ds;
    	config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl( "jdbc:mysql://localhost:3306/parser?useSSL=false&rewriteBatchedStatements=true" );
        config.setUsername( "root" );
        config.setPassword( "root" );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
	    
		return ds;
	}*/
	
	@Bean
	public DataSource dataSource() {
		
		// no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder
			.setName("testDB;MODE=MySQL")
			.setType(EmbeddedDatabaseType.H2)
			.addScript("h2db-schema.sql")
			.build();
		return db;
	}

	@Bean
	@Qualifier("customJdbcTemplate")
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource());
		return jdbcTemplate;
	}

}
