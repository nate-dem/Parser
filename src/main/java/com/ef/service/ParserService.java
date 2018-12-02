package com.ef.service;

import java.util.List;

import com.ef.exception.InvalidLogFileException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;

public interface ParserService {
	
	public void saveLog(String pathToFile) throws InvalidLogFileException;

	public boolean findByIP(String ipAddress);
	
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs);
	
	//public List<BlockedIP> prepareBlockedIPsForSave(CommandLineArgs commandLineArgs, List<IPRequest> ipRequests);
	
	public boolean saveBlockedIPs(List<BlockedIP> blockedIPs);
	
}
