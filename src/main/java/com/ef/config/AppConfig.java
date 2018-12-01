package com.ef.config;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.ef.controller.CommandLineArgsParser;
import com.ef.controller.ParserController;
import com.ef.repository.ParserRepo;
import com.ef.repository.ParserRepoImpl;
import com.ef.service.ParserService;
import com.ef.service.ParserServiceImpl;

@Configuration
public class AppConfig {

	@Bean
	public ParserService getWebLogParserService() {
		return new ParserServiceImpl(getWebLogParserRepo());
	}

	@Bean
	public ParserRepo getWebLogParserRepo() {
		return new ParserRepoImpl(jdbcTemplate());
	}

	@Bean
	public Options getCliOptions() {
		Options options = new Options();

		options.addOption("s", "startDate", true, "Start date");
		options.addOption("d", "duration", true, "Duration");
		options.addOption("t", "threshold", true, "Threshold");
		options.addOption("a", "accesslog", true, "Access log file");

		return options;
	}

	@Bean
	public CommandLineArgsParser getCommandLineParseController() {
		Options options = getCliOptions();
		CommandLineParser parser = new DefaultParser();
		return new CommandLineArgsParser(options, parser);
	}

	@Bean
	public ParserController webLogParserController() {
		return new ParserController(getWebLogParserService());
	}

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		// MySQL database we are using
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/parser?rewriteBatchedStatements=true");
		dataSource.setUsername("root");
		dataSource.setPassword("root");

		// H2 database
		/*
		 * dataSource.setDriverClassName("org.h2.Driver");
		 * dataSource.setUrl("jdbc:h2:tcp://localhost/~/test");
		 * dataSource.setUsername("sa"); dataSource.setPassword("");
		 */
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource());
		return jdbcTemplate;
	}

}
