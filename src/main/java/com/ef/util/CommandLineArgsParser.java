package com.ef.util;

import java.io.File;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.exception.CommandLineArgsParseException;
import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;

public class CommandLineArgsParser {

	private static final Logger logger = LoggerFactory.getLogger(CommandLineArgsParser.class);

	private Options options;
	private CommandLineParser parser;

	public CommandLineArgsParser(Options options, CommandLineParser parser) {
		this.options = options;
		this.parser = parser;
	}

	public CommandLineArgs parseArguments(String[] args) throws CommandLineArgsParseException {

		String startDateInp = null;
		String durationInp = null;
		String thresholdInp = null;
		String accesslogInp = null;
		CommandLineArgs commandLineArgs = null;

		try {
			CommandLine cmd = parser.parse(options, args);

			// parse arg: accesslog
			if (cmd.getOptionValue("a") != null) {
				accesslogInp = cmd.getOptionValue("a");
			}

			// parse args: startDate, duration, threshold
			if (cmd.getOptionValue("s") != null && cmd.getOptionValue("d") != null && cmd.getOptionValue("t") != null) {
				startDateInp = cmd.getOptionValue("s");
				durationInp = cmd.getOptionValue("d");
				thresholdInp = cmd.getOptionValue("t");
			}

			// mock args for testing
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("access.log").getFile());

			accesslogInp = file.getAbsolutePath();
			thresholdInp = "500";
			durationInp = "daily";
			startDateInp = "2017-01-01.00:00:00";
			// end of mock

			int threshold = Integer.parseInt(thresholdInp);
			Date startDate = DateForamtter.fromString(startDateInp, ParserConstants.START_DATE_FORMAT);

			commandLineArgs = new CommandLineArgs(startDate, DurationType.fromValue(durationInp), threshold,
					accesslogInp);

		} catch (NumberFormatException | ParseException | java.text.ParseException e) {
			throw new CommandLineArgsParseException("Unable to parse cmd args: " + e.getMessage());
		}

		return commandLineArgs;

	}
}
