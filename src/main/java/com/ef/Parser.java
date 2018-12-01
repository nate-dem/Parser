package com.ef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ef.config.AppConfig;
import com.ef.controller.CommandLineArgsParser;
import com.ef.controller.ParserController;
import com.ef.model.CommandLineArgs;

/*
 * Java parser that parses web server access log file and save to MySQL db.
 *
 * @author Natnael Demisse
 * @version 2.0
 * @since 2017-12-29
 */
public class Parser {

	private static final Logger LOGGER =  LoggerFactory.getLogger(Parser.class);

	
	public static void main(String[] args) {
		
	      ApplicationContext ctx = 
	    	         new AnnotationConfigApplicationContext(AppConfig.class);

		// instantiate parser service and repo layers
		//WebLogParserRepo parserRepo = ctx.getBean(WebLogParserRepo.class);
		//WebLogParserService parserService = ctx.getBean(WebLogParserService.class);
	    CommandLineArgsParser cmdParser = ctx.getBean(CommandLineArgsParser.class);
	    ParserController parserController = ctx.getBean(ParserController.class);
	    
		String ip = "192.168.102.136";
		CommandLineArgs commandLineArgs = cmdParser.parseArguments(args);
		
		parserController.filterLogFile(commandLineArgs);
		parserController.saveLog(commandLineArgs.getAccesslog());
	}
}
