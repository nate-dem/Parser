package com.ef.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;

public class CommandLineArgsParser {
	
	private static final Logger LOGGER =  LoggerFactory.getLogger(CommandLineArgsParser.class);
	
	private Options options;
	private CommandLineParser parser;
	
	public CommandLineArgsParser(Options options, CommandLineParser parser){
		this.options = options;
		this.parser = parser;
	}

	public CommandLineArgs parseArguments(String[] args){

		String startDateInp = null;
		String durationInp = null;
		String thresholdInp = null;
		String accesslogInp = null;
		CommandLineArgs commandLineArgs = null;
		boolean validCmdLineInput = false;
		
		try {
			CommandLine cmd = parser.parse(options, args);

			// Parse arg: accesslog
			if (cmd.getOptionValue("a") != null ) {
				accesslogInp = cmd.getOptionValue("a");
			}
			
			 // Parse args: startDate, duration, threshold
			if (cmd.getOptionValue("s") != null && cmd.getOptionValue("d") != null && cmd.getOptionValue("t") != null) {
				startDateInp = cmd.getOptionValue("s");
				durationInp = cmd.getOptionValue("d");
				thresholdInp = cmd.getOptionValue("t");
				validCmdLineInput = true;
			}
			
			/*
			// mock args for testing
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("access.log").getFile());
			
			accesslogInp = file.getAbsolutePath(); // "C:/ef/access.log";
			thresholdInp = "500";
			durationInp = "daily";
			startDateInp = "2017-01-01.00:00:00";
			// end of mock
			*/
			
			int threshold = Integer.parseInt(thresholdInp);
			Date startDate= new SimpleDateFormat(ParserConstants.START_DATE_FORMAT).parse(startDateInp);
			
			commandLineArgs = new CommandLineArgs(startDate, DurationType.fromValue(durationInp), threshold, accesslogInp);
			
		} catch (ParseException | java.text.ParseException | NumberFormatException e) {
			LOGGER.error(e.getMessage());
		}
		
		return commandLineArgs;

	}
}
