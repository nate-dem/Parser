package com.ef.action;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;

import com.ef.exception.CommandLineArgsParseException;
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
		
		try {
			CommandLineArgs commandLineArgs = cmdLineArgsParser.parseArguments(args);

			Assert.notNull(commandLineArgs, "commandLineArgs object must not be null");
			
			String pathToFile = commandLineArgs.getAccesslog();
			
			// if accesslog flag is present, save log entries to db
			if(pathToFile != null) {
				parserService.saveLogEntries(pathToFile);
			}
			
			List<BlockedIP> blockedIPs = parserService.findBlockedIPs(commandLineArgs);
			
			if(!blockedIPs.isEmpty()) {
				parserService.saveBlockedIPs(blockedIPs);
			}
			
		} catch (CommandLineArgsParseException | InvalidLogFileException | ParserServiceException e) {
			logger.error(messageSource.getMessage("parser.error", new Object[] { e.getMessage() }, Locale.US));
		}
		
	}

}
