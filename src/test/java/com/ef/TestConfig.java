package com.ef;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.ef.config.AppConfig;

@Configuration
@PropertySource("classpath:application-test.properties")
@ComponentScan(basePackages = { "com.ef" })
@Import(AppConfig.class)
public class TestConfig {
	
	@Bean
	public DataSource dataSource() {
		
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder
			.setName("testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;")
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

}
