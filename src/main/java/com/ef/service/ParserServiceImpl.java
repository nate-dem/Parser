package com.ef.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.exception.InvalidWebLogFileException;
import com.ef.exception.WebLogFileNotFoundException;
import com.ef.model.CommandLineArgs;
import com.ef.model.ServerRequest;
import com.ef.observer.ConsoleLogger;
import com.ef.observer.Observer;
import com.ef.repository.ParserRepo;
import com.ef.util.DurationType;
import com.ef.util.ParserConstants;

public class ParserServiceImpl implements ParserService {

	private ParserRepo parserRepo;
	private List<Observer> observers = new ArrayList<>();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ParserServiceImpl.class);
	
	public ParserServiceImpl(ParserRepo parserRepoImpl) {
		this.parserRepo = parserRepoImpl;
	}
	
	/*
	@Override
	public List<ServerRequest> parseWebServerAccessLogFile(String pathToFile){
		
		List<ServerRequest> serverRequests = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

			stream.forEach(str -> {
				String[] trimInput = str.split("\\|");
				
				try {
					Date parsedDate = new SimpleDateFormat(WebLogParserConstants.LOG_DATE_FORMAT).parse(trimInput[0]);
					// log format: Date, IP, Request, Status, User Agent
					ServerRequest req = new ServerRequest(parsedDate, trimInput[1], trimInput[2], trimInput[3], trimInput[4]);
					//serverRequests.add(req);
					System.out.println(req);
				} catch (ParseException e) {
					logger.error(e.getMessage());
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return serverRequests;
		
	}*/
	
	/*
	 * Filter requests either from log file or db.
	 * @param pathToFile - path to log file. Can be empty if filtering from db.
	 */	
	@Override
	public void saveLogs(String pathToFile) throws InvalidWebLogFileException {
		
		//if (!new File(pathToFile).exists()){
			//pathToFile = WebLogParserConstants.LOG_FILE_URL;
		//}
		try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

			parserRepo.saveRequestData(stream);

		} catch (Exception e) {
			throw new InvalidWebLogFileException(e.getMessage());
		}

	}
	
	/*
	 * Retrieves log from db.
	 * @param startDate is of "yyyy-MM-dd.HH:mm:ss" format
	 * @param duration can take only "hourly", "daily" 
	 * @param threshold can be an integer.
	 */
	
	@Override
	public boolean retrieveLogs(CommandLineArgs commandLineArgs) {

		try {
			Calendar calendar = Calendar.getInstance();
			//Date parsedStartDate = new SimpleDateFormat(WebLogParserConstants.START_DATE_FORMAT).parse(startDate);
			calendar.setTime(commandLineArgs.getStartDate());
			//int threshold = Integer.parseInt(thresholdInp);

			if (commandLineArgs.getDuration() == DurationType.HOURLY) {
				calendar.set(Calendar.MINUTE, Calendar.MINUTE + 60);
			} else if (commandLineArgs.getDuration() == DurationType.DAILY) {
				calendar.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY + 24);
			} else {
				LOGGER.error(ParserConstants.INVALID_DURATION);
				return false;
			}
			
			Date parsedEndDate = calendar.getTime();
			
			return parserRepo.filterRequestData(commandLineArgs.getStartDate(), parsedEndDate, commandLineArgs.getDuration(), commandLineArgs.getThreshold());

		} catch (NumberFormatException e) {
			//logger.error(e.getMessage());
			e.printStackTrace();
		}

		return false;

	}

	/*
	@Override
	public boolean retrieveLogs(String startDate, String duration, String thresholdInp) {

		try {
			Calendar calendar = Calendar.getInstance();
			Date parsedStartDate = new SimpleDateFormat(WebLogParserConstants.START_DATE_FORMAT).parse(startDate);
			calendar.setTime(parsedStartDate);
			int threshold = Integer.parseInt(thresholdInp);

			if (duration.equalsIgnoreCase(DurationType.HOURLY.toString())) {
				calendar.set(Calendar.MINUTE, Calendar.MINUTE + 60);
			} else if (duration.equalsIgnoreCase(DurationType.DAILY.toString())) {
				calendar.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY + 24);
			} else {
				logger.error(WebLogParserConstants.INVALID_DURATION);
				return false;
			}
			
			Date parsedEndDate = calendar.getTime();
			
			return parserRepo.filterRequestData(parsedStartDate, parsedEndDate, duration, threshold);

		} catch (ParseException | NumberFormatException e) {
			//logger.error(e.getMessage());
			e.printStackTrace();
		}

		return false;

	}*/
	
	/*
	 * Method to filter requests either from log file or db.
	 * @param pathToFile - path to log file. Can be empty if filtering from db.
	 * @param startDate is of "yyyy-MM-dd.HH:mm:ss" format
	 * @param duration can take only "hourly", "daily" 
	 * @param threshold can be an integer.
	 */
	@Override
	public boolean filterFile(CommandLineArgs commandLineArgs) throws WebLogFileNotFoundException {

		try {
			
			//Date parsedStartDate = commandLineArgs.getStartDate(); //new SimpleDateFormat(WebLogParserConstants.START_DATE_FORMAT).parse(startDate);
			
			//int threshold = commandLineArgs.getThreshold(); // Integer.parseInt(thresholdInp);

			Date endDate = calculatedEndDate(commandLineArgs.getStartDate(), commandLineArgs.getDuration());
			
			// if log file passed as param, read file and filter data based on CLI args.
			//if (!pathToFile.equals("")) {
				return filterAccessLogFile(commandLineArgs.getAccesslog(), commandLineArgs.getStartDate(), endDate, commandLineArgs.getThreshold());
			//} else {
				//return parserRepo.filterRequestData(parsedStartDate, parsedEndDate, duration, threshold);
			//}

		} catch (NumberFormatException e) {
			//logger.error(e.getMessage());
			e.printStackTrace();
		}

		return false;

	}
	
	/*
	 * Method to find requests made by a given IP.
	 * @param ipAddress
	 */
	@Override
	public boolean filterByIP(String ipAddress) {

		InetAddressValidator inetValidator = InetAddressValidator.getInstance();

		// validate IP address
		if (null == ipAddress  || ipAddress.equals("") || !inetValidator.isValidInet4Address(ipAddress)) {
			LOGGER.error(ParserConstants.INVALID_IP);
			return false;
		}

		return parserRepo.filterByIP(ipAddress);

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
		
		if (!new File(pathToFile).exists()){
			LOGGER.error(ParserConstants.INVALID_FILE);
			return false;
		}
		
		LOGGER.info(ParserConstants.PROCESSING_QUERY);
		
		try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

			Map<String, Long> counterMap = 
				stream.filter(t -> {
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
				})
			  .map(str -> str.split("\\|"))
			  .map(str -> str[1])
			  .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	
			boolean hasResult = false;
			
			// The log file assumes 200 as hourly limit and 500 as daily limit,
			
			for(Entry<String, Long> k : counterMap.entrySet()) {
				if (k.getValue() >= threshold) {
					LOGGER.info("IP --> " + k.getKey());
					// notifyObservers();
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
	
	public void addObservers(){
		Observer consoleLogger = new ConsoleLogger();
		observers.add(consoleLogger);
	}

	@Override
	public void notifyObservers() {
		// TODO Auto-generated method stub
		
	}

}
