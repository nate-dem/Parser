package com.ef.repository;

import java.util.Date;
import java.util.List;

import com.ef.exception.InvalidLogFileException;
import com.ef.model.BlockedIP;
import com.ef.model.CommandLineArgs;
import com.ef.observer.Observable;

public interface ParserRepo extends Observable {
	
	public void saveLog(String pathToFile)  throws InvalidLogFileException ;
	
	public List<BlockedIP> findBlockedIPs(CommandLineArgs commandLineArgs, Date endDate);

	public boolean findByIP(String ipAddress);
	
	public int[] saveBlockedIPs(List<BlockedIP> blockedIPs);
	
}
