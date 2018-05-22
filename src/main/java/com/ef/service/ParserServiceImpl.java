package com.ef.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.log4j.Logger;
import com.ef.repository.ParserRepo;
import com.ef.util.DurationProperty;
import com.ef.util.Helper;
import com.ef.util.Property;

public class ParserServiceImpl implements ParserService {

	private ParserRepo parserRepo;
	private Helper helper;
	private final Logger logger = Logger.getLogger(ParserService.class);
	
	public ParserServiceImpl(ParserRepo parserRepoImpl ) {
		this.parserRepo = parserRepoImpl;
		helper = new Helper();
	}
	
	/*
	 * Method to filter requests either from log file or db.
	 * @param pathToFile - path to log file. Can be empty if filtering from db.
	 */	
	public void saveRequestData(String pathToFile) {
		
		if (!new File(pathToFile).exists()){
			pathToFile = Property.LOG_FILE_URL;
		}
		try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

			parserRepo.saveRequestData(stream);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	/*
	 * Method to filter requests either from log file or db.
	 * @param pathToFile - path to log file. Can be empty if filtering from db.
	 * @param startDate is of "yyyy-MM-dd.HH:mm:ss" format
	 * @param duration can take only "hourly", "daily" 
	 * @param threshold can be an integer.
	 */
	public boolean filterRequestData(String pathToFile, String startDate, String duration, String thresholdInp) {

		try {
			Calendar calendar = Calendar.getInstance();
			Date parsedStartDate = helper.getFilterDateFormat().parse(startDate);
			calendar.setTime(parsedStartDate);
			int threshold = Integer.parseInt(thresholdInp);

			if (duration.equalsIgnoreCase(DurationProperty.HOURLY.toString())) {
				calendar.set(Calendar.MINUTE, Calendar.MINUTE + 60);
			} else if (duration.equalsIgnoreCase(DurationProperty.DAILY.toString())) {
				calendar.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY + 24);
			} else {
				logger.error(Property.INVALID_DURATION);
				return false;
			}
			
			Date parsedEndDate = calendar.getTime();
			
			// if log file passed as param, read file and filter data based on CLI args.
			if (!pathToFile.equals("")) {
				return parserRepo.filterAccessLogFile(pathToFile, parsedStartDate, parsedEndDate, threshold);
			} else {
				return parserRepo.filterRequestData(parsedStartDate, parsedEndDate, duration, threshold);
			}

		} catch (ParseException | NumberFormatException e) {
			logger.error(e.getMessage());
		}

		return false;

	}

	/*
	 * Method to find requests made by a given IP.
	 * @param ipAddress
	 */
	public boolean filterByIP(String ipAddress) {

		InetAddressValidator inetValidator = InetAddressValidator.getInstance();

		// validate IP address
		if (ipAddress.equals("") || !inetValidator.isValidInet4Address(ipAddress)) {
			logger.error(Property.INVALID_IP);
			return false;
		}

		return parserRepo.filterByIP(ipAddress);

	}
}
