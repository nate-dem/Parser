package com.ef;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ef.config.AppConfig;
import com.ef.controller.ParserController;
import com.ef.model.CommandLineArgs;
import com.ef.util.CommandLineArgsParser;

/*
 * Java parser that parses web server access log file and save to MySQL db.
 *
 * @author Natnael Demisse
 * @version 1.0
 * @since 2017-12-29
 */
public class Parser {

	public static void main(String[] args) {
		
	    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

	    CommandLineArgsParser cmdLineArgsParser = ctx.getBean(CommandLineArgsParser.class);
	    ParserController parserController = ctx.getBean(ParserController.class);
	    
		CommandLineArgs commandLineArgs = cmdLineArgsParser.parseArguments(args);
		
		parserController.processArgs(commandLineArgs);
		
		((AnnotationConfigApplicationContext)ctx).close();
	}
}
