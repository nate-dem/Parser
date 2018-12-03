package com.ef.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.ef.exception.InvalidLogFileException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.service.ParserService;

@Controller
public class ParserController {
	
	private final Logger logger = LoggerFactory.getLogger(ParserController.class);
	
	@Autowired
	ParserService parserService;
	
	public void processArgs(CommandLineArgs commandLineArgs) {
		
		saveLog(commandLineArgs);
		
		List<BlockedIP> blockedIPs = parserService.findBlockedIPs(commandLineArgs);
		
		if(!blockedIPs.isEmpty()) {
			parserService.saveBlockedIPs(blockedIPs);
		}
		
	}
	
	/*
	 * filter request by IP
	 */
	public void findByIp(String ip){
		parserService.findByIP(ip);
	}
	

	/*
	 * save log entries to db
	 */
	public void saveLog(CommandLineArgs commandLineArgs){
		
		String pathToFile = commandLineArgs.getAccesslog();
		if(pathToFile == null) {
			return;
		}
		
		 try {
			 parserService.saveLog(pathToFile);
		} catch (InvalidLogFileException e) {
			logger.error(e.getMessage());
		}
		 
	}
}
