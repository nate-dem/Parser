package com.ef.service;

import java.util.List;

import com.ef.exception.InvalidLogFileException;
import com.ef.exception.ParserServiceException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;

public interface ParserService {
	
	public int saveLogEntries(String pathToFile) throws InvalidLogFileException;

	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs) throws ParserServiceException;
	
	public int saveBlockedIPs(List<BlockedIP> blockedIPs);
	
}
