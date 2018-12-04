package com.ef.config;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ef.observer.ConsoleLogger;
import com.ef.observer.Observer;
import com.ef.repository.ParserRepo;
import com.ef.repository.ParserRepoImpl;
import com.ef.util.CommandLineArgsParser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = { "com.ef" })
@PropertySource("classpath:application.properties")
public class AppConfig {
	
	@Autowired
	private Environment env;

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
	/*
	@Bean
	@Qualifier("h2DataSource")
	public DataSource h2DataSource() {
		
		// no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder
			.setName("testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;") // DATABASE_TO_UPPER=false;
			.setType(EmbeddedDatabaseType.H2)
			.addScript("h2db-schema.sql")
			.build();
		return db;
	}*/

	@Bean
	@Qualifier("customJdbcTemplate")
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(hikariDataSource());
		return jdbcTemplate;
	}
	
    @Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:messages");
		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setDefaultEncoding("UTF-8");
		// # -1 : never reload, 0 always reload
		messageSource.setCacheSeconds(0);
	    return messageSource;
	}

	@Bean
	public Validator getValidator(){
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		return factory.getValidator();
	}
	
}
