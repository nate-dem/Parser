package com.ef.service;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.ef.exception.InvalidLogFileException;
import com.ef.exception.ParserServiceException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;
import com.ef.repository.ParserRepo;
import com.ef.util.ParserConstants;

@Service
public class ParserServiceImpl implements ParserService {

	@Autowired
	private ParserRepo parserRepo;
	
	@Autowired
	private MessageSource messageSource;
	
	private static final Logger logger = LoggerFactory.getLogger(ParserServiceImpl.class);
	
	/*
	 * Save log entries to DB
	 * @param pathToFile - path to log file.
	 */	
	@Override
	public void saveLog(String pathToFile) throws InvalidLogFileException {

		if (!new File(pathToFile).exists()){
			throw new InvalidLogFileException(messageSource.getMessage("parser.validation.error.file.not.found", null, Locale.US));
		}
		
		parserRepo.saveLog(pathToFile);

	}
	
	/*
	 * Retrieves blocked IPs from database based on filter params
	 * @param startDate is of "yyyy-MM-dd.HH:mm:ss" format
	 * @param duration can take only "hourly", "daily" 
	 * @param threshold can be an integer.
	 */
	
	@Override
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs) throws ParserServiceException {

		Date endDate = calculatedEndDate(commandLineArgs.getStartDate(), commandLineArgs.getDuration());

		return parserRepo.findBlockedIPs(commandLineArgs, endDate);

	}
	
	@Override
	public void saveBlockedIPs(List<BlockedIP> blockedIPs) {
		parserRepo.saveBlockedIPs(blockedIPs);
	}
	
	private Date calculatedEndDate(Date startDate, DurationType durationType) throws ParserServiceException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		
		if (durationType == DurationType.HOURLY ) {
			calendar.set(Calendar.MINUTE, Calendar.MINUTE + 60);
		} else if (durationType == DurationType.DAILY ) {
			calendar.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY + 24);
		} else {
			logger.error(messageSource.getMessage("parser.validation.error.invalid.duration", null, Locale.US));
			throw new ParserServiceException();
		}
		
		return calendar.getTime();
	}	
	
}
