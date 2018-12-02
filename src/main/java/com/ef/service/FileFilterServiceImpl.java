package com.ef.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.exception.InvalidLogFileException;
import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;
import com.ef.util.ParserConstants;

public class FileFilterServiceImpl implements FileFilterService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParserServiceImpl.class);
	
	/*
	 * Filter requests either from log file or db.
	 * @param pathToFile - path to log file. Can be empty if filtering from db.
	 * @param startDate is of "yyyy-MM-dd.HH:mm:ss" format
	 * @param duration can take only "hourly", "daily" 
	 * @param threshold can be an integer.
	 */
	//@Override
	public boolean filterFile(CommandLineArgs commandLineArgs) throws InvalidLogFileException {

		try {
			
			Date endDate = calculatedEndDate(commandLineArgs.getStartDate(), commandLineArgs.getDuration());
			
			return filterAccessLogFile(commandLineArgs.getAccesslog(), commandLineArgs.getStartDate(), endDate, commandLineArgs.getThreshold());

		} catch (NumberFormatException e) {
			//logger.error(e.getMessage());
			e.printStackTrace();
		}

		return false;

	}
	
	private Date calculatedEndDate(Date startDate, DurationType durationType){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		
		if (durationType == DurationType.HOURLY ) {
			calendar.set(Calendar.MINUTE, Calendar.MINUTE + 60);
		} else if (durationType == DurationType.DAILY ) {
			calendar.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY + 24);
		} else {
			LOGGER.error(ParserConstants.INVALID_DURATION);
			//return false;
		}
		
		return calendar.getTime();
	}

	private boolean filterAccessLogFile(String pathToFile, Date parsedStartDate, Date parsedEndDate, int threshold) {

		if (!new File(pathToFile).exists()) {
			LOGGER.error(ParserConstants.INVALID_FILE);
			return false;
		}

		LOGGER.info(ParserConstants.PROCESSING_QUERY);

		try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

			Map<String, Long> counterMap = stream.filter(t -> {
				String[] trimInput = t.split(ParserConstants.LOG_FILE_DELIMITER);
				try {
					Date parsedDate = new SimpleDateFormat(ParserConstants.LOG_DATE_FORMAT).parse(trimInput[0]);
					if (parsedDate.compareTo(parsedStartDate) >= 0 && parsedDate.compareTo(parsedEndDate) <= 0) {
						return true;
					}
				} catch (ParseException e) {
					LOGGER.error(e.getMessage());
				}
				return false;
			}).map(str -> str.split("\\|")).map(str -> str[1])
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

			boolean hasResult = false;

			// The log file assumes 200 as hourly limit and 500 as daily limit,

			for (Entry<String, Long> k : counterMap.entrySet()) {
				if (k.getValue() >= threshold) {
					LOGGER.info("IP --> " + k.getKey());
					hasResult = true;
				}
			}

			if (hasResult) {
				return true;
			} else {
				LOGGER.info(ParserConstants.NO_RESULT_FOUND);
				return false;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

}
