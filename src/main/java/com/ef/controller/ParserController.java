package com.ef.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.exception.InvalidWebLogFileException;
import com.ef.model.CommandLineArgs;
import com.ef.service.ParserService;

public class ParserController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(ParserController.class);
	
	ParserService logParserService;
	
	public ParserController(ParserService parserService){
		this.logParserService = parserService;
	}
	
	/*
	 * filter log record from db. Search results are also saved to another table.
	 */
	public void retrieveLogs(CommandLineArgs commandLineArgs){

		//logParserService.filterRequestData("", "2017-01-01.13:00:00", "daily", "250");
		//logParserService.filterRequestData("", "2017-01-01.00:00:00", "daily", "500");
		logParserService.retrieveLogs(commandLineArgs);
	}
	
	/*
	 * filter log record from file 
	 */
	public void filterLogFile(CommandLineArgs commandLineArgs){
	
		 try {
			 logParserService.filterFile(commandLineArgs);
			//parserService.filterLogFile(pathToFile, "2017-01-01.00:00:00", "hourly", "500");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	/*
	 * filter request by IP
	 */
	public void findByIp(String ip){
		logParserService.filterByIP(ip);
	}
	
	/*
	 * save log file records to db
	 */
	public void saveLog(String pathToFile){
		
		 try {
			 logParserService.saveLogs(pathToFile);
		} catch (InvalidWebLogFileException e) {
			LOGGER.error(e.getMessage());
		}
		 
	}
}
