package com.ef.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.model.CommandLineArgs;
import com.ef.util.DurationType;
import com.ef.util.ParserConstants;

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
		
		try {
			CommandLine cmd = parser.parse(options, args);

			/*
			 * CLI args: accesslog, startDate, duration, threshold
			 */
			if (cmd.getOptionValue("a") != null && cmd.getOptionValue("s") != null && cmd.getOptionValue("d") != null && cmd.getOptionValue("t") != null) {
				
				// reformat accesslog arg (/path/to/file) to (\path\to\file)
				String[] pathToFileArr = cmd.getOptionValue("a").split("\\/");
				StringBuilder stringBuilder = new StringBuilder();
				for (String p : pathToFileArr) {
					stringBuilder.append(p).append("\\");
				}
				accesslogInp = stringBuilder.toString();
				startDateInp = cmd.getOptionValue("s");
				durationInp = cmd.getOptionValue("d");
				thresholdInp = cmd.getOptionValue("t");

				//parserController.filterLogFile(pathToFile, commandLineArg1);
			}
			/*
			 * CLI Args: startDate, duration, threshold
			 */
			else if (cmd.getOptionValue("s") != null && cmd.getOptionValue("d") != null && cmd.getOptionValue("t") != null) {
				startDateInp = cmd.getOptionValue("s");
				durationInp = cmd.getOptionValue("d");
				thresholdInp = cmd.getOptionValue("t");
				
				//parserService.filterRequestData("", startDate, duration, threshold);
			} else {
				//throw new IllegalArgumentException("Unsupported Command Line args");
			}
			
			// mock args for testing
			accesslogInp = "C:/ef/access.log";
			thresholdInp = "250";
			durationInp = "daily";
			startDateInp = "2017-01-01.13:00:00";
			// end of mock
			
			int threshold = Integer.parseInt(thresholdInp);
			Date startDate= new SimpleDateFormat(ParserConstants.START_DATE_FORMAT).parse(startDateInp);
			
			commandLineArgs = new CommandLineArgs(startDate, DurationType.fromValue(durationInp), threshold, accesslogInp);
			
		} catch (ParseException | java.text.ParseException | NumberFormatException e) {
			LOGGER.error(e.getMessage());
		}
		
		return commandLineArgs;

	}
}
