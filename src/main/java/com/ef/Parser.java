package com.ef;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import com.ef.repository.ParserRepo;
import com.ef.repository.ParserRepoImpl;
import com.ef.service.ParserService;
import com.ef.service.ParserServiceImpl;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

/*
 * Java parser that parses web server access log file and save to MySQL db.
 *
 * @author Natnael Demisse
 * @version 1.0
 * @since 2017-12-29
 */
public class Parser {

	private static final Logger logger = Logger.getLogger(Parser.class);
	
	public static void main(String[] args) {

		// instantiate parser service and repo layers
		ParserRepo parserRepo = new ParserRepoImpl();
		ParserService parserService = new ParserServiceImpl(parserRepo);
		
		// setup options to parse command line arguments
		Options options = new Options();
		
		options.addOption("s", "startDate", true, "Start date");
		options.addOption("d", "duration", true, "Duration");
		options.addOption("t", "threshold", true, "Threshold");
		options.addOption("a", "accesslog", true, "Access log file");

		String startDate = "";
		String duration = "";
		String threshold = "";
		String pathToFile = "";

		// parse the command line arguments
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

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
				pathToFile = stringBuilder.toString();
				startDate = cmd.getOptionValue("s");
				duration = cmd.getOptionValue("d");
				threshold = cmd.getOptionValue("t");
				//parserService.filterRequestData(pathToFile, startDate, duration, threshold);
			}
			/*
			 * CLI Args: startDate, duration, threshold
			 */
			else if (cmd.getOptionValue("s") != null && cmd.getOptionValue("d") != null && cmd.getOptionValue("t") != null) {
				startDate = cmd.getOptionValue("s");
				duration = cmd.getOptionValue("d");
				threshold = cmd.getOptionValue("t");
				parserService.filterRequestData("", startDate, duration, threshold);
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
		
		/*
		 * uncomment the codes below and change log file path to test various functionalities 
		 */
		
		pathToFile = "C:/Users/Nate/Desktop/Java_MySQL_Test/access.log";
		
		/*
		 * save log file records to db
		 */
		 parserService.saveRequestData(pathToFile);

		/*
		 * filter log record from db. Search results are also saved to another table.
		 */
		parserService.filterRequestData("", "2017-01-01.13:00:00", "daily", "250");
		parserService.filterRequestData("", "2017-01-01.00:00:00", "daily", "500");
		
		/*
		 * filter log record from file 
		 */
		//parserService.filterRequestData(pathToFile, "2017-01-01.00:00:00", "hourly", "500");
		 //parserService.filterRequestData(pathToFile, "2017-01-01.13:00:00", "daily", "250");

		/*
		 * filter request by IP
		 */
		 //parserService.filterByIP("192.168.102.136");

	}
}
