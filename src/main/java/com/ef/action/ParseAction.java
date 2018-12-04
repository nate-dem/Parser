package com.ef.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ef.exception.InvalidLogFileException;
import com.ef.exception.ParserServiceException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.service.ParserService;
import com.ef.util.CommandLineArgsParser;

@Controller
public class ParseAction {
	
	private final Logger logger = LoggerFactory.getLogger(ParseAction.class);
	
	@Autowired
	private ParserService parserService;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private CommandLineArgsParser cmdLineArgsParser;
	
	public void execute(String[] args) {
		
		CommandLineArgs commandLineArgs = parseArgs(args);
		
		System.out.println(commandLineArgs);
		String pathToFile = commandLineArgs.getAccesslog();
		
		// if accesslog flag is present, save log to db
		if(pathToFile != null) {
			saveLog(pathToFile);
		}
		
		List<BlockedIP> blockedIPs = findBlockedIPs(commandLineArgs);
		
		if(!blockedIPs.isEmpty()) {
			parserService.saveBlockedIPs(blockedIPs);
		}
		
	}
	
	private CommandLineArgs parseArgs(String[] args){
		CommandLineArgs commandLineArgs = null;
		try {
			commandLineArgs = cmdLineArgsParser.parseArguments(args);
		} catch (Exception e) {
			logger.error(messageSource.getMessage("parser.error.invalid.args.input", new Object[] {}, Locale.US));
		}
		return commandLineArgs;
	}
	
	/*
	 * save log entries to DB
	 */
	public void saveLog(String pathToFile){
		
		try {
			 parserService.saveLog(pathToFile);
		} catch (InvalidLogFileException e) {
			logger.error(e.getMessage());
		}
		 
	}
	
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs){
		List<BlockedIP> blockedIPs = new ArrayList<>();
		try {
			blockedIPs = parserService.findBlockedIPs(commandLineArgs);
		} catch (ParserServiceException e) {
			logger.error(e.getMessage());
		}
		return blockedIPs;
	}
	
}
