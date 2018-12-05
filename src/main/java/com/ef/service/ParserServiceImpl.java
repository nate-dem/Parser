package com.ef.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ef.exception.InvalidLogFileException;
import com.ef.exception.ParserServiceException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;
import com.ef.model.LogEntry;
import com.ef.repository.ParserRepo;
import com.ef.util.DateForamtter;
import com.ef.util.ParserConstants;
import com.ef.validation.LogEntryValidator;

@Service
public class ParserServiceImpl implements ParserService {

	@Autowired
	private ParserRepo parserRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(ParserServiceImpl.class);
	
	/*
	 * Save Log entries to DB
	 * @param pathToFile - path to log file.
	 */	
	@Override
	public int saveLogEntries(String pathToFile) throws InvalidLogFileException {

		final AtomicInteger affectedCount = new AtomicInteger();
		if (!new File(pathToFile).exists()){
			throw new InvalidLogFileException("Log File not found");
		}
		
		try (Stream<String> lines = Files.lines(Paths.get(pathToFile))) {
			final AtomicInteger lineCount = new AtomicInteger();
			List<LogEntry> logEntries = new ArrayList<>();
			long start = System.currentTimeMillis();
			lines.forEach(line -> {
				String[] entryStr = line.split(ParserConstants.LOG_FILE_DELIMITER);
				
				if(!LogEntryValidator.validate(entryStr))
					return;
				
				LogEntry logEntry = populateLogEntry(entryStr);
				
				if(logEntry != null) {
					logEntries.add(logEntry);
					logger.info("Processing file line # {} ", lineCount.incrementAndGet() );
				}
				
				if(logEntries.size() % 20000 == 0) {
					affectedCount.getAndAdd(parserRepo.saveLogEntries(logEntries));
					logEntries.clear();
				}
				
			});
			
			if(!logEntries.isEmpty()) {
				parserRepo.saveLogEntries(logEntries);
				logEntries.clear();
			}
			if(affectedCount.get() > 0) {
				logger.info("Database Insertion completed. Time taken: {} ", System.currentTimeMillis()-start );
			} else {
				logger.info("No log entry has been saved to database");
			}
			
		} catch (IOException | InvalidPathException  e) {
			throw new InvalidLogFileException("Error saving log: " + e);
		}
		
		return affectedCount.get();
		
	}
	
	/*
	 * Retrieves blocked IPs from database based on command line filter args
	 * @param CommandLineArgs
	 * @throws ParserServiceException
	 */
	
	@Override
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs) throws ParserServiceException {

		Date endDate = calculatedEndDate(commandLineArgs.getStartDate(), commandLineArgs.getDuration());

		return parserRepo.findBlockedIPs(commandLineArgs, endDate);

	}
	
	@Override
	public int saveBlockedIPs(List<BlockedIP> blockedIPs) {
		return parserRepo.saveBlockedIPs(blockedIPs);
	}
	
	private Date calculatedEndDate(Date startDate, DurationType durationType) throws ParserServiceException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		
		if (durationType == DurationType.HOURLY ) {
			calendar.set(Calendar.MINUTE, Calendar.MINUTE + 60);
		} else if (durationType == DurationType.DAILY ) {
			calendar.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY + 24);
		} else {
			throw new ParserServiceException("Invalid Duration");
		}
		
		return calendar.getTime();
	}
	
	private LogEntry populateLogEntry(String[] entryStr){
		LogEntry entry = null;
		try {
			
			Date parsedDate = DateForamtter.fromString(entryStr[0], ParserConstants.LOG_DATE_FORMAT);

			entry = new LogEntry();
			entry.setDate(parsedDate);
			entry.setIP(entryStr[1]);
			entry.setRequestMethod(entryStr[2]);
			entry.setStatus(entryStr[3]);
			entry.setUserAgent(entryStr[4]);
			
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
		return entry;
	}
	
}
