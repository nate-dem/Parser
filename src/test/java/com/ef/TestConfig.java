package com.ef;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.ef.observer.ConsoleLogger;
import com.ef.observer.Observer;
import com.ef.repository.ParserRepo;
import com.ef.repository.ParserRepoImpl;
import com.ef.util.CommandLineArgsParser;

@Configuration
@PropertySource("classpath:application-test.properties")
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

	@Bean
	public DataSource dataSource() {
		
		// no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder
			.setName("testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;") // DATABASE_TO_UPPER=false;
			.setType(EmbeddedDatabaseType.H2)
			.addScript("h2db-schema.sql")
			.build();
		return db;
	}

	@Bean
	@Qualifier("customJdbcTemplate")
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setResultsMapCaseInsensitive(true);
		jdbcTemplate.setDataSource(dataSource());
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

}
