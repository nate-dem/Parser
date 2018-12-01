package com.ef.config;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	      return new ParserRepoImpl();
	   }
	   
	   @Bean
	   public Options getCliOptions(){
			Options options = new Options();
			
			options.addOption("s", "startDate", true, "Start date");
			options.addOption("d", "duration", true, "Duration");
			options.addOption("t", "threshold", true, "Threshold");
			options.addOption("a", "accesslog", true, "Access log file");
			
			return options;
	   }
	   
	   @Bean
	   public CommandLineArgsParser getCommandLineParseController(){
		   Options options = getCliOptions();
		   CommandLineParser parser = new DefaultParser();
		   return new CommandLineArgsParser(options, parser);
	   }
	   
	   @Bean
	   public ParserController webLogParserController(){
		   return new ParserController(getWebLogParserService());
	   }

}
