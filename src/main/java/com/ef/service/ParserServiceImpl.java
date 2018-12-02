package com.ef.service;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ef.exception.InvalidLogFileException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.model.DurationType;
import com.ef.repository.ParserRepo;
import com.ef.util.ParserConstants;

@Service
public class ParserServiceImpl implements ParserService {

	@Autowired
	private ParserRepo parserRepo;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ParserServiceImpl.class);
	
	/*
	 * Filter requests either from log file or db.
	 * @param pathToFile - path to log file. Can be empty if filtering from db.
	 */	
	@Override
	public void saveLog(String pathToFile) throws InvalidLogFileException {
		
		if (!new File(pathToFile).exists()){
			throw new InvalidLogFileException();
		}
		
		parserRepo.saveLog(pathToFile);

	}
	
	/*
	 * Retrieves log from database based on filter params
	 * @param startDate is of "yyyy-MM-dd.HH:mm:ss" format
	 * @param duration can take only "hourly", "daily" 
	 * @param threshold can be an integer.
	 */
	
	@Override
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs) {

		try {
			
			Date endDate = calculatedEndDate(commandLineArgs.getStartDate(), commandLineArgs.getDuration());
			
			return parserRepo.findBlockedIPs(commandLineArgs, endDate);

		} catch (NumberFormatException e) {
			LOGGER.error(e.getMessage());
			throw e;
		}

	}

	/*
	 * Find requests made by a given IP.
	 * @param ipAddress
	 */
	@Override
	public boolean findByIP(String ipAddress) {

		InetAddressValidator inetValidator = InetAddressValidator.getInstance();

		// validate IP address
		if (null == ipAddress  || ipAddress.equals("") || !inetValidator.isValidInet4Address(ipAddress)) {
			LOGGER.error(ParserConstants.INVALID_IP);
			return false;
		}

		return parserRepo.findByIP(ipAddress);

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
	
	@Override
	public boolean saveBlockedIPs(List<BlockedIP> blockedIPs) {
		int[] saveResult = parserRepo.saveBlockedIPs(blockedIPs);
		return true;
	}
	
}
